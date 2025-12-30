package com.tilingroofing.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to ensure booking status value is valid.
 */
@Documented
@Constraint(validatedBy = ValidBookingStatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingStatus {
    String message() default "Invalid booking status";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

