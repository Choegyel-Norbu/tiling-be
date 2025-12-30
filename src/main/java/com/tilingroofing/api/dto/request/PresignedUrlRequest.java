package com.tilingroofing.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating a presigned URL for client-side uploads.
 * Clients use this to upload files directly to UploadThing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRequest {

    /**
     * Original filename.
     */
    @NotBlank(message = "Filename is required")
    private String filename;

    /**
     * File size in bytes.
     */
    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    private Long fileSize;

    /**
     * MIME type of the file.
     */
    @NotBlank(message = "Content type is required")
    private String contentType;

    /**
     * Optional custom ID for tracking.
     */
    private String customId;
}

