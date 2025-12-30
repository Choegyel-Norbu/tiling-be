package com.tilingroofing.domain.repository;

import com.tilingroofing.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entities.
 * Provides data access operations for users authenticated via Google OAuth.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Useful for account linking or email-based lookups.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     */
    boolean existsByEmail(String email);
}

