package com.tilingroofing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a rating/review for a completed booking.
 * One-to-one relationship with Booking - each booking can have one rating.
 * Simple rating system with a score out of 10 and an optional comment.
 */
@Entity
@Table(name = "ratings", indexes = {
    @Index(name = "idx_rating_booking_id", columnList = "booking_id", unique = true),
    @Index(name = "idx_rating_score", columnList = "rating")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with Booking.
     * Each booking can have at most one rating.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true, 
                foreignKey = @ForeignKey(name = "fk_ratings_booking"))
    private Booking booking;

    /**
     * Rating score out of 10.
     * Required field - represents the overall satisfaction (1-10 scale).
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * Optional comment/review text.
     * Allows customers to provide detailed feedback.
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

