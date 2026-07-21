package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.TimeRangeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TimeRangeValidator
        implements ConstraintValidator<ValidTimeRange, TimeRangeRequest> {

    @Override
    public boolean isValid(TimeRangeRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.startAt() == null || value.endAt() == null) { // NOSONAR - startAt/endAt are @NotNull by contract but better guarded defensively
            return true;
        }
        return value.endAt().isAfter(value.startAt());
    }
}
