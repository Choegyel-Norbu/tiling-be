package com.tilingroofing.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from UploadThing API.
 * Represents the response after uploading a file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadThingResponse {

    /**
     * The file key used to access the file.
     */
    @JsonProperty("key")
    private String key;

    /**
     * The URL to access the uploaded file.
     */
    @JsonProperty("url")
    private String url;

    /**
     * The file name.
     */
    @JsonProperty("name")
    private String name;

    /**
     * The file size in bytes.
     */
    @JsonProperty("size")
    private Long size;

    /**
     * The MIME type of the file.
     */
    @JsonProperty("type")
    private String type;

    /**
     * Upload status.
     */
    @JsonProperty("status")
    private String status;
}

