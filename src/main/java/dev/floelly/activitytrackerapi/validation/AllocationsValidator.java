package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class AllocationsValidator
        implements ConstraintValidator<ValidAllocations, List<CreateCategoryAllocationRequest>> {

    @Override
    public boolean isValid(List<CreateCategoryAllocationRequest> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        long distinctCount = value.stream()
                .map(this::allocationKey)
                .distinct()
                .count();
        return distinctCount == value.size();
    }

    private String allocationKey(CreateCategoryAllocationRequest request) {
        return request.categoryId() + "::" + request.subCategoryId();
    }
}