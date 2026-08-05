package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class AllocationSumValidator
        implements ConstraintValidator<ValidAllocationSum, List<CreateCategoryAllocationRequest>> {

    @Override
    public boolean isValid(List<CreateCategoryAllocationRequest> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        int percentageSum = value.stream()
                .mapToInt(CreateCategoryAllocationRequest::percentage)
                .sum();
        return percentageSum == 100;
    }
}