package com.tilingroofing.api.controller;

import com.tilingroofing.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for file operations.
 * Provides endpoints for downloading uploaded files.
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File download endpoints")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Downloads a file by its ID.
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "Download file", description = "Downloads a file by its ID")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File ID")
            @PathVariable Long fileId
    ) {
        Resource resource = fileStorageService.loadFileAsResource(fileId);

        // Try to determine content type
        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

