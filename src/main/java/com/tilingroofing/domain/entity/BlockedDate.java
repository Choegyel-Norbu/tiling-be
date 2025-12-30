package com.tilingroofing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a date that is blocked for bookings.
 * Used to prevent customers from booking on holidays, fully-booked days, etc.
 * Can be linked to a specific booking when the date is blocked due to a booking.
 */
@Entity
@Table(name = "blocked_dates", indexes = {
    @Index(name = "idx_blocked_date", columnList = "date", unique = true),
    @Index(name = "idx_blocked_dates_booking_id", columnList = "booking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BlockedDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", unique = true, nullable = false)
    private LocalDate date;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", foreignKey = @ForeignKey(name = "fk_blocked_dates_booking"))
    private Booking booking;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

