package com.tilingroofing.domain.repository;

import com.tilingroofing.domain.entity.Booking;
import com.tilingroofing.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for Booking entities.
 * Provides data access operations for bookings.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds a booking by its reference number.
     */
    Optional<Booking> findByBookingRef(String bookingRef);

    /**
     * Checks if a booking exists with the given reference.
     */
    boolean existsByBookingRef(String bookingRef);

    /**
     * Finds all bookings with optional status filter.
     */
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    /**
     * Finds all bookings ordered by creation date descending.
     */
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Finds bookings by status ordered by creation date descending.
     */
    Page<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);

    /**
     * Finds bookings by user email.
     */
    @Query("SELECT b FROM Booking b WHERE b.user.email = :email ORDER BY b.createdAt DESC")
    Page<Booking> findByUserEmailOrderByCreatedAtDesc(@Param("email") String email, Pageable pageable);

    /**
     * Finds bookings for a specific date.
     */
    @Query("SELECT b FROM Booking b WHERE b.preferredDate = :date ORDER BY b.createdAt DESC")
    Page<Booking> findByPreferredDate(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Counts bookings by status.
     */
    long countByStatus(BookingStatus status);

    /**
     * Search bookings by various criteria.
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE (:status IS NULL OR b.status = :status)
        AND (:search IS NULL OR 
             LOWER(b.bookingRef) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(b.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(b.user.email) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY b.createdAt DESC
    """)
    Page<Booking> searchBookings(
            @Param("status") BookingStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Finds all bookings for a specific user, optionally filtered by status.
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.user.id = :userId
        AND (:status IS NULL OR b.status = :status)
        ORDER BY b.createdAt DESC
    """)
    Page<Booking> findByUserId(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status,
            Pageable pageable
    );
}

