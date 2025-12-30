package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.PresignedUrlRequest;
import com.tilingroofing.api.dto.response.PresignedUrlResponse;
import com.tilingroofing.api.dto.response.UploadThingResponse;
import com.tilingroofing.common.exception.FileStorageException;
import com.tilingroofing.config.UploadThingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Implementation of UploadThingService.
 * Communicates with a Node.js microservice that wraps UploadThing's SDK.
 */
@Service
public class UploadThingServiceImpl implements UploadThingService {

    private static final Logger log = LoggerFactory.getLogger(UploadThingServiceImpl.class);

    private final UploadThingProperties properties;
    private final WebClient webClient;

    public UploadThingServiceImpl(UploadThingProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        
        // Build WebClient pointing to Node.js service
        this.webClient = webClientBuilder
                .baseUrl(properties.getServiceUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public UploadThingResponse uploadFile(MultipartFile file, String customId) {
        if (!properties.isEnabled()) {
            throw new FileStorageException("UploadThing is not enabled");
        }

        validateFile(file);

        try {
            log.debug("Uploading file via Node.js service: {}", file.getOriginalFilename());

            // Prepare multipart form data for Node.js service
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource())
                    .filename(file.getOriginalFilename())
                    .contentType(MediaType.parseMediaType(file.getContentType()));
            
            if (customId != null) {
                builder.part("customId", customId);
                builder.part("bookingRef", customId); // Also send as bookingRef for Node.js service
            }

            // Call Node.js service endpoint
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = webClient.post()
                    .uri("/api/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null || !Boolean.TRUE.equals(responseMap.get("success"))) {
                throw new FileStorageException("Upload failed: No response from Node.js service");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

            UploadThingResponse response = UploadThingResponse.builder()
                    .key((String) data.get("key"))
                    .url((String) data.get("url"))
                    .name((String) data.get("name"))
                    .size(data.get("size") != null ? Long.valueOf(data.get("size").toString()) : file.getSize())
                    .type((String) data.get("type"))
                    .status("completed")
                    .build();

            log.info("Successfully uploaded file via Node.js service: {} -> {}", 
                    file.getOriginalFilename(), response.getUrl());

            return response;

        } catch (WebClientResponseException e) {
            log.error("Node.js service error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new FileStorageException(
                    "Failed to upload file via Node.js service: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading via Node.js service", e);
            throw new FileStorageException("Unexpected error during upload", e);
        }
    }

    @Override
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        if (!properties.isEnabled()) {
            throw new FileStorageException("UploadThing is not enabled");
        }

        validateFileMetadata(request.getFilename(), request.getFileSize(), request.getContentType());

        // For UploadThing, clients upload directly to the file router endpoints
        // We return the endpoint URL that clients should use
        String endpoint = determineEndpoint(request.getContentType());
        String uploadUrl = properties.getServiceUrl() + "/api/uploadthing/" + endpoint;

        PresignedUrlResponse presignedResponse = PresignedUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .fileKey(null) // Will be provided after upload
                .fileUrl(null) // Will be provided after upload
                .build();

        log.info("Generated upload endpoint URL for: {} -> {}", request.getFilename(), uploadUrl);
        return presignedResponse;
    }

    @Override
    public void deleteFile(String fileKey) {
        if (!properties.isEnabled()) {
            log.warn("UploadThing is not enabled, skipping file deletion: {}", fileKey);
            return;
        }

        try {
            log.debug("Deleting file via Node.js service: {}", fileKey);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.delete()
                    .uri("/api/files/{fileKey}", fileKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                log.info("Successfully deleted file via Node.js service: {}", fileKey);
            } else {
                log.warn("Delete response indicates failure for file: {}", fileKey);
            }

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("File not found in UploadThing: {}", fileKey);
            } else {
                log.error("Failed to delete file via Node.js service: {} - {}", 
                        fileKey, e.getResponseBodyAsString());
                throw new FileStorageException("Failed to delete file: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error deleting file via Node.js service", e);
            throw new FileStorageException("Unexpected error during file deletion", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled() && properties.getServiceUrl() != null && !properties.getServiceUrl().isBlank();
    }

    /**
     * Determines the appropriate UploadThing endpoint based on content type.
     */
    private String determineEndpoint(String contentType) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return "imageUploader";
            } else if (contentType.equals("application/pdf")) {
                return "pdfUploader";
            }
        }
        return "fileUploader"; // Default to general file uploader
    }

    /**
     * Validates file against UploadThing configuration.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }

        if (file.getSize() > properties.getMaxFileSize()) {
            throw new FileStorageException(
                    String.format("File size exceeds maximum allowed size of %d MB", 
                            properties.getMaxFileSize() / 1024 / 1024));
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new FileStorageException(
                    "File type not allowed. Allowed types: " + properties.getAllowedTypes());
        }
    }

    /**
     * Validates file metadata.
     */
    private void validateFileMetadata(String filename, Long fileSize, String contentType) {
        if (filename == null || filename.isBlank()) {
            throw new FileStorageException("Filename is required");
        }

        if (fileSize == null || fileSize <= 0) {
            throw new FileStorageException("File size must be positive");
        }

        if (fileSize > properties.getMaxFileSize()) {
            throw new FileStorageException(
                    String.format("File size exceeds maximum allowed size of %d MB", 
                            properties.getMaxFileSize() / 1024 / 1024));
        }

        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new FileStorageException(
                    "File type not allowed. Allowed types: " + properties.getAllowedTypes());
        }
    }

    /**
     * Checks if the content type is allowed.
     */
    private boolean isAllowedContentType(String contentType) {
        String[] allowed = properties.getAllowedTypes().split(",");
        for (String allowedType : allowed) {
            String trimmed = allowedType.trim();
            if (trimmed.endsWith("/*")) {
                String prefix = trimmed.replace("/*", "/");
                if (contentType.startsWith(prefix)) {
                    return true;
                }
            } else if (trimmed.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}

