package com.tilingroofing.domain.repository;

import com.tilingroofing.domain.entity.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BlockedDate entities.
 * Provides data access operations for blocked dates.
 */
@Repository
public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {

    /**
     * Checks if a specific date is blocked.
     */
    boolean existsByDate(LocalDate date);

    /**
     * Finds a blocked date by date.
     */
    Optional<BlockedDate> findByDate(LocalDate date);

    /**
     * Finds all blocked dates within a date range.
     * Fetches booking and user information eagerly.
     */
    @Query("""
        SELECT DISTINCT bd FROM BlockedDate bd
        LEFT JOIN FETCH bd.booking booking
        LEFT JOIN FETCH booking.user user
        LEFT JOIN FETCH user.role
        WHERE bd.date BETWEEN :startDate AND :endDate
        ORDER BY bd.date
    """)
    List<BlockedDate> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Finds all blocked dates from today onwards.
     * Fetches booking and user information eagerly.
     */
    @Query("""
        SELECT DISTINCT bd FROM BlockedDate bd
        LEFT JOIN FETCH bd.booking booking
        LEFT JOIN FETCH booking.user user
        LEFT JOIN FETCH user.role
        WHERE bd.date >= :today
        ORDER BY bd.date
    """)
    List<BlockedDate> findUpcomingBlockedDates(@Param("today") LocalDate today);

    /**
     * Deletes a blocked date by date.
     */
    void deleteByDate(LocalDate date);

    /**
     * Checks if any of the given dates are blocked.
     */
    @Query("SELECT b.date FROM BlockedDate b WHERE b.date IN :dates")
    List<LocalDate> findBlockedDatesIn(@Param("dates") List<LocalDate> dates);

    /**
     * Finds a blocked date by booking ID.
     * Used to find the blocked date associated with a specific booking.
     */
    Optional<BlockedDate> findByBookingId(Long bookingId);
}

