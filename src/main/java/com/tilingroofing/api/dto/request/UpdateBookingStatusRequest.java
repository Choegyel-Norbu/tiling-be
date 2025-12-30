package com.tilingroofing.api.dto.request;

import com.tilingroofing.common.validation.ValidBookingStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a booking's status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingStatusRequest {

    @NotBlank(message = "Status is required")
    @ValidBookingStatus
    private String status;
}

