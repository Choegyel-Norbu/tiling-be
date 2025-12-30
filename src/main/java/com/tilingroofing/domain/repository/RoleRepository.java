package com.tilingroofing.domain.repository;

import com.tilingroofing.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entities.
 * Provides data access operations for user roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its name.
     * 
     * @param name Role name (e.g., "USER", "ADMIN")
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Checks if a role exists with the given name.
     * 
     * @param name Role name
     * @return true if role exists, false otherwise
     */
    boolean existsByName(String name);
}

