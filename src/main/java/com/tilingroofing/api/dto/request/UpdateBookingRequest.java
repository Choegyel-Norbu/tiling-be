package com.tilingroofing.api.dto.request;

import com.tilingroofing.common.validation.FutureDate;
import com.tilingroofing.common.validation.ValidJobSize;
import com.tilingroofing.common.validation.ValidTimeSlot;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a booking.
 * All fields are optional to support partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequest {

    @Size(max = 50, message = "Service ID must not exceed 50 characters")
    private String serviceId;

    @ValidJobSize
    private String jobSize;

    @Size(max = 100, message = "Suburb must not exceed 100 characters")
    private String suburb;

    @Pattern(regexp = "^\\d{4}$", message = "Postcode must be exactly 4 digits")
    private String postcode;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @FutureDate
    private String date;

    @ValidTimeSlot
    private String timeSlot;

    @Pattern(regexp = "^(\\+61|0)?[2-9]\\d{8}$", message = "Invalid Australian phone number format")
    private String phone;
}

