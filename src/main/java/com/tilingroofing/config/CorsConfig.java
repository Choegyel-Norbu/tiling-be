package com.tilingroofing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * CORS configuration for the API.
 * Allows cross-origin requests from configured origins.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age}")
    private long maxAge;

    // Getters for SecurityConfig
    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    // NOTE: CORS is handled by SecurityConfig.corsConfigurationSource()
    // This class provides configuration values to SecurityConfig via getters
    // We don't need a separate CorsFilter bean as it would conflict with Spring Security's CORS handling
}

