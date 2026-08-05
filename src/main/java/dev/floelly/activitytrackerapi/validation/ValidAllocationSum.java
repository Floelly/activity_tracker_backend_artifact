package dev.floelly.activitytrackerapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE_USE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllocationSumValidator.class)
public @interface ValidAllocationSum {
    String message() default "Sum of category allocations must be 100%";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}