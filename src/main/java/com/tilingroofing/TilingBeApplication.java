package com.tilingroofing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application entry point for Tiling Roofing Booking System.
 * 
 * This Spring Boot application provides a RESTful API for managing
 * service bookings, file uploads, and administrative operations.
 */
@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
public class TilingBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TilingBeApplication.class, args);
    }
}

