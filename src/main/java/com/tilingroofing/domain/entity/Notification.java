package com.tilingroofing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a notification for a booking.
 * One-to-one relationship with Booking - each booking has one notification.
 * Minimal fields: message, read status, and timestamps.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_booking_id", columnList = "booking_id", unique = true),
    @Index(name = "idx_notification_read", columnList = "is_read"),
    @Index(name = "idx_notification_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with Booking.
     * Each booking has exactly one notification.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_notifications_booking"))
    private Booking booking;

    /**
     * Notification message/title.
     * Contains relevant information about the booking.
     */
    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /**
     * Read status flag.
     * Indicates whether the notification has been read.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

