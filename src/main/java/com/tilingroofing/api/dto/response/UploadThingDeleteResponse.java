package com.tilingroofing.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for UploadThing file deletion operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadThingDeleteResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("deletedFiles")
    private List<String> deletedFiles;

    @JsonProperty("failedFiles")
    private List<String> failedFiles;

    @JsonProperty("error")
    private String error;
}

