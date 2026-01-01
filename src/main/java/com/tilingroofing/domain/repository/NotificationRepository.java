package com.tilingroofing.domain.repository;

import com.tilingroofing.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Notification entities.
 * Provides data access operations for notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds a notification by booking ID.
     */
    Optional<Notification> findByBookingId(Long bookingId);

    /**
     * Finds all notifications ordered by creation date descending (newest first).
     */
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Finds all unread notifications ordered by creation date descending.
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadNotificationsOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Counts unread notifications.
     */
    long countByIsReadFalse();
}

