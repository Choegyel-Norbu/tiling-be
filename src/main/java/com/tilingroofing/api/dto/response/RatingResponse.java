package com.tilingroofing.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a rating/review.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RatingResponse {

    private Long id;
    private Long bookingId;
    private String bookingRef;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

