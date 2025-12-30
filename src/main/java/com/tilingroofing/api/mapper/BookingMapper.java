package com.tilingroofing.api.mapper;

import com.tilingroofing.api.dto.response.BlockedDateResponse;
import com.tilingroofing.api.dto.response.BookingResponse;
import com.tilingroofing.api.dto.response.FileResponse;
import com.tilingroofing.domain.entity.BlockedDate;
import com.tilingroofing.domain.entity.Booking;
import com.tilingroofing.domain.entity.BookingFile;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface BookingMapper {

    /**
     * Converts a Booking entity to BookingResponse DTO.
     */
    @Mapping(target = "status", expression = "java(booking.getStatus().getValue())")
    @Mapping(target = "jobSize", expression = "java(booking.getJobSize().getValue())")
    @Mapping(target = "timeSlot", expression = "java(booking.getTimeSlot().getValue())")
    @Mapping(target = "files", source = "files")
    @Mapping(target = "user", source = "user", qualifiedByName = "userToUserInfo")
    BookingResponse toBookingResponse(Booking booking);


    /**
     * Converts a list of Booking entities to BookingResponse DTOs.
     */
    List<BookingResponse> toBookingResponseList(List<Booking> bookings);

    /**
     * Converts a BookingFile entity to FileResponse DTO.
     * URL is set based on filePath - if it's an HTTP/HTTPS URL, use it directly,
     * otherwise use the download endpoint.
     */
    @Mapping(target = "url", expression = "java(bookingFile.getFilePath() != null && (bookingFile.getFilePath().startsWith(\"http://\") || bookingFile.getFilePath().startsWith(\"https://\")) ? bookingFile.getFilePath() : \"/api/files/\" + bookingFile.getId())")
    FileResponse toFileResponse(BookingFile bookingFile);

    /**
     * Converts a list of BookingFile entities to FileResponse DTOs.
     */
    List<FileResponse> toFileResponseList(List<BookingFile> files);

    /**
     * Converts a BlockedDate entity to BlockedDateResponse DTO.
     * Maps booking and user information when available.
     */
    @Mapping(target = "user", expression = "java(blockedDate.getBooking() != null && blockedDate.getBooking().getUser() != null ? toUserInfo(blockedDate.getBooking().getUser()) : null)")
    @Mapping(target = "booking", expression = "java(blockedDate.getBooking() != null ? toBookingSummary(blockedDate.getBooking()) : null)")
    BlockedDateResponse toBlockedDateResponse(BlockedDate blockedDate);
    
    /**
     * Helper method to convert User to UserInfo.
     * Delegates to UserMapper.
     */
    @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId().toString() : null)")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().getName() : null)")
    com.tilingroofing.api.dto.response.UserInfo toUserInfo(com.tilingroofing.domain.entity.User user);

    /**
     * Converts a list of BlockedDate entities to BlockedDateResponse DTOs.
     */
    List<BlockedDateResponse> toBlockedDateResponseList(List<BlockedDate> blockedDates);

    /**
     * Converts a Booking entity to BookingSummary for BlockedDateResponse.
     */
    @Mapping(target = "status", expression = "java(booking.getStatus().getValue())")
    @Mapping(target = "jobSize", expression = "java(booking.getJobSize().getValue())")
    @Mapping(target = "timeSlot", expression = "java(booking.getTimeSlot().getValue())")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "bookingRef", source = "bookingRef")
    @Mapping(target = "serviceId", source = "serviceId")
    @Mapping(target = "suburb", source = "suburb")
    @Mapping(target = "postcode", source = "postcode")
    @Mapping(target = "preferredDate", source = "preferredDate")
    BlockedDateResponse.BookingSummary toBookingSummary(Booking booking);

}

