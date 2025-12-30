package com.tilingroofing.api.controller;

import com.tilingroofing.api.dto.request.GoogleSignInRequest;
import com.tilingroofing.api.dto.response.ApiResponse;
import com.tilingroofing.api.dto.response.AuthResponse;
import com.tilingroofing.api.dto.response.UserInfo;
import com.tilingroofing.api.mapper.UserMapper;
import com.tilingroofing.common.exception.BusinessException;
import com.tilingroofing.domain.entity.User;
import com.tilingroofing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Handles Google Sign-In and user authentication.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(
            AuthService authService,
            UserMapper userMapper
    ) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    /**
     * Authenticates a user with Google ID token.
     * Verifies the token, creates/updates user, and returns JWT token.
     */
    @PostMapping("/google-signin")
    @Operation(summary = "Sign in with Google", description = "Authenticates user with Google ID token and returns JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> googleSignIn(
            @Valid @RequestBody GoogleSignInRequest request
    ) {
        try {
            AuthService.AuthenticationResult result = authService.authenticateWithGoogle(request.getIdToken());

            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(result.getUser().getId().toString())
                    .email(result.getUser().getEmail())
                    .name(result.getUser().getName())
                    .picture(result.getUser().getPicture())
                    .role(result.getUser().getRole() != null ? result.getUser().getRole().getName() : null)
                    .build();

            AuthResponse authResponse = AuthResponse.builder()
                    .token(result.getToken())
                    .user(userInfo)
                    .build();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(authResponse, "Authentication successful"));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("AUTHENTICATION_FAILED", e.getMessage());
        }
    }

    /**
     * Gets the current authenticated user information.
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Returns information about the currently authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("UNAUTHORIZED", "User is not authenticated");
        }

        // Extract user ID from authentication principal (set by JWT filter)
        String userIdString = authentication.getName();
        Long userId = Long.parseLong(userIdString);

        User user = authService.getUserById(userId);
        UserInfo userInfo = userMapper.toUserInfo(user);

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}

