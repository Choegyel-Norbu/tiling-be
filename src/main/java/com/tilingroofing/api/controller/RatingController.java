package com.tilingroofing.api.controller;

import com.tilingroofing.api.dto.request.CreateRatingRequest;
import com.tilingroofing.api.dto.request.UpdateRatingRequest;
import com.tilingroofing.api.dto.response.ApiResponse;
import com.tilingroofing.api.dto.response.RatingResponse;
import com.tilingroofing.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for rating operations.
 * Provides endpoints for creating, retrieving, updating, and deleting ratings.
 */
@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Ratings", description = "Rating and review management endpoints")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Creates a rating for a booking.
     * Requires authentication - user ID is extracted from JWT token.
     */
    @PostMapping("/bookings/{bookingId}")
    @Operation(
            summary = "Create rating for booking",
            description = "Creates a rating/review for a booking. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<RatingResponse>> createRating(
            @Parameter(description = "Booking ID")
            @PathVariable Long bookingId,
            @Valid @RequestBody CreateRatingRequest request,
            Authentication authentication
    ) {
        // Extract user ID from authentication context
        Long userId = Long.parseLong(authentication.getName());
        RatingResponse rating = ratingService.createRating(bookingId, request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(rating, "Rating created successfully"));
    }

    /**
     * Retrieves a rating by booking ID.
     */
    @GetMapping("/bookings/{bookingId}")
    @Operation(
            summary = "Get rating by booking ID",
            description = "Retrieves the rating for a specific booking"
    )
    public ResponseEntity<ApiResponse<RatingResponse>> getRatingByBookingId(
            @Parameter(description = "Booking ID")
            @PathVariable Long bookingId
    ) {
        RatingResponse rating = ratingService.getRatingByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    /**
     * Retrieves a rating by its ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get rating by ID",
            description = "Retrieves a rating by its ID"
    )
    public ResponseEntity<ApiResponse<RatingResponse>> getRatingById(
            @Parameter(description = "Rating ID")
            @PathVariable Long id
    ) {
        RatingResponse rating = ratingService.getRatingById(id);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    /**
     * Updates a rating.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update rating",
            description = "Updates a rating with the provided fields. Only non-null fields will be updated.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<RatingResponse>> updateRating(
            @Parameter(description = "Rating ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateRatingRequest request
    ) {
        RatingResponse rating = ratingService.updateRating(id, request);
        return ResponseEntity.ok(ApiResponse.success(rating, "Rating updated successfully"));
    }

    /**
     * Deletes a rating.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete rating",
            description = "Deletes a rating",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteRating(
            @Parameter(description = "Rating ID")
            @PathVariable Long id
    ) {
        ratingService.deleteRating(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Rating deleted successfully"));
    }

    /**
     * Gets the average rating across all ratings.
     */
    @GetMapping("/stats/average")
    @Operation(
            summary = "Get average rating",
            description = "Calculates and returns the average rating across all ratings"
    )
    public ResponseEntity<ApiResponse<Double>> getAverageRating() {
        Double average = ratingService.getAverageRating();
        return ResponseEntity.ok(ApiResponse.success(average));
    }

    /**
     * Gets the total count of ratings.
     */
    @GetMapping("/stats/count")
    @Operation(
            summary = "Get total ratings count",
            description = "Returns the total number of ratings"
    )
    public ResponseEntity<ApiResponse<Long>> getTotalRatingsCount() {
        Long count = ratingService.getTotalRatingsCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

