package com.tilingroofing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a user role.
 * Defines the access level and permissions for users in the system.
 * 
 * Roles:
 * - USER: Standard user with basic access
 * - ADMIN: Administrator with full system access
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role name (e.g., "USER", "ADMIN").
     * Must be unique and not null.
     */
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    /**
     * Human-readable description of the role.
     */
    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

