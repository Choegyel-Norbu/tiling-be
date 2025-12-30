package com.tilingroofing.common.validation;

import com.tilingroofing.domain.enums.JobSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidJobSize annotation.
 */
public class ValidJobSizeValidator implements ConstraintValidator<ValidJobSize, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        try {
            JobSize.fromValue(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

