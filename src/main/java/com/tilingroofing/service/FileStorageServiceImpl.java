package com.tilingroofing.service;

import com.tilingroofing.common.exception.FileStorageException;
import com.tilingroofing.common.exception.InvalidFileException;
import com.tilingroofing.common.exception.ResourceNotFoundException;
import com.tilingroofing.config.FileStorageProperties;
import com.tilingroofing.config.UploadThingConfig;
import com.tilingroofing.domain.entity.BookingFile;
import com.tilingroofing.domain.repository.BookingFileRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Implementation of FileStorageService.
 * Handles file storage operations with support for both local and cloud storage.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path uploadPath;
    private final List<String> allowedTypes;
    private final long maxFileSize;
    private final BookingFileRepository fileRepository;
    private final UploadThingScriptService uploadThingService;
    private final UploadThingConfig uploadThingConfig;
    private final com.tilingroofing.domain.repository.BookingRepository bookingRepository;

    public FileStorageServiceImpl(
            FileStorageProperties properties,
            BookingFileRepository fileRepository,
            UploadThingScriptService uploadThingService,
            UploadThingConfig uploadThingConfig,
            com.tilingroofing.domain.repository.BookingRepository bookingRepository
    ) {
        this.uploadPath = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        this.allowedTypes = properties.getAllowedTypes();
        this.maxFileSize = properties.getMaxFileSize();
        this.fileRepository = fileRepository;
        this.uploadThingService = uploadThingService;
        this.uploadThingConfig = uploadThingConfig;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadPath);
            log.info("File upload directory initialized: {}", uploadPath);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory", e);
        }
    }

    @Override
    public BookingFile storeFile(MultipartFile file, String bookingRef) {
        validateFile(file);
        
        // Try UploadThing if configured
        if (isUploadThingEnabled()) {
            try {
                return storeFileWithUploadThing(file, bookingRef);
            } catch (Exception e) {
                log.warn("UploadThing upload failed, falling back to local storage: {}", e.getMessage());
                // Fallback to local storage on failure
            }
        }
        
        // Use local storage
        return storeFileLocally(file, bookingRef);
    }
    
    /**
     * Checks if UploadThing is enabled and configured.
     */
    private boolean isUploadThingEnabled() {
        String apiSecret = uploadThingConfig.getApiSecret();
        return apiSecret != null && !apiSecret.isBlank();
    }
    
    /**
     * Stores file using UploadThing cloud storage.
     */
    private BookingFile storeFileWithUploadThing(MultipartFile file, String bookingRef) {
        String originalFilename = file.getOriginalFilename();
        
        // Determine file type
        String contentType = file.getContentType();
        String fileType = "image"; // default
        if (contentType != null && contentType.equals("application/pdf")) {
            fileType = "pdf";
        }
        
        // Upload to UploadThing
        com.tilingroofing.api.dto.response.UploadThingUploadResponse uploadResponse = 
                uploadThingService.uploadFile(file, "booking-files", fileType);
        
        if (!uploadResponse.isSuccess()) {
            throw new FileStorageException("UploadThing upload failed: " + 
                    (uploadResponse.getError() != null ? uploadResponse.getError() : "Unknown error"));
        }
        
        // Get the file key - required for success
        String fileKey = uploadResponse.getFileKey();
        if (fileKey == null || fileKey.isBlank()) {
            throw new FileStorageException("UploadThing upload succeeded but no file key returned");
        }
        
        // Get the URL - construct from fileKey if not provided
        String fileUrl = uploadResponse.getUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            // Construct URL from file key: https://utfs.io/f/{fileKey}
            fileUrl = "https://utfs.io/f/" + fileKey;
            log.info("Constructed UploadThing URL from fileKey: {}", fileUrl);
        }
        
        // Store metadata in database
        // Note: filePath stores the UploadThing URL for cloud-stored files
        BookingFile bookingFile = BookingFile.builder()
                .filename(fileKey)
                .originalFilename(originalFilename)
                .filePath(fileUrl) // Store UploadThing URL
                .fileSize(uploadResponse.getFileSize() != null ? uploadResponse.getFileSize() : file.getSize())
                .mimeType(file.getContentType())
                .build();
        
        log.info("Stored file with UploadThing: {} -> {} for booking {}", 
                originalFilename, fileUrl, bookingRef);
        
        return bookingFile;
    }

    /**
     * Stores file on local filesystem.
     */
    private BookingFile storeFileLocally(MultipartFile file, String bookingRef) {
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String storedFilename = System.currentTimeMillis() + "_" + 
                (int)(Math.random() * 10000) + "." + extension;
        
        // Create booking-specific subdirectory
        Path bookingDir = uploadPath.resolve(bookingRef);
        
        try {
            Files.createDirectories(bookingDir);
            Path targetLocation = bookingDir.resolve(storedFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Stored file locally: {} for booking {}", storedFilename, bookingRef);

            return BookingFile.builder()
                    .filename(storedFilename)
                    .originalFilename(originalFilename)
                    .filePath(targetLocation.toString())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .build();

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }
    }

    /**
     * Validates a file against size and type restrictions.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File cannot be null or empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException(
                    String.format("File size %d bytes exceeds maximum allowed size of %d bytes",
                            file.getSize(), maxFileSize));
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            throw new InvalidFileException(
                    String.format("File type '%s' is not allowed. Allowed types: %s",
                            contentType, String.join(", ", allowedTypes)));
        }
    }

    /**
     * Checks if the content type is allowed.
     */
    private boolean isAllowedType(String contentType) {
        return allowedTypes.stream()
                .anyMatch(allowed -> {
                    if (allowed.endsWith("/*")) {
                        String prefix = allowed.replace("/*", "/");
                        return contentType.startsWith(prefix);
                    }
                    return contentType.equalsIgnoreCase(allowed);
                });
    }

    @Override
    public Resource loadFileAsResource(Long fileId) {
        BookingFile bookingFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        String filePath = bookingFile.getFilePath();
        
        // If filePath is a URL (UploadThing), we can't load it as a local resource
        if (filePath != null && (filePath.startsWith("http://") || filePath.startsWith("https://"))) {
            throw new FileStorageException("Cannot load UploadThing file as local resource. Use the URL directly: " + filePath);
        }

        try {
            Path file = uploadPath.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Invalid file path: " + filePath, e);
        }
    }

    @Override
    public void deleteFile(BookingFile file) {
        if (file == null || file.getFilePath() == null || file.getFilePath().isBlank()) {
            log.warn("Cannot delete file with null or blank file path: {}", 
                    file != null ? file.getFilename() : "null");
            return;
        }

        String filePath = file.getFilePath();
        
        // Check if it's an UploadThing URL
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            log.info("File stored in UploadThing (URL: {}). Use UploadThing delete endpoint to remove it.", filePath);
            // Note: UploadThing files should be deleted via /api/v1/uploadthing/files/{fileKey} endpoint
            return;
        }

        // Delete from local filesystem
        try {
            Path fileToDelete = uploadPath.resolve(filePath).normalize();
            Files.deleteIfExists(fileToDelete);
            log.info("Deleted local file: {}", file.getFilename());
        } catch (IOException e) {
            log.error("Failed to delete local file: {}", file.getFilename(), e);
        }
    }

    @Override
    public void deleteBookingFiles(String bookingRef) {
        List<BookingFile> files = fileRepository.findByBookingId(
                bookingRepository.findByBookingRef(bookingRef)
                        .map(booking -> booking.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingRef", bookingRef))
        );

        for (BookingFile file : files) {
            deleteFile(file);
        }

        log.info("Deleted all files for booking: {}", bookingRef);
    }
}

