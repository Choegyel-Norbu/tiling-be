package com.tilingroofing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a user authenticated via Google OAuth.
 * Contains user information exposed by Google's OAuth userinfo endpoint.
 * 
 * Fields correspond to Google OAuth 2.0 userinfo response:
 * - email: User's email address
 * - name: User's full name
 * - picture: URL to user's profile picture
 * - locale: User's locale preference
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's email address from Google account.
     */
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    /**
     * User's full name (display name).
     */
    @Column(name = "name", length = 200)
    private String name;

    /**
     * URL to user's profile picture from Google.
     */
    @Column(name = "picture", columnDefinition = "TEXT")
    private String picture;

    /**
     * User's locale preference (e.g., "en", "en-US").
     */
    @Column(name = "locale", length = 10)
    private String locale;

    /**
     * User's role in the system.
     * Every user must have exactly one role (USER or ADMIN).
     * Default role is USER, assigned automatically during user creation.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to get user's display name.
     * Returns the full name if available, otherwise falls back to email.
     */
    public String getDisplayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return email != null ? email : "User";
    }
}

