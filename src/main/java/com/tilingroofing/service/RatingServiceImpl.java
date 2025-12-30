package com.tilingroofing.service;

import com.tilingroofing.api.dto.request.CreateRatingRequest;
import com.tilingroofing.api.dto.request.UpdateRatingRequest;
import com.tilingroofing.api.dto.response.RatingResponse;
import com.tilingroofing.api.mapper.RatingMapper;
import com.tilingroofing.common.exception.BusinessException;
import com.tilingroofing.common.exception.ResourceNotFoundException;
import com.tilingroofing.domain.entity.Booking;
import com.tilingroofing.domain.entity.Rating;
import com.tilingroofing.domain.repository.BookingRepository;
import com.tilingroofing.domain.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of RatingService.
 * Handles business logic for creating, retrieving, and updating ratings.
 */
@Service
public class RatingServiceImpl implements RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingServiceImpl.class);

    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final RatingMapper ratingMapper;

    public RatingServiceImpl(
            RatingRepository ratingRepository,
            BookingRepository bookingRepository,
            RatingMapper ratingMapper
    ) {
        this.ratingRepository = ratingRepository;
        this.bookingRepository = bookingRepository;
        this.ratingMapper = ratingMapper;
    }

    @Override
    @Transactional
    public RatingResponse createRating(Long bookingId, CreateRatingRequest request, Long userId) {
        // Validate booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Check if rating already exists for this booking
        if (ratingRepository.existsByBookingId(bookingId)) {
            throw new BusinessException("RATING_EXISTS", 
                    "A rating already exists for this booking");
        }

        // Create rating entity
        Rating rating = Rating.builder()
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        rating = ratingRepository.save(rating);
        log.info("Created rating {} for booking {}", rating.getId(), booking.getBookingRef());

        return ratingMapper.toRatingResponse(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getRatingByBookingId(Long bookingId) {
        Rating rating = ratingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", "bookingId", bookingId));

        return ratingMapper.toRatingResponse(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getRatingById(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", "id", id));

        return ratingMapper.toRatingResponse(rating);
    }

    @Override
    @Transactional
    public RatingResponse updateRating(Long id, UpdateRatingRequest request) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", "id", id));

        // Update fields only if provided (partial update)
        if (request.getRating() != null) {
            rating.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            rating.setComment(request.getComment());
        }

        rating = ratingRepository.save(rating);
        log.info("Updated rating: {}", id);

        return ratingMapper.toRatingResponse(rating);
    }

    @Override
    @Transactional
    public void deleteRating(Long id) {
        if (!ratingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rating", "id", id);
        }

        ratingRepository.deleteById(id);
        log.info("Deleted rating: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating() {
        return ratingRepository.calculateAverageRating();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalRatingsCount() {
        return ratingRepository.countAllRatings();
    }
}

