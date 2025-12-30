package com.tilingroofing.service;

import com.tilingroofing.domain.entity.BookingFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for handling file storage operations.
 * Defines the contract for file storage operations.
 */
public interface FileStorageService {

    /**
     * Initializes the upload directory on startup.
     */
    void init();

    /**
     * Validates and stores a file, returning the stored file information.
     * 
     * @param file The multipart file to store
     * @param bookingRef The booking reference for organizing files
     * @return BookingFile entity with file metadata
     */
    BookingFile storeFile(MultipartFile file, String bookingRef);

    /**
     * Loads a file as a resource by file ID.
     * 
     * @param fileId The file ID
     * @return Resource representing the file
     */
    Resource loadFileAsResource(Long fileId);

    /**
     * Deletes a file.
     * 
     * @param file The booking file to delete
     */
    void deleteFile(BookingFile file);

    /**
     * Deletes all files for a booking.
     * 
     * @param bookingRef The booking reference
     */
    void deleteBookingFiles(String bookingRef);
}
