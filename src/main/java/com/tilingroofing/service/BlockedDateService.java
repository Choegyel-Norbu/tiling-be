package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.BlockDatesRequest;
import com.tilingroofing.api.dto.response.BlockedDateResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing blocked dates.
 * Defines the contract for blocked date operations.
 */
public interface BlockedDateService {

    /**
     * Blocks multiple dates from booking.
     * 
     * @param request The request containing dates to block and reason
     * @return List of blocked date responses
     */
    List<BlockedDateResponse> blockDates(BlockDatesRequest request);

    /**
     * Unblocks a date.
     * 
     * @param id The blocked date ID
     */
    void unblockDate(Long id);

    /**
     * Gets all upcoming blocked dates.
     * 
     * @return List of blocked date responses
     */
    List<BlockedDateResponse> getUpcomingBlockedDates();

    /**
     * Gets blocked dates within a date range.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of blocked date responses
     */
    List<BlockedDateResponse> getBlockedDatesInRange(LocalDate startDate, LocalDate endDate);

    /**
     * Checks if a date is blocked.
     * 
     * @param date The date to check
     * @return true if the date is blocked, false otherwise
     */
    boolean isDateBlocked(LocalDate date);
}
