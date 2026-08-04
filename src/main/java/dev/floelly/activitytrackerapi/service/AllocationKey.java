package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;

record AllocationKey(String categoryId, String subCategoryId) {

    static AllocationKey from(CreateCategoryAllocationRequest request) {
        return new AllocationKey(request.categoryId(), request.subCategoryId());
    }

    static AllocationKey from(CategoryAllocation allocation) {
        String subCategoryId = allocation.getSubCategory() != null
                ? allocation.getSubCategory().getBusinessId()
                : null;
        return new AllocationKey(allocation.getCategory().getBusinessId(), subCategoryId);
    }
}