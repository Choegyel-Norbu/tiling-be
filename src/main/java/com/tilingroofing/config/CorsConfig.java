package com.tilingroofing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the API.
 * Allows cross-origin requests from configured origins.
 * Configuration is defined directly in this class with environment variable override support.
 */
@Configuration
public class CorsConfig {

    // Default allowed origins - can be overridden via CORS_ORIGINS environment variable
    private static final String DEFAULT_ALLOWED_ORIGINS = 
            "http://localhost:3000,http://localhost:5173,https://tiling-fe.vercel.app";
    
    private static final String DEFAULT_ALLOWED_METHODS = 
            "GET,POST,PUT,PATCH,DELETE,OPTIONS";
    
    private static final long DEFAULT_MAX_AGE = 3600L; // 1 hour
    
    private final Environment environment;

    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Creates and returns CORS configuration.
     * Uses environment variable CORS_ORIGINS if set, otherwise uses default origins.
     */
    public CorsConfiguration getCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Get allowed origins from environment variable or use defaults
        String allowedOriginsEnv = environment.getProperty("CORS_ORIGINS");
        String allowedOrigins = allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty() 
                ? allowedOriginsEnv 
                : DEFAULT_ALLOWED_ORIGINS;
        
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);
        
        // Set allowed methods
        List<String> methods = Arrays.stream(DEFAULT_ALLOWED_METHODS.split(","))
                .map(String::trim)
                .toList();
        config.setAllowedMethods(methods);
        
        // Set allowed headers - allow all headers
        config.addAllowedHeader("*");
        
        // Set credentials - required for cookies/auth headers
        config.setAllowCredentials(true);
        
        // Set max age for preflight cache
        config.setMaxAge(DEFAULT_MAX_AGE);
        
        // Expose headers that clients might need
        config.addExposedHeader("Content-Disposition");
        config.addExposedHeader("Authorization");
        
        return config;
    }
}
