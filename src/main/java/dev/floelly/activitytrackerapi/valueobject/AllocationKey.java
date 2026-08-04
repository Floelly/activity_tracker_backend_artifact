package dev.floelly.activitytrackerapi.valueobject;

public record AllocationKey(String categoryId, String subCategoryId) {

    public static AllocationKey of(String categoryId, String subCategoryId) {
        return new AllocationKey(categoryId, subCategoryId);
    }
}