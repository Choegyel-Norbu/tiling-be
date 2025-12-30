package com.tilingroofing.common.validation;

import com.tilingroofing.domain.enums.BookingStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidBookingStatus annotation.
 */
public class ValidBookingStatusValidator implements ConstraintValidator<ValidBookingStatus, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        try {
            BookingStatus.fromValue(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

