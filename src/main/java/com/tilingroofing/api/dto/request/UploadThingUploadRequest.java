package com.tilingroofing.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for UploadThing file upload.
 * Used to upload files to UploadThing's storage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadThingUploadRequest {

    /**
     * The file content as base64 encoded string or file data.
     * In practice, we'll send multipart form data.
     */
    @JsonProperty("file")
    private String file;

    /**
     * Original filename.
     */
    @JsonProperty("name")
    private String name;

    /**
     * File size in bytes.
     */
    @JsonProperty("size")
    private Long size;

    /**
     * MIME type of the file.
     */
    @JsonProperty("type")
    private String type;

    /**
     * Optional custom ID for the file.
     */
    @JsonProperty("customId")
    private String customId;
}

