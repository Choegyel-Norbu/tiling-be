package com.tilingroofing.domain.repository;

import com.tilingroofing.domain.entity.BookingFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BookingFile entities.
 * Provides data access operations for uploaded files.
 */
@Repository
public interface BookingFileRepository extends JpaRepository<BookingFile, Long> {

    /**
     * Finds all files associated with a booking.
     */
    List<BookingFile> findByBookingId(Long bookingId);

    /**
     * Deletes all files associated with a booking.
     */
    void deleteByBookingId(Long bookingId);

    /**
     * Finds a file by its filename (which stores the UploadThing file key).
     */
    List<BookingFile> findByFilename(String filename);

    /**
     * Deletes a file by its filename (file key).
     */
    void deleteByFilename(String filename);
}

