package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.CreateRatingRequest;
import com.tilingroofing.api.dto.request.UpdateRatingRequest;
import com.tilingroofing.api.dto.response.RatingResponse;

/**
 * Service interface for managing ratings and reviews for bookings.
 * Defines the contract for rating operations.
 */
public interface RatingService {

    /**
     * Creates a rating for a booking.
     * 
     * @param bookingId The ID of the booking to rate
     * @param request The rating request with rating and optional comment
     * @param userId The ID of the user creating the rating
     * @return RatingResponse with the created rating
     */
    RatingResponse createRating(Long bookingId, CreateRatingRequest request, Long userId);

    /**
     * Retrieves a rating by booking ID.
     * 
     * @param bookingId The booking ID
     * @return RatingResponse if found
     */
    RatingResponse getRatingByBookingId(Long bookingId);

    /**
     * Retrieves a rating by its ID.
     * 
     * @param id The rating ID
     * @return RatingResponse if found
     */
    RatingResponse getRatingById(Long id);

    /**
     * Updates an existing rating.
     * 
     * @param id The rating ID
     * @param request The update request with optional fields
     * @return RatingResponse with the updated rating
     */
    RatingResponse updateRating(Long id, UpdateRatingRequest request);

    /**
     * Deletes a rating.
     * 
     * @param id The rating ID
     */
    void deleteRating(Long id);

    /**
     * Calculates the average rating across all ratings.
     * 
     * @return Average rating (null if no ratings exist)
     */
    Double getAverageRating();

    /**
     * Gets the total count of ratings.
     * 
     * @return Total number of ratings
     */
    Long getTotalRatingsCount();
}
