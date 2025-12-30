package com.tilingroofing.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for UploadThing file upload operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadThingUploadResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("url")
    private String url;           // UploadThing CDN URL

    @JsonProperty("fileKey")
    private String fileKey;       // File key for deletion

    @JsonProperty("field")
    private String field;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("fileType")
    private String fileType;

    @JsonProperty("error")
    private String error;
}

