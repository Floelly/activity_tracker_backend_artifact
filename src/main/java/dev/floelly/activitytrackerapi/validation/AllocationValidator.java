package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllocationValidator {

    public void validateAllocationSum(List<CreateCategoryAllocationRequest> allocations) {
        int percentageSum = allocations.stream()
                .mapToInt(CreateCategoryAllocationRequest::percentage).sum();
        if (percentageSum != 100) {
            throw new BadRequestException("Sum of category allocations must be 100%");
        }
    }

    public void validateAllocations(List<CreateCategoryAllocationRequest> allocationRequests) {
        long distinctCount = allocationRequests.stream()
                .map(this::allocationKey)
                .distinct()
                .count();
        if (distinctCount != allocationRequests.size()) {
            throw new BadRequestException("Duplicate category allocation keys found.");
        }
    }

    public String allocationKey(CreateCategoryAllocationRequest request) {
        return request.categoryId() + "::" + request.subCategoryId();
    }
}
