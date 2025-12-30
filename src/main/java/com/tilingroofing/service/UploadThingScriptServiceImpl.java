package com.tilingroofing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilingroofing.api.dto.response.UploadThingDeleteResponse;
import com.tilingroofing.api.dto.response.UploadThingUploadResponse;
import com.tilingroofing.common.exception.FileStorageException;
import com.tilingroofing.config.UploadThingConfig;
import com.tilingroofing.domain.repository.BookingFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of UploadThingScriptService.
 * Executes Node.js scripts that use the official UploadThing SDK.
 */
@Service
public class UploadThingScriptServiceImpl implements UploadThingScriptService {

    private static final Logger log = LoggerFactory.getLogger(UploadThingScriptServiceImpl.class);

    private final UploadThingConfig config;
    private final ObjectMapper objectMapper;
    private final BookingFileRepository bookingFileRepository;

    @Value("${uploadthing.script.upload:uploadthing-upload.js}")
    private String uploadScriptPath;

    @Value("${uploadthing.script.delete:uploadthing-delete.js}")
    private String deleteScriptPath;

    @Value("${uploadthing.script.timeout:60}")
    private int scriptTimeoutSeconds;

    public UploadThingScriptServiceImpl(
            UploadThingConfig config, 
            ObjectMapper objectMapper,
            BookingFileRepository bookingFileRepository
    ) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.bookingFileRepository = bookingFileRepository;
    }

    @Override
    public UploadThingUploadResponse uploadFile(MultipartFile file, String field, String fileType) {
        Path tempFile = null;
        try {
            // Validate inputs
            if (file == null || file.isEmpty()) {
                throw new FileStorageException("File is required and cannot be empty");
            }

            // Determine file type if not provided
            if (fileType == null || fileType.isBlank()) {
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    fileType = "image";
                } else if (contentType != null && contentType.equals("application/pdf")) {
                    fileType = "pdf";
                } else {
                    throw new FileStorageException("Unsupported file type. Only images and PDFs are supported");
                }
            }

            // Create temporary file
            tempFile = Files.createTempFile("uploadthing-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            // Build script path (use absolute path from project root)
            String scriptPath = new File(uploadScriptPath).getAbsolutePath();
            if (!new File(scriptPath).exists()) {
                // Try relative to current directory
                scriptPath = uploadScriptPath;
            }

            // Execute Node.js script
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    scriptPath,
                    tempFile.toString(),
                    field,
                    fileType,
                    file.getOriginalFilename()
            );

            // Set environment variables
            // UPLOADTHING_SECRET is the preferred env var name for the SDK
            processBuilder.environment().put("UPLOADTHING_SECRET", config.getApiSecret());
            processBuilder.environment().put("UPLOADTHING_API_SECRET", config.getApiSecret());
            
            // Add app ID if available
            String appId = config.getAppId();
            if (appId != null && !appId.isBlank()) {
                processBuilder.environment().put("UPLOADTHING_APP_ID", appId);
            }

            // Redirect stderr to capture logs
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            // Read stdout (JSON response)
            StringBuilder stdout = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line);
                }
            }

            // Read stderr (logs) - need to read this in parallel to avoid blocking
            StringBuilder stderr = new StringBuilder();
            Thread stderrReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.append(line).append("\n");
                        // Log debug info from stderr
                        if (line.contains("UploadThing response:")) {
                            log.debug("UploadThing SDK response: {}", line);
                        }
                    }
                } catch (IOException e) {
                    log.warn("Error reading stderr", e);
                }
            });
            stderrReader.start();

            // Wait for process with timeout
            boolean finished = process.waitFor(scriptTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                stderrReader.join(1000);
                throw new FileStorageException("Upload script timed out after " + scriptTimeoutSeconds + " seconds");
            }

            // Wait for stderr reader to finish
            stderrReader.join(1000);

            int exitCode = process.exitValue();
            String stderrOutput = stderr.toString();
            
            // Parse JSON response
            String jsonResponse = stdout.toString();
            if (jsonResponse.isBlank()) {
                log.error("Empty stdout response. Exit code: {}. Stderr: {}", exitCode, stderrOutput);
                throw new FileStorageException("Empty response from upload script. Check logs for details.");
            }

            log.debug("UploadThing script stdout: {}", jsonResponse);
            if (!stderrOutput.isBlank()) {
                log.debug("UploadThing script stderr: {}", stderrOutput);
            }

            if (exitCode != 0) {
                log.error("Upload script failed with exit code {}: {}", exitCode, stderrOutput);
                throw new FileStorageException("Upload failed: " + stderrOutput);
            }

            UploadThingUploadResponse response;
            try {
                response = objectMapper.readValue(jsonResponse, UploadThingUploadResponse.class);
            } catch (Exception e) {
                log.error("Failed to parse UploadThing response JSON: {}", jsonResponse, e);
                throw new FileStorageException("Failed to parse upload response: " + e.getMessage());
            }

            if (!response.isSuccess()) {
                throw new FileStorageException(
                        response.getError() != null ? response.getError() : "Upload failed");
            }

            // Validate response has required fields
            if (response.getFileKey() == null || response.getFileKey().isBlank()) {
                log.error("UploadThing response missing fileKey. Response: {}", jsonResponse);
                throw new FileStorageException("Upload response missing fileKey field");
            }

            // If URL is missing, construct it from the file key
            if (response.getUrl() == null || response.getUrl().isBlank()) {
                log.warn("UploadThing response missing URL, constructing from fileKey. Response: {}", jsonResponse);
                // Construct URL: https://utfs.io/f/{fileKey}
                response.setUrl("https://utfs.io/f/" + response.getFileKey());
                log.info("Constructed UploadThing URL: {}", response.getUrl());
            }

            log.info("Successfully uploaded file via UploadThing: {} -> {}", 
                    file.getOriginalFilename(), response.getUrl());

            return response;

        } catch (FileStorageException e) {
            throw e;
        } catch (IOException e) {
            log.error("IO error during file upload", e);
            throw new FileStorageException("Failed to process file upload: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Upload process interrupted", e);
            throw new FileStorageException("Upload process was interrupted", e);
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            throw new FileStorageException("Unexpected error during upload: " + e.getMessage(), e);
        } finally {
            // Cleanup temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary file: {}", tempFile, e);
                }
            }
        }
    }

    @Override
    public UploadThingDeleteResponse deleteFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            throw new FileStorageException("File keys list cannot be null or empty");
        }

        try {
            // Build script path
            String scriptPath = new File(deleteScriptPath).getAbsolutePath();
            if (!new File(scriptPath).exists()) {
                scriptPath = deleteScriptPath;
            }

            // Build command with file keys as arguments
            List<String> command = new ArrayList<>();
            command.add("node");
            command.add(scriptPath);
            command.addAll(fileKeys);

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // Set environment variables
            // UPLOADTHING_SECRET is the preferred env var name for the SDK
            processBuilder.environment().put("UPLOADTHING_SECRET", config.getApiSecret());
            processBuilder.environment().put("UPLOADTHING_API_SECRET", config.getApiSecret());
            processBuilder.environment().put("UPLOADTHING_TOKEN", config.getApiSecret());
            
            // Add app ID if available (required when using raw API secret)
            String appId = config.getAppId();
            if (appId != null && !appId.isBlank()) {
                processBuilder.environment().put("UPLOADTHING_APP_ID", appId);
            }

            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            // Read stdout
            StringBuilder stdout = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line);
                }
            }

            // Read stderr
            StringBuilder stderr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            }

            // Wait for process
            boolean finished = process.waitFor(scriptTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new FileStorageException("Delete script timed out after " + scriptTimeoutSeconds + " seconds");
            }

            int exitCode = process.exitValue();
            String jsonResponse = stdout.toString();
            String stderrOutput = stderr.toString();

            // Log for debugging
            log.debug("Delete script exit code: {}", exitCode);
            log.debug("Delete script stdout: {}", jsonResponse);
            if (!stderrOutput.isBlank()) {
                log.debug("Delete script stderr: {}", stderrOutput);
            }

            // Parse response (even if exit code is non-zero, check JSON)
            UploadThingDeleteResponse response;
            try {
                response = objectMapper.readValue(jsonResponse, UploadThingDeleteResponse.class);
            } catch (Exception e) {
                log.error("Failed to parse delete response. Exit code: {}, stdout: {}, stderr: {}", 
                        exitCode, jsonResponse, stderrOutput);
                if (exitCode != 0) {
                    throw new FileStorageException("Delete failed: " + stderrOutput);
                }
                throw new FileStorageException("Failed to parse delete response: " + jsonResponse);
            }
            
            log.debug("Parsed delete response: success={}, deletedFiles={}, error={}", 
                    response.isSuccess(), response.getDeletedFiles(), response.getError());

            if (!response.isSuccess()) {
                throw new FileStorageException(
                        response.getError() != null ? response.getError() : "Delete failed");
            }

            // Only delete database records for files that were actually deleted from UploadThing
            List<String> deletedFromUploadThing = response.getDeletedFiles();
            if (deletedFromUploadThing != null && !deletedFromUploadThing.isEmpty()) {
                deleteDatabaseRecords(deletedFromUploadThing);
                log.info("Successfully deleted {} file(s) via UploadThing and removed database records", 
                        deletedFromUploadThing.size());
            } else {
                log.warn("UploadThing returned success but no deleted files list. File keys: {}", fileKeys);
                // Still try to delete from database as the files might have been deleted
                deleteDatabaseRecords(fileKeys);
            }

            return response;

        } catch (FileStorageException e) {
            throw e;
        } catch (IOException e) {
            log.error("IO error during file deletion", e);
            throw new FileStorageException("Failed to process file deletion: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Delete process interrupted", e);
            throw new FileStorageException("Delete process was interrupted", e);
        } catch (Exception e) {
            log.error("Unexpected error during file deletion", e);
            throw new FileStorageException("Unexpected error during deletion: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes database records for files that were successfully deleted from UploadThing.
     * Files are identified by their filename (which stores the UploadThing file key).
     * 
     * @param fileKeys List of file keys that were deleted from UploadThing
     */
    @org.springframework.transaction.annotation.Transactional
    private void deleteDatabaseRecords(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return;
        }

        int deletedCount = 0;
        for (String fileKey : fileKeys) {
            try {
                // Find files by filename (which stores the file key)
                List<com.tilingroofing.domain.entity.BookingFile> files = 
                        bookingFileRepository.findByFilename(fileKey);
                
                if (!files.isEmpty()) {
                    // Delete all matching files (should typically be just one)
                    bookingFileRepository.deleteAll(files);
                    deletedCount += files.size();
                    log.debug("Deleted {} database record(s) for file key: {}", files.size(), fileKey);
                } else {
                    log.debug("No database record found for file key: {}", fileKey);
                }
            } catch (Exception e) {
                // Log but don't fail the entire operation if one file record deletion fails
                log.warn("Failed to delete database record for file key: {}", fileKey, e);
            }
        }

        if (deletedCount > 0) {
            log.info("Deleted {} database record(s) for {} file key(s)", deletedCount, fileKeys.size());
        }
    }
}

