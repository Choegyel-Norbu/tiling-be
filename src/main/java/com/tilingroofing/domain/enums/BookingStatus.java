package com.tilingroofing.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the possible statuses of a booking.
 */
public enum BookingStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Parses a string value to BookingStatus enum.
     * Case-insensitive matching.
     */
    public static BookingStatus fromValue(String value) {
        for (BookingStatus status : BookingStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown booking status: " + value);
    }
}

