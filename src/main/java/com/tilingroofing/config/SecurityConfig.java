package com.tilingroofing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the application.
 * Configures JWT-based authentication and authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfig corsConfig;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfig corsConfig
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfig = corsConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Stateless session management (JWT)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configure request authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/google-signin",
                                "/api/auth/health",
                                "/api/v1/uploadthing/health",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        
                        // UploadThing endpoints require authentication
                        .requestMatchers("/api/v1/uploadthing/**").authenticated()
                        
                        // Protected endpoints - require authentication
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/bookings/**"
                        ).authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Creates CORS configuration source for Spring Security.
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Get allowed origins from CorsConfig
        String allowedOrigins = corsConfig.getAllowedOrigins();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
        config.setAllowedOrigins(origins);
        
        // Set allowed methods
        String allowedMethods = corsConfig.getAllowedMethods();
        List<String> methods = Arrays.stream(allowedMethods.split(","))
                .map(String::trim)
                .toList();
        config.setAllowedMethods(methods);
        
        // Set allowed headers
        config.addAllowedHeader("*");
        
        // Set credentials
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        
        // Set max age
        config.setMaxAge(corsConfig.getMaxAge());
        
        // Expose headers
        config.addExposedHeader("Content-Disposition");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        
        return source;
    }
}

