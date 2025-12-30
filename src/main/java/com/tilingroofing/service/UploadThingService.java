package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.PresignedUrlRequest;
import com.tilingroofing.api.dto.response.PresignedUrlResponse;
import com.tilingroofing.api.dto.response.UploadThingResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for interacting with UploadThing via Node.js service.
 * Defines the contract for UploadThing operations.
 */
public interface UploadThingService {

    /**
     * Uploads a file to UploadThing via Node.js service.
     * 
     * @param file The multipart file to upload
     * @param customId Optional custom identifier for tracking
     * @return UploadThingResponse with file URL and metadata
     */
    UploadThingResponse uploadFile(MultipartFile file, String customId);

    /**
     * Generates a presigned URL for client-side direct uploads.
     * 
     * @param request Presigned URL request with file metadata
     * @return PresignedUrlResponse with upload URL and file key
     */
    PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request);

    /**
     * Deletes a file from UploadThing storage via Node.js service.
     * 
     * @param fileKey The file key to delete
     */
    void deleteFile(String fileKey);

    /**
     * Checks if UploadThing is enabled and configured.
     * 
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();
}
