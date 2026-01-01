package com.tilingroofing.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing a notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private Long id;
    private Long bookingId;
    private String bookingRef;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    /**
     * Essential booking information.
     */
    private BookingInfo booking;
    
    /**
     * Essential user information.
     */
    private UserInfo user;

    /**
     * Nested DTO for essential booking information.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BookingInfo {
        private String bookingRef;
        private String status;
        private String serviceId;
        private String suburb;
        private String postcode;
        private LocalDate preferredDate;
        private String timeSlot;
    }
}

