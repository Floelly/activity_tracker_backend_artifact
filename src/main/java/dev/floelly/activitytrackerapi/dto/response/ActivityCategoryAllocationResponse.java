package dev.floelly.activitytrackerapi.dto.response;

public record ActivityCategoryAllocationResponse(int percentage, ActivityCategoryResponse category,
                                                 ActivitySubCategoryResponse subcategory) {
}
