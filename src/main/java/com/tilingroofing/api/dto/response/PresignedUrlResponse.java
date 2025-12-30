package com.tilingroofing.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing presigned URL for client-side uploads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponse {

    /**
     * The presigned URL for uploading the file.
     */
    private String uploadUrl;

    /**
     * The file key that will be used after upload.
     */
    private String fileKey;

    /**
     * The URL where the file will be accessible after upload.
     */
    private String fileUrl;
}

