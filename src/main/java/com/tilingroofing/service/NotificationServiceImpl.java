package com.tilingroofing.service;

import com.tilingroofing.api.dto.response.NotificationResponse;
import com.tilingroofing.api.dto.response.PagedResponse;
import com.tilingroofing.api.dto.response.UserInfo;
import com.tilingroofing.common.exception.ResourceNotFoundException;
import com.tilingroofing.domain.entity.Booking;
import com.tilingroofing.domain.entity.Notification;
import com.tilingroofing.domain.entity.User;
import com.tilingroofing.domain.repository.BookingRepository;
import com.tilingroofing.domain.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService.
 * Handles business logic for retrieving and creating notifications.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            BookingRepository bookingRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getAllNotifications(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Notification> notificationPage = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        List<Notification> notifications = notificationPage.getContent();
        
        // Initialize booking and user relationships to avoid lazy loading issues
        notifications.forEach(this::triggerLazyLoading);
        
        List<NotificationResponse> responses = notifications.stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());

        return PagedResponse.from(notificationPage, responses);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationResponse createNotification(Long bookingId, String message) {
        log.debug("Creating notification for booking ID: {} with message: {}", bookingId, message);
        
        // Check if notification already exists for this booking (idempotent operation)
        Optional<Notification> existingNotification = notificationRepository.findByBookingId(bookingId);
        if (existingNotification.isPresent()) {
            Notification notification = existingNotification.get();
            log.info("Notification already exists for booking ID: {}, returning existing notification", bookingId);
            triggerLazyLoading(notification);
            return toNotificationResponse(notification);
        }

        // Fetch booking - ensure it exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found for ID: {} when creating notification", bookingId);
                    return new ResourceNotFoundException("Booking", "id", bookingId);
                });

        log.debug("Found booking: {} for notification creation", booking.getBookingRef());

        // Create new notification
        Notification notification = Notification.builder()
                .booking(booking)
                .message(message)
                .isRead(false)
                .build();

        try {
            // Use saveAndFlush to ensure ID is generated immediately
            notification = notificationRepository.saveAndFlush(notification);
            
            // Verify ID was generated
            if (notification.getId() == null) {
                log.error("Notification ID is null after save for booking ID: {}", bookingId);
                throw new RuntimeException("Failed to generate notification ID");
            }
            
            log.info("Successfully created notification (ID: {}) for booking: {}", 
                    notification.getId(), booking.getBookingRef());
            
            // Trigger lazy loading to ensure all relationships are loaded
            triggerLazyLoading(notification);
            
            return toNotificationResponse(notification);
        } catch (Exception e) {
            log.error("Failed to save notification for booking ID {}: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("Failed to create notification: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            log.debug("Notification {} is already marked as read", notificationId);
            // Still need to trigger lazy loading for complete response
            triggerLazyLoading(notification);
            return toNotificationResponse(notification);
        }

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        log.info("Marked notification {} as read", notificationId);

        // Trigger lazy loading for complete response
        triggerLazyLoading(notification);

        return toNotificationResponse(notification);
    }

    /**
     * Triggers lazy loading for booking and user relationships to avoid N+1 queries.
     */
    private void triggerLazyLoading(Notification notification) {
        if (notification.getBooking() != null) {
            Booking booking = notification.getBooking();
            booking.getBookingRef();
            booking.getStatus();
            booking.getServiceId();
            booking.getSuburb();
            booking.getPostcode();
            booking.getPreferredDate();
            booking.getTimeSlot();
            if (booking.getUser() != null) {
                User user = booking.getUser();
                user.getName();
                user.getEmail();
                user.getPicture();
            }
        }
    }

    /**
     * Maps Notification entity to NotificationResponse DTO.
     * Includes essential booking and user information.
     */
    private NotificationResponse toNotificationResponse(Notification notification) {
        Booking booking = notification.getBooking();
        
        NotificationResponse.BookingInfo bookingInfo = null;
        UserInfo userInfo = null;

        if (booking != null) {
            // Build booking info
            bookingInfo = NotificationResponse.BookingInfo.builder()
                    .bookingRef(booking.getBookingRef())
                    .status(booking.getStatus() != null ? booking.getStatus().getValue() : null)
                    .serviceId(booking.getServiceId())
                    .suburb(booking.getSuburb())
                    .postcode(booking.getPostcode())
                    .preferredDate(booking.getPreferredDate())
                    .timeSlot(booking.getTimeSlot() != null ? booking.getTimeSlot().getValue() : null)
                    .build();

            // Build user info if available
            User user = booking.getUser();
            if (user != null) {
                userInfo = UserInfo.builder()
                        .id(user.getId() != null ? user.getId().toString() : null)
                        .email(user.getEmail())
                        .name(user.getName())
                        .picture(user.getPicture())
                        .build();
            }
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .bookingId(booking != null ? booking.getId() : null)
                .bookingRef(booking != null ? booking.getBookingRef() : null)
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .booking(bookingInfo)
                .user(userInfo)
                .build();
    }
}

