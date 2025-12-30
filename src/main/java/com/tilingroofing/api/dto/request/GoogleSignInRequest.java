package com.tilingroofing.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Google Sign-In.
 * Contains the Google ID token from the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleSignInRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;
}

