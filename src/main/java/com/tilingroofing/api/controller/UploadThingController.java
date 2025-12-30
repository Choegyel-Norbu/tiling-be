package com.tilingroofing.api.controller;

import com.tilingroofing.api.dto.response.ApiResponse;
import com.tilingroofing.api.dto.response.UploadThingDeleteResponse;
import com.tilingroofing.api.dto.response.UploadThingUploadResponse;
import com.tilingroofing.service.UploadThingScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for UploadThing file operations.
 * Provides endpoints for file uploads and deletions using UploadThing.
 */
@RestController
@RequestMapping("/api/v1/uploadthing")
@Tag(name = "UploadThing", description = "File upload and management endpoints using UploadThing")
public class UploadThingController {

    private final UploadThingScriptService uploadThingService;

    public UploadThingController(UploadThingScriptService uploadThingService) {
        this.uploadThingService = uploadThingService;
    }

    /**
     * Uploads a file to UploadThing.
     *
     * @param file     The file to upload
     * @param field    Field identifier (e.g., "photos", "license")
     * @param fileType File type ("image" or "pdf")
     * @return UploadThingUploadResponse with upload details
     */
    @PostMapping("/upload")
    @Operation(
            summary = "Upload file to UploadThing",
            description = "Uploads a file to UploadThing using the official SDK via Node.js script",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UploadThingUploadResponse>> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") @NotNull MultipartFile file,
            @Parameter(description = "Field identifier (e.g., 'photos', 'license')", required = true)
            @RequestParam("field") @NotBlank String field,
            @Parameter(description = "File type: 'image' or 'pdf'", required = true)
            @RequestParam("fileType") @NotBlank String fileType,
            Authentication authentication
    ) {
        UploadThingUploadResponse response = uploadThingService.uploadFile(file, field, fileType);
        return ResponseEntity.ok(ApiResponse.success(response, "File uploaded successfully"));
    }

    /**
     * Deletes multiple files from UploadThing.
     *
     * @param fileKeys List of file keys to delete
     * @return UploadThingDeleteResponse with deletion results
     */
    @DeleteMapping("/files")
    @Operation(
            summary = "Delete files from UploadThing",
            description = "Deletes multiple files from UploadThing by their file keys",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UploadThingDeleteResponse>> deleteFiles(
            @Parameter(description = "List of file keys to delete", required = true)
            @RequestBody @NotNull List<String> fileKeys,
            Authentication authentication
    ) {
        UploadThingDeleteResponse response = uploadThingService.deleteFiles(fileKeys);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Deletes a single file from UploadThing.
     *
     * @param fileKey The file key to delete
     * @return UploadThingDeleteResponse with deletion results
     */
    @DeleteMapping("/files/{fileKey}")
    @Operation(
            summary = "Delete single file from UploadThing",
            description = "Deletes a single file from UploadThing by its file key",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UploadThingDeleteResponse>> deleteFile(
            @Parameter(description = "File key to delete", required = true)
            @PathVariable @NotBlank String fileKey,
            Authentication authentication
    ) {
        UploadThingDeleteResponse response = uploadThingService.deleteFiles(List.of(fileKey));
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Health check endpoint for UploadThing service.
     *
     * @return Health status message
     */
    @GetMapping("/health")
    @Operation(summary = "UploadThing health check", description = "Checks if UploadThing service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UploadThing service is healthy");
    }
}
