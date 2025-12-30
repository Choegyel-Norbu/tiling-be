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
 * Request DTO for creating a new booking.
 * Contains all fields submitted via the booking form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    @NotBlank(message = "Service ID is required")
    @Size(max = 50, message = "Service ID must not exceed 50 characters")
    private String serviceId;

    @NotBlank(message = "Job size is required")
    @ValidJobSize
    private String jobSize;

    @NotBlank(message = "Suburb is required")
    @Size(max = 100, message = "Suburb must not exceed 100 characters")
    private String suburb;

    @NotBlank(message = "Postcode is required")
    @Pattern(regexp = "^\\d{4}$", message = "Postcode must be exactly 4 digits")
    private String postcode;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Date is required")
    @FutureDate
    private String date;

    @NotBlank(message = "Time slot is required")
    @ValidTimeSlot
    private String timeSlot;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+61|0)?[2-9]\\d{8}$", message = "Invalid Australian phone number format")
    private String phone;
}

