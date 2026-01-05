package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.CreateBookingRequest;
import com.tilingroofing.api.dto.request.UpdateBookingRequest;
import com.tilingroofing.api.dto.response.BookingResponse;
import com.tilingroofing.api.dto.response.PagedResponse;
import com.tilingroofing.api.mapper.BookingMapper;
import com.tilingroofing.common.exception.BusinessException;
import com.tilingroofing.common.exception.ResourceNotFoundException;
import com.tilingroofing.domain.entity.BlockedDate;
import com.tilingroofing.domain.entity.Booking;
import com.tilingroofing.domain.entity.BookingFile;
import com.tilingroofing.domain.entity.User;
import com.tilingroofing.domain.enums.BookingStatus;
import com.tilingroofing.domain.enums.JobSize;
import com.tilingroofing.domain.enums.TimeSlot;
import com.tilingroofing.domain.repository.BlockedDateRepository;
import com.tilingroofing.domain.repository.BookingRepository;
import com.tilingroofing.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of BookingService.
 * Handles business logic for creating, retrieving, and updating bookings.
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final Random RANDOM = new Random();

    private final BookingRepository bookingRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final String bookingRefPrefix;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BlockedDateRepository blockedDateRepository,
            UserRepository userRepository,
            BookingMapper bookingMapper,
            FileStorageService fileStorageService,
            EmailService emailService,
            @Lazy NotificationService notificationService,
            @Value("${app.booking.ref-prefix:TR}") String bookingRefPrefix
    ) {
        this.bookingRepository = bookingRepository;
        this.blockedDateRepository = blockedDateRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.bookingRefPrefix = bookingRefPrefix;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, List<MultipartFile> files, Long userId) {
        // Validate date is not blocked
        LocalDate preferredDate = LocalDate.parse(request.getDate());
        validateDateNotBlocked(preferredDate);

        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Generate unique booking reference
        String bookingRef = generateUniqueBookingRef();

        // Create booking entity
        Booking booking = Booking.builder()
                .bookingRef(bookingRef)
                .status(BookingStatus.PENDING)
                .serviceId(request.getServiceId())
                .jobSize(JobSize.fromValue(request.getJobSize()))
                .suburb(request.getSuburb())
                .postcode(request.getPostcode())
                .description(request.getDescription())
                .preferredDate(preferredDate)
                .timeSlot(TimeSlot.fromValue(request.getTimeSlot()))
                .user(user)
                .customerPhone(normalizePhone(request.getPhone()))
                .build();

        // Handle file uploads
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    BookingFile bookingFile = fileStorageService.storeFile(file, bookingRef);
                    booking.addFile(bookingFile);
                }
            }
        }

        // Save booking
        booking = bookingRepository.save(booking);
        log.info("Created booking: {}", bookingRef);

        // Block the booking date to prevent double bookings
        blockBookingDate(preferredDate, booking);

        // Prepare data for post-transaction operations
        final Booking savedBooking = booking;
        final String customerName = user.getName() != null ? user.getName() : user.getEmail();
        final String notificationMessage = String.format("You have a new booking: %s - %s, %s", 
                bookingRef, customerName, preferredDate);

        // Schedule non-critical operations to run after transaction commits
        // This prevents blocking the HTTP response and improves performance
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            log.debug("Starting post-transaction operations for booking: {}", bookingRef);
                            
                            // Create notification (idempotent - won't fail if already exists)
                            try {
                                notificationService.createNotification(savedBooking.getId(), notificationMessage);
                                log.info("Successfully created notification for booking: {}", bookingRef);
                            } catch (IllegalStateException e) {
                                // Notification already exists - this is okay, just log it
                                log.warn("Notification already exists for booking {}: {}", bookingRef, e.getMessage());
                            } catch (Exception e) {
                                // Log notification creation failure but continue with other operations
                                log.error("Failed to create notification for booking {}: {}", 
                                        bookingRef, e.getMessage(), e);
                            }
                            
                            // Send email notifications (already async, but now outside transaction)
                            try {
                                emailService.sendCustomerConfirmation(savedBooking);
                                log.debug("Sent customer confirmation email for booking: {}", bookingRef);
                            } catch (Exception e) {
                                log.error("Failed to send customer confirmation email for booking {}: {}", 
                                        bookingRef, e.getMessage(), e);
                            }
                            
                            try {
                                emailService.sendAdminNotification(savedBooking);
                                log.debug("Sent admin notification email for booking: {}", bookingRef);
                            } catch (Exception e) {
                                log.error("Failed to send admin notification email for booking {}: {}", 
                                        bookingRef, e.getMessage(), e);
                            }
                            
                            log.info("Post-transaction operations completed for booking: {}", bookingRef);
                        } catch (Exception e) {
                            // Catch-all for any unexpected errors
                            log.error("Unexpected error in post-transaction operations for booking {}: {}", 
                                    bookingRef, e.getMessage(), e);
                        }
                    }
                }
        );

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByRef(String bookingRef) {
        Booking booking = bookingRepository.findByBookingRef(bookingRef)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingRef", bookingRef));

        // Initialize files collection to avoid lazy loading issues
        if (booking.getFiles() != null) {
            booking.getFiles().size(); // Trigger lazy loading
        }

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        // Initialize files collection to avoid lazy loading issues
        if (booking.getFiles() != null) {
            booking.getFiles().size(); // Trigger lazy loading
        }

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> listBookings(String status, String search, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        BookingStatus bookingStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                bookingStatus = BookingStatus.fromValue(status);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("INVALID_STATUS", "Invalid booking status: " + status);
            }
        }

        Page<Booking> bookingPage = bookingRepository.searchBookings(bookingStatus, search, pageable);
        List<Booking> bookings = bookingPage.getContent();
        
        // Initialize files collection for each booking to avoid lazy loading issues
        bookings.forEach(booking -> {
            if (booking.getFiles() != null) {
                booking.getFiles().size(); // Trigger lazy loading
            }
        });
        
        List<BookingResponse> responses = bookingMapper.toBookingResponseList(bookings);

        return PagedResponse.from(bookingPage, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getUserBookings(Long userId, String status, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        BookingStatus bookingStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                bookingStatus = BookingStatus.fromValue(status);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("INVALID_STATUS", "Invalid booking status: " + status);
            }
        }

        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, bookingStatus, pageable);
        List<Booking> bookings = bookingPage.getContent();
        
        // Initialize files collection for each booking to avoid lazy loading issues
        bookings.forEach(booking -> {
            if (booking.getFiles() != null) {
                booking.getFiles().size(); // Trigger lazy loading
            }
        });
        
        List<BookingResponse> responses = bookingMapper.toBookingResponseList(bookings);

        return PagedResponse.from(bookingPage, responses);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, String newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        BookingStatus status = BookingStatus.fromValue(newStatus);
        BookingStatus oldStatus = booking.getStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, status);

        booking.setStatus(status);
        booking = bookingRepository.save(booking);

        log.info("Updated booking {} status from {} to {}", 
                booking.getBookingRef(), oldStatus, status);

        // Send status update email
        emailService.sendStatusUpdateEmail(booking);

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long id, UpdateBookingRequest request, List<MultipartFile> files) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        // Store original date for comparison
        LocalDate originalDate = booking.getPreferredDate();

        // Update fields only if provided (partial update)
        if (request.getServiceId() != null) {
            booking.setServiceId(request.getServiceId());
        }
        if (request.getJobSize() != null) {
            booking.setJobSize(JobSize.fromValue(request.getJobSize()));
        }
        if (request.getSuburb() != null) {
            booking.setSuburb(request.getSuburb());
        }
        if (request.getPostcode() != null) {
            booking.setPostcode(request.getPostcode());
        }
        if (request.getDescription() != null) {
            booking.setDescription(request.getDescription());
        }
        if (request.getDate() != null) {
            LocalDate newPreferredDate = LocalDate.parse(request.getDate());
            
            // Only validate and update blocked dates if the date is actually changing
            if (!newPreferredDate.equals(originalDate)) {
                // Validate new date is not blocked (excluding this booking's own blocked date)
                validateDateNotBlockedForUpdate(newPreferredDate, booking.getId());
                
                // Unblock the old date if it was blocked by this booking
                unblockBookingDate(originalDate, booking.getId());
                
                // Update the booking date
                booking.setPreferredDate(newPreferredDate);
                
                // Block the new date for this booking
                blockBookingDate(newPreferredDate, booking);
            }
            // If date is not changing, no validation or date blocking/unblocking needed
        }
        if (request.getTimeSlot() != null) {
            booking.setTimeSlot(TimeSlot.fromValue(request.getTimeSlot()));
        }
        if (request.getPhone() != null) {
            booking.setCustomerPhone(normalizePhone(request.getPhone()));
        }

        // Handle file uploads - add new files to existing booking
        if (files != null && !files.isEmpty()) {
            String bookingRef = booking.getBookingRef();
            int addedCount = 0;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    BookingFile bookingFile = fileStorageService.storeFile(file, bookingRef);
                    booking.addFile(bookingFile);
                    addedCount++;
                    log.debug("Added file {} to booking {}", file.getOriginalFilename(), bookingRef);
                }
            }
            if (addedCount > 0) {
                log.info("Added {} file(s) to booking {}", addedCount, bookingRef);
            }
        }

        booking = bookingRepository.save(booking);
        log.info("Updated booking: {}", booking.getBookingRef());

        return bookingMapper.toBookingResponse(booking);
    }

    /**
     * Validates that the date is not blocked.
     */
    private void validateDateNotBlocked(LocalDate date) {
        if (blockedDateRepository.existsByDate(date)) {
            throw new BusinessException("DATE_BLOCKED", 
                    "The selected date is not available for booking");
        }
    }

    /**
     * Validates that the date is not blocked, excluding the blocked date
     * associated with the current booking (if any).
     * Used when updating a booking to allow keeping the same date or changing
     * to a date that's only blocked by the current booking itself.
     */
    private void validateDateNotBlockedForUpdate(LocalDate date, Long currentBookingId) {
        Optional<BlockedDate> existingBlockedDate = blockedDateRepository.findByDate(date);
        
        if (existingBlockedDate.isPresent()) {
            BlockedDate blockedDate = existingBlockedDate.get();
            // Allow if the blocked date is linked to the current booking
            if (blockedDate.getBooking() != null && 
                blockedDate.getBooking().getId().equals(currentBookingId)) {
                return; // Date is blocked by this booking itself, which is allowed
            }
            // Date is blocked by another booking or manually blocked
            throw new BusinessException("DATE_BLOCKED", 
                    "The selected date is not available for booking");
        }
    }

    /**
     * Blocks a date when a booking is created or updated.
     * Links the blocked date to the booking for traceability.
     * If the date is already blocked, this method does nothing (idempotent).
     * 
     * @param date The date to block
     * @param booking The booking entity to link
     */
    private void blockBookingDate(LocalDate date, Booking booking) {
        // Check if date is already blocked
        if (blockedDateRepository.existsByDate(date)) {
            log.debug("Date {} is already blocked, skipping", date);
            return;
        }

        // Create blocked date entry linked to the booking
        BlockedDate blockedDate = BlockedDate.builder()
                .date(date)
                .reason(String.format("Booking created: %s", booking.getBookingRef()))
                .booking(booking)
                .build();

        blockedDateRepository.save(blockedDate);
        log.info("Blocked date {} for booking {}", date, booking.getBookingRef());
    }

    /**
     * Unblocks a date that was blocked by a specific booking.
     * Only removes the blocked date if it's linked to the specified booking.
     * 
     * @param date The date to unblock
     * @param bookingId The ID of the booking that blocked the date
     */
    private void unblockBookingDate(LocalDate date, Long bookingId) {
        Optional<BlockedDate> blockedDateOpt = blockedDateRepository.findByDate(date);
        
        if (blockedDateOpt.isPresent()) {
            BlockedDate blockedDate = blockedDateOpt.get();
            // Only unblock if it's linked to this booking
            if (blockedDate.getBooking() != null && 
                blockedDate.getBooking().getId().equals(bookingId)) {
                blockedDateRepository.delete(blockedDate);
                log.info("Unblocked date {} for booking {}", date, bookingId);
            } else {
                log.debug("Date {} is blocked by another booking or manually, not unblocking", date);
            }
        }
    }

    /**
     * Generates a unique booking reference in format TR-XXXXX.
     */
    private String generateUniqueBookingRef() {
        String bookingRef;
        int attempts = 0;
        do {
            int randomNumber = 10000 + RANDOM.nextInt(90000);
            bookingRef = bookingRefPrefix + "-" + randomNumber;
            attempts++;
            if (attempts > 100) {
                throw new BusinessException("REFERENCE_GENERATION_FAILED", 
                        "Failed to generate unique booking reference");
            }
        } while (bookingRepository.existsByBookingRef(bookingRef));

        return bookingRef;
    }

    /**
     * Normalizes phone number to consistent format.
     */
    private String normalizePhone(String phone) {
        // Remove all non-digit characters
        String digits = phone.replaceAll("[^0-9]", "");
        
        // If starts with 61, add + prefix
        if (digits.startsWith("61") && digits.length() == 11) {
            return "+61" + digits.substring(2);
        }
        
        // If starts with 0, convert to +61 format
        if (digits.startsWith("0") && digits.length() == 10) {
            return "+61" + digits.substring(1);
        }
        
        return phone;
    }

    /**
     * Validates status transition is allowed.
     * 
     * Allowed transitions:
     * - PENDING -> CONFIRMED, CANCELLED
     * - CONFIRMED -> IN_PROGRESS, COMPLETED, CANCELLED
     * - IN_PROGRESS -> COMPLETED, CANCELLED
     * - COMPLETED, CANCELLED -> no transitions allowed (terminal states)
     */
    private void validateStatusTransition(BookingStatus from, BookingStatus to) {
        // Define valid transitions
        boolean validTransition = switch (from) {
            case PENDING -> to == BookingStatus.CONFIRMED || to == BookingStatus.CANCELLED;
            case CONFIRMED -> to == BookingStatus.IN_PROGRESS || 
                             to == BookingStatus.COMPLETED || 
                             to == BookingStatus.CANCELLED;
            case IN_PROGRESS -> to == BookingStatus.COMPLETED || to == BookingStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new BusinessException("INVALID_STATUS_TRANSITION", 
                    String.format("Cannot transition from %s to %s", from.getValue(), to.getValue()));
        }
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        // Unblock the date if it was blocked by this booking
        unblockBookingDate(booking.getPreferredDate(), booking.getId());

        // Delete associated files
        fileStorageService.deleteBookingFiles(booking.getBookingRef());

        // Delete the booking
        bookingRepository.delete(booking);
        log.info("Deleted booking: {}", booking.getBookingRef());
    }
}

