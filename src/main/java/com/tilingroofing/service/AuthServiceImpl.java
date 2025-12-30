package com.tilingroofing.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.tilingroofing.config.JwtTokenProvider;
import com.tilingroofing.domain.entity.Role;
import com.tilingroofing.domain.entity.User;
import com.tilingroofing.domain.repository.RoleRepository;
import com.tilingroofing.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService.
 * Handles authentication operations including Google Sign-In and JWT token generation.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            GoogleTokenVerifier googleTokenVerifier,
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthenticationResult authenticateWithGoogle(String idTokenString) {
        // Verify Google token
        GoogleIdToken googleIdToken = googleTokenVerifier.verifyToken(idTokenString);
        if (googleIdToken == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        // Extract user info from token
        GoogleTokenVerifier.UserInfo googleUserInfo = googleTokenVerifier.extractUserInfo(googleIdToken);
        if (googleUserInfo == null || googleUserInfo.getEmail() == null) {
            throw new IllegalArgumentException("Unable to extract user information from Google token");
        }

        // Find or create user
        User user = findOrCreateUser(googleUserInfo);

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getName() != null ? user.getName() : user.getEmail()
        );

        log.info("User authenticated successfully: {}", user.getEmail());

        return AuthenticationResult.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }

    /**
     * Finds an existing user by email or creates a new one.
     * Updates user info if it has changed.
     */
    private User findOrCreateUser(GoogleTokenVerifier.UserInfo googleUserInfo) {
        return userRepository.findByEmail(googleUserInfo.getEmail())
                .map(existingUser -> {
                    // Update user info if changed
                    boolean updated = false;
                    if (hasChanged(existingUser.getName(), googleUserInfo.getName())) {
                        existingUser.setName(googleUserInfo.getName());
                        updated = true;
                    }
                    if (hasChanged(existingUser.getPicture(), googleUserInfo.getPicture())) {
                        existingUser.setPicture(googleUserInfo.getPicture());
                        updated = true;
                    }
                    if (hasChanged(existingUser.getLocale(), googleUserInfo.getLocale())) {
                        existingUser.setLocale(googleUserInfo.getLocale());
                        updated = true;
                    }
                    if (updated) {
                        log.debug("Updated user info for: {}", existingUser.getEmail());
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    // Get USER role (default role for all new users)
                    Role userRole = roleRepository.findByName("USER")
                            .orElseThrow(() -> new IllegalStateException("USER role not found in database. Please ensure roles are initialized."));
                    
                    // Create new user with USER role
                    User newUser = User.builder()
                            .email(googleUserInfo.getEmail())
                            .name(googleUserInfo.getName())
                            .picture(googleUserInfo.getPicture())
                            .locale(googleUserInfo.getLocale())
                            .role(userRole)
                            .build();
                    log.info("Created new user: {} with role: {}", newUser.getEmail(), userRole.getName());
                    return userRepository.save(newUser);
                });
    }

    /**
     * Checks if a value has changed (null-safe comparison).
     */
    private boolean hasChanged(String oldValue, String newValue) {
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }
        return !oldValue.equals(newValue);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}

