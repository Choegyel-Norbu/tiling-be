package com.tilingroofing.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to ensure time slot value is valid.
 */
@Documented
@Constraint(validatedBy = ValidTimeSlotValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeSlot {
    String message() default "Time slot must be one of: morning, afternoon, flexible";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

