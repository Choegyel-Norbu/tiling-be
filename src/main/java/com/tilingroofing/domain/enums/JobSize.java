package com.tilingroofing.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the size of a job.
 */
public enum JobSize {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    private final String value;

    JobSize(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Parses a string value to JobSize enum.
     * Case-insensitive matching.
     */
    public static JobSize fromValue(String value) {
        for (JobSize size : JobSize.values()) {
            if (size.value.equalsIgnoreCase(value)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown job size: " + value);
    }
}

