package com.tilingroofing.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing a booking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private Long id;
    private String bookingRef;
    private String status;
    private String serviceId;
    private String jobSize;
    private String suburb;
    private String postcode;
    private String description;
    private LocalDate preferredDate;
    private String timeSlot;
    private UserInfo user;
    private String customerPhone;
    private List<FileResponse> files;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

