package dev.floelly.activitytrackerapi.valueobject;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;

/**
 * Immutable value object representing a unique allocation key composed of a category ID and an optional sub-category ID.
 * The key format is: {@code categoryId::subCategoryId} (or {@code categoryId::null} when subCategoryId is absent).
 */
public record AllocationKey(String categoryId, String subCategoryId) {

    private static final String DELIMITER = "::";

    /**
     * Creates an {@code AllocationKey} from a {@link CreateCategoryAllocationRequest}.
     */
    public static AllocationKey from(CreateCategoryAllocationRequest request) {
        return new AllocationKey(request.categoryId(), request.subCategoryId());
    }

    /**
     * Creates an {@code AllocationKey} from a {@link CategoryAllocation} entity.
     */
    public static AllocationKey from(CategoryAllocation allocation) {
        String subCategoryId = allocation.getSubCategory() != null
                ? allocation.getSubCategory().getBusinessId()
                : null;
        return new AllocationKey(allocation.getCategory().getBusinessId(), subCategoryId);
    }

    @Override
    public String toString() {
        return categoryId + DELIMITER + subCategoryId;
    }
}