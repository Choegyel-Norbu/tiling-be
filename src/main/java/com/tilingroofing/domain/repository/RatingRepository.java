package com.tilingroofing.domain.repository;

import com.tilingroofing.domain.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Rating entities.
 * Provides data access operations for ratings and reviews.
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Finds a rating by booking ID.
     * Since it's a one-to-one relationship, this should return at most one rating.
     */
    Optional<Rating> findByBookingId(Long bookingId);

    /**
     * Checks if a rating exists for a booking.
     */
    boolean existsByBookingId(Long bookingId);


    /**
     * Finds all ratings with a specific rating or higher.
     */
    List<Rating> findByRatingGreaterThanEqual(Integer minRating);

    /**
     * Calculates the average rating across all ratings.
     */
    @Query("SELECT AVG(r.rating) FROM Rating r")
    Double calculateAverageRating();

    /**
     * Counts the total number of ratings.
     */
    @Query("SELECT COUNT(r) FROM Rating r")
    Long countAllRatings();

    /**
     * Finds ratings for bookings within a date range.
     */
    @Query("SELECT r FROM Rating r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<Rating> findRatingsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                        @Param("endDate") java.time.LocalDateTime endDate);
}

