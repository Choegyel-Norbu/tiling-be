package com.tilingroofing.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for blocking dates from booking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockDatesRequest {

    @NotEmpty(message = "At least one date is required")
    @Size(max = 365, message = "Cannot block more than 365 dates at once")
    private List<String> dates;

    private String reason;
}

