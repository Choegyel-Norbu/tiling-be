package com.tilingroofing.service;

import com.tilingroofing.api.dto.response.UploadThingDeleteResponse;
import com.tilingroofing.api.dto.response.UploadThingUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for UploadThing file operations using Node.js scripts.
 * Defines the contract for UploadThing operations.
 */
public interface UploadThingScriptService {

    /**
     * Uploads a file using Node.js script.
     *
     * @param file The multipart file to upload
     * @param field Field identifier (e.g., "photos", "license")
     * @param fileType File type ("image" or "pdf")
     * @return UploadThingUploadResponse with upload details
     */
    UploadThingUploadResponse uploadFile(MultipartFile file, String field, String fileType);

    /**
     * Deletes files using Node.js script.
     *
     * @param fileKeys List of file keys to delete
     * @return UploadThingDeleteResponse with deletion results
     */
    UploadThingDeleteResponse deleteFiles(List<String> fileKeys);
}
