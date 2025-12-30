package com.tilingroofing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

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

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Set allowed origins - trim whitespace to handle configuration properly
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
        config.setAllowedOrigins(origins);
        
        // Set allowed methods - trim whitespace
        List<String> methods = Arrays.stream(allowedMethods.split(","))
                .map(String::trim)
                .toList();
        config.setAllowedMethods(methods);
        
        // Set allowed headers
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        
        // Set credentials
        config.setAllowCredentials(allowCredentials);
        
        // Set max age
        config.setMaxAge(maxAge);
        
        // Expose headers for clients
        config.addExposedHeader("Content-Disposition");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        
        return new CorsFilter(source);
    }
}

