package com.tilingroofing.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing an uploaded file.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {

    private Long id;
    private String filename;
    private String originalFilename;
    private String url;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadedAt;
}

