package dev.floelly.activitytrackerapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TSIDValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTSID {

    String message() default "must be a valid 13-character TSID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Darf der Wert null sein?
     * Default: false = null ist nicht erlaubt.
     */
    boolean nullable() default false;
}
