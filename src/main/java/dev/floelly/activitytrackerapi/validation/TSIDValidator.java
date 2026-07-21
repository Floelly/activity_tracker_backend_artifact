package dev.floelly.activitytrackerapi.validation;

import io.hypersistence.tsid.TSID;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

public class TSIDValidator implements ConstraintValidator<ValidTSID, String> {

    private boolean nullable;

    @Override
    public void initialize(ValidTSID annotation) {
        this.nullable = annotation.nullable();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return nullable;
        }

        if (value.length() != 13) {
            return false;
        }

        try {
            return TSID.from(value).toString().equals(value.toUpperCase(Locale.ENGLISH));
        } catch (Exception ex) {
            return false;
        }
    }
}
