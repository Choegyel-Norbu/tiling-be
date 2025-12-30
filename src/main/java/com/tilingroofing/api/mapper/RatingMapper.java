package com.tilingroofing.api.mapper;

import com.tilingroofing.api.dto.response.RatingResponse;
import com.tilingroofing.domain.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for converting between Rating entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface RatingMapper {

    /**
     * Converts a Rating entity to RatingResponse DTO.
     */
    @Mapping(target = "bookingId", expression = "java(rating.getBooking() != null ? rating.getBooking().getId() : null)")
    @Mapping(target = "bookingRef", expression = "java(rating.getBooking() != null ? rating.getBooking().getBookingRef() : null)")
    RatingResponse toRatingResponse(Rating rating);

    /**
     * Converts a list of Rating entities to RatingResponse DTOs.
     */
    List<RatingResponse> toRatingResponseList(List<Rating> ratings);
}

