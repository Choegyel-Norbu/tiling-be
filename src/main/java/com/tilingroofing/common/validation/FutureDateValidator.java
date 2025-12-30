package com.tilingroofing.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Validator implementation for @FutureDate annotation.
 * Validates that a date string is in the future.
 */
public class FutureDateValidator implements ConstraintValidator<FutureDate, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        try {
            LocalDate date = LocalDate.parse(value);
            return date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid date format. Use ISO format (YYYY-MM-DD)")
                    .addConstraintViolation();
            return false;
        }
    }
}

