package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.CreateBookingRequest;
import com.tilingroofing.api.dto.request.UpdateBookingRequest;
import com.tilingroofing.api.dto.response.BookingResponse;
import com.tilingroofing.api.dto.response.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for managing bookings.
 * Defines the contract for booking operations.
 */
public interface BookingService {

    /**
     * Creates a new booking with optional file attachments.
     * 
     * @param request The booking creation request
     * @param files Optional list of files to attach
     * @param userId The ID of the user creating the booking
     * @return BookingResponse with the created booking
     */
    BookingResponse createBooking(CreateBookingRequest request, List<MultipartFile> files, Long userId);

    /**
     * Retrieves a booking by its reference number.
     * 
     * @param bookingRef The booking reference
     * @return BookingResponse if found
     */
    BookingResponse getBookingByRef(String bookingRef);

    /**
     * Retrieves a booking by its ID.
     * 
     * @param id The booking ID
     * @return BookingResponse if found
     */
    BookingResponse getBookingById(Long id);

    /**
     * Lists bookings with optional status filter and pagination.
     * 
     * @param status Optional status filter
     * @param search Optional search term
     * @param page Page number (0-based)
     * @param limit Page size
     * @return PagedResponse with bookings
     */
    PagedResponse<BookingResponse> listBookings(String status, String search, int page, int limit);

    /**
     * Lists bookings for a specific user with optional status filter and pagination.
     * 
     * @param userId The user ID
     * @param status Optional status filter
     * @param page Page number (0-based)
     * @param limit Page size
     * @return PagedResponse with bookings
     */
    PagedResponse<BookingResponse> getUserBookings(Long userId, String status, int page, int limit);

    /**
     * Updates the status of a booking.
     * 
     * @param id The booking ID
     * @param newStatus The new status
     * @return BookingResponse with the updated booking
     */
    BookingResponse updateBookingStatus(Long id, String newStatus);

    /**
     * Updates a booking with the provided fields.
     * 
     * @param id The booking ID
     * @param request The update request with optional fields
     * @param files Optional list of files to add
     * @return BookingResponse with the updated booking
     */
    BookingResponse updateBooking(Long id, UpdateBookingRequest request, List<MultipartFile> files);

    /**
     * Deletes a booking by its ID.
     * 
     * @param id The booking ID
     */
    void deleteBooking(Long id);
}
