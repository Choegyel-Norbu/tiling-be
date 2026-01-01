package com.tilingroofing.domain.entity;

import com.tilingroofing.domain.enums.BookingStatus;
import com.tilingroofing.domain.enums.JobSize;
import com.tilingroofing.domain.enums.TimeSlot;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a service booking.
 * Contains customer information, service details, and scheduling preferences.
 */
@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_ref", columnList = "booking_ref"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_preferred_date", columnList = "preferred_date"),
    @Index(name = "idx_booking_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_ref", unique = true, nullable = false, length = 20)
    private String bookingRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "service_id", nullable = false, length = 50)
    private String serviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_size", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private JobSize jobSize;

    @Column(name = "suburb", nullable = false, length = 100)
    private String suburb;

    @Column(name = "postcode", nullable = false, length = 4)
    private String postcode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "preferred_date", nullable = false)
    private LocalDate preferredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private TimeSlot timeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingFile> files = new ArrayList<>();

    /**
     * One-to-one relationship with Rating.
     * Each booking can have at most one rating.
     */
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Rating rating;

    /**
     * One-to-one relationship with Notification.
     * Each booking has exactly one notification.
     */
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Notification notification;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to add a file to this booking.
     * Maintains bidirectional relationship.
     */
    public void addFile(BookingFile file) {
        files.add(file);
        file.setBooking(this);
    }

    /**
     * Helper method to remove a file from this booking.
     * Maintains bidirectional relationship.
     */
    public void removeFile(BookingFile file) {
        files.remove(file);
        file.setBooking(null);
    }
}

