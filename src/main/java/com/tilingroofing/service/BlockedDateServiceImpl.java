package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.BlockDatesRequest;
import com.tilingroofing.api.dto.response.BlockedDateResponse;
import com.tilingroofing.api.mapper.BookingMapper;
import com.tilingroofing.common.exception.BusinessException;
import com.tilingroofing.domain.entity.BlockedDate;
import com.tilingroofing.domain.repository.BlockedDateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of BlockedDateService.
 * Handles business logic for managing blocked dates.
 */
@Service
public class BlockedDateServiceImpl implements BlockedDateService {

    private static final Logger log = LoggerFactory.getLogger(BlockedDateServiceImpl.class);

    private final BlockedDateRepository blockedDateRepository;
    private final BookingMapper bookingMapper;

    public BlockedDateServiceImpl(BlockedDateRepository blockedDateRepository, BookingMapper bookingMapper) {
        this.blockedDateRepository = blockedDateRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    @Transactional
    public List<BlockedDateResponse> blockDates(BlockDatesRequest request) {
        List<BlockedDate> blockedDates = new ArrayList<>();

        for (String dateStr : request.getDates()) {
            LocalDate date = parseDate(dateStr);
            
            // Validate date is in the future
            if (!date.isAfter(LocalDate.now())) {
                throw new BusinessException("INVALID_DATE", 
                        "Cannot block past dates: " + dateStr);
            }

            // Skip if already blocked
            if (blockedDateRepository.existsByDate(date)) {
                log.info("Date {} is already blocked, skipping", date);
                continue;
            }

            BlockedDate blockedDate = BlockedDate.builder()
                    .date(date)
                    .reason(request.getReason())
                    .build();

            blockedDates.add(blockedDateRepository.save(blockedDate));
            log.info("Blocked date: {}", date);
        }

        return bookingMapper.toBlockedDateResponseList(blockedDates);
    }

    @Override
    @Transactional
    public void unblockDate(Long id) {
        if (!blockedDateRepository.existsById(id)) {
            throw new BusinessException("NOT_FOUND", "Blocked date not found");
        }
        blockedDateRepository.deleteById(id);
        log.info("Unblocked date with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockedDateResponse> getUpcomingBlockedDates() {
        List<BlockedDate> blockedDates = blockedDateRepository
                .findUpcomingBlockedDates(LocalDate.now());
        return bookingMapper.toBlockedDateResponseList(blockedDates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockedDateResponse> getBlockedDatesInRange(LocalDate startDate, LocalDate endDate) {
        List<BlockedDate> blockedDates = blockedDateRepository
                .findByDateBetween(startDate, endDate);
        return bookingMapper.toBlockedDateResponseList(blockedDates);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDateBlocked(LocalDate date) {
        return blockedDateRepository.existsByDate(date);
    }

    /**
     * Parses a date string to LocalDate.
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new BusinessException("INVALID_DATE_FORMAT", 
                    "Invalid date format: " + dateStr + ". Use ISO format (YYYY-MM-DD)");
        }
    }
}

