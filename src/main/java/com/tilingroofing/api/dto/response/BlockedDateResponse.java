package com.tilingroofing.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing a blocked date.
 * Includes user and booking information when the blocked date is associated with a booking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockedDateResponse {

    private Long id;
    private LocalDate date;
    private String reason;
    private LocalDateTime createdAt;
    
    // User information (when blocked date is linked to a booking)
    private UserInfo user;
    
    // Booking information (when blocked date is linked to a booking)
    private BookingSummary booking;
    
    /**
     * Summary of booking information for blocked date response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BookingSummary {
        private Long id;
        private String bookingRef;
        private String status;
        private String serviceId;
        private String jobSize;
        private String suburb;
        private String postcode;
        private LocalDate preferredDate;
        private String timeSlot;
    }
}

