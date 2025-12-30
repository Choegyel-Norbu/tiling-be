package com.tilingroofing.common.validation;

import com.tilingroofing.domain.enums.TimeSlot;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidTimeSlot annotation.
 */
public class ValidTimeSlotValidator implements ConstraintValidator<ValidTimeSlot, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        try {
            TimeSlot.fromValue(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

