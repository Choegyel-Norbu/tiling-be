package com.tilingroofing.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to ensure job size value is valid.
 */
@Documented
@Constraint(validatedBy = ValidJobSizeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidJobSize {
    String message() default "Job size must be one of: small, medium, large";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

