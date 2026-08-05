package dev.floelly.activitytrackerapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE_USE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllocationsValidator.class)
public @interface ValidAllocations {
    String message() default "Duplicate category allocation keys found.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}