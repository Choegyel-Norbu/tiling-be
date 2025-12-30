package com.tilingroofing.api.controller;

import com.tilingroofing.api.dto.request.BlockDatesRequest;
import com.tilingroofing.api.dto.response.ApiResponse;
import com.tilingroofing.api.dto.response.BlockedDateResponse;
import com.tilingroofing.service.BlockedDateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for blocked date operations.
 * Provides endpoints for managing dates that are unavailable for booking.
 */
@RestController
@RequestMapping("/api/bookings/block-dates")
@Tag(name = "Blocked Dates", description = "Blocked date management endpoints")
public class BlockedDateController {

    private final BlockedDateService blockedDateService;

    public BlockedDateController(BlockedDateService blockedDateService) {
        this.blockedDateService = blockedDateService;
    }

    /**
     * Blocks multiple dates from booking.
     */
    @PostMapping
    @Operation(summary = "Block dates", description = "Blocks one or more dates from being booked")
    public ResponseEntity<ApiResponse<List<BlockedDateResponse>>> blockDates(
            @Valid @RequestBody BlockDatesRequest request
    ) {
        List<BlockedDateResponse> blockedDates = blockedDateService.blockDates(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(blockedDates, "Dates blocked successfully"));
    }

    /**
     * Gets all upcoming blocked dates.
     */
    @GetMapping
    @Operation(summary = "Get blocked dates", description = "Retrieves all upcoming blocked dates")
    public ResponseEntity<ApiResponse<List<BlockedDateResponse>>> getBlockedDates(
            @Parameter(description = "Start date for range filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for range filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<BlockedDateResponse> blockedDates;
        
        if (startDate != null && endDate != null) {
            blockedDates = blockedDateService.getBlockedDatesInRange(startDate, endDate);
        } else {
            blockedDates = blockedDateService.getUpcomingBlockedDates();
        }
        
        return ResponseEntity.ok(ApiResponse.success(blockedDates));
    }

    /**
     * Checks if a specific date is blocked.
     */
    @GetMapping("/check")
    @Operation(summary = "Check if date is blocked", description = "Checks whether a specific date is blocked")
    public ResponseEntity<ApiResponse<Boolean>> isDateBlocked(
            @Parameter(description = "Date to check")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        boolean isBlocked = blockedDateService.isDateBlocked(date);
        return ResponseEntity.ok(ApiResponse.success(isBlocked));
    }

    /**
     * Unblocks a date.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Unblock a date", description = "Removes a blocked date")
    public ResponseEntity<ApiResponse<Void>> unblockDate(
            @Parameter(description = "Blocked date ID")
            @PathVariable Long id
    ) {
        blockedDateService.unblockDate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Date unblocked successfully"));
    }
}

