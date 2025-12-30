package com.tilingroofing.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to ensure a date string represents a future date.
 */
@Documented
@Constraint(validatedBy = FutureDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureDate {
    String message() default "Date must be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

