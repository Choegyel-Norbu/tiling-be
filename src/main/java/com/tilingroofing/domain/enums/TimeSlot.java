package com.tilingroofing.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing preferred time slots for a booking.
 */
public enum TimeSlot {
    MORNING("morning"),
    AFTERNOON("afternoon"),
    FLEXIBLE("flexible");

    private final String value;

    TimeSlot(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Parses a string value to TimeSlot enum.
     * Case-insensitive matching.
     */
    public static TimeSlot fromValue(String value) {
        for (TimeSlot slot : TimeSlot.values()) {
            if (slot.value.equalsIgnoreCase(value)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("Unknown time slot: " + value);
    }
}

