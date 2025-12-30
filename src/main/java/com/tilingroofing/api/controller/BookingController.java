package com.tilingroofing.api.controller;

import com.tilingroofing.api.dto.request.CreateBookingRequest;
import com.tilingroofing.api.dto.request.UpdateBookingRequest;
import com.tilingroofing.api.dto.request.UpdateBookingStatusRequest;
import com.tilingroofing.api.dto.response.ApiResponse;
import com.tilingroofing.api.dto.response.BookingResponse;
import com.tilingroofing.api.dto.response.PagedResponse;
import com.tilingroofing.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for booking operations.
 * Provides endpoints for creating, retrieving, and managing bookings.
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a new booking with optional file attachments.
     * Requires authentication - user ID is extracted from JWT token.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new booking",
            description = "Creates a booking with optional file uploads. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @ModelAttribute CreateBookingRequest request,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        // Extract user ID from authentication context
        Long userId = Long.parseLong(authentication.getName());
        BookingResponse booking = bookingService.createBooking(request, files, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking, "Booking created successfully"));
    }

    /**
     * Retrieves a booking by its reference number.
     */
    @GetMapping("/{bookingRef}")
    @Operation(summary = "Get booking by reference", description = "Retrieves a booking using its reference number")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @Parameter(description = "Booking reference (e.g., TR-12345)")
            @PathVariable String bookingRef
    ) {
        BookingResponse booking = bookingService.getBookingByRef(bookingRef);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Lists all bookings with optional filtering and pagination (Admin).
     */
    @GetMapping
    @Operation(summary = "List bookings", description = "Lists all bookings with optional status filter and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> listBookings(
            @Parameter(description = "Filter by status (pending, confirmed, in_progress, completed, cancelled)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Search by booking ref, name, or email")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") int limit
    ) {
        // Enforce maximum page size
        limit = Math.min(limit, 100);
        
        PagedResponse<BookingResponse> bookings = bookingService.listBookings(status, search, page, limit);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Lists bookings for the authenticated user with optional status filter and pagination.
     */
    @GetMapping("/my-bookings")
    @Operation(
            summary = "Get user's bookings",
            description = "Retrieves all bookings for the authenticated user with optional status filter and pagination",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getMyBookings(
            @Parameter(description = "Filter by status (pending, confirmed, in_progress, completed, cancelled)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication
    ) {
        // Enforce maximum page size
        limit = Math.min(limit, 100);
        
        // Extract user ID from authentication context
        Long userId = Long.parseLong(authentication.getName());
        PagedResponse<BookingResponse> bookings = bookingService.getUserBookings(userId, status, page, limit);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Updates a booking with the provided fields (partial update).
     * Supports optional file uploads - new files will be added to the existing booking.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update booking",
            description = "Updates a booking with the provided fields. Only non-null fields will be updated. Optional file uploads are supported.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @Parameter(description = "Booking ID")
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateBookingRequest request,
            @Parameter(description = "Optional files to add to the booking")
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        BookingResponse booking = bookingService.updateBooking(id, request, files);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking updated successfully"));
    }

    /**
     * Updates the status of a booking (Admin).
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update booking status", description = "Updates the status of a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBookingStatus(
            @Parameter(description = "Booking ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        BookingResponse booking = bookingService.updateBookingStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(booking, "Status updated successfully"));
    }
}

