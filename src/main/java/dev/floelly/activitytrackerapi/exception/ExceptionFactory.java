package dev.floelly.activitytrackerapi.exception;

import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.SubCategory;

import java.util.List;

public final class ExceptionFactory {

    private ExceptionFactory() {
    }

    public static NotFoundException resourceNotFound(String resource, String id) {
        return new NotFoundException(resource + " with id '" + id + "' not found.");
    }

    public static NotFoundException categoryNotFound(String businessId) {
        return new NotFoundException("Category not found " + businessId);
    }

    public static NotFoundException categoriesNotFound(List<String> missingIds) {
        return new NotFoundException(String.format("One or more categories with the given ids were not found! %s", missingIds));
    }

    public static BadRequestException idMismatch(String resourceName) {
        return new BadRequestException(resourceName + " id in request does not match the path variable");
    }

    public static BadRequestException invalidSubCategoryRelation(SubCategory subCategory, Category category) {
        return new BadRequestException("SubCategory '" + subCategory.getName()
                + "' (id: " + subCategory.getBusinessId() + ") is not related to Category '" + category.getName()
                + "' (id: " + category.getBusinessId() + ").");
    }

    public static BadRequestException allocationSumNot100() {
        return new BadRequestException("Sum of category allocations must be 100%");
    }

    public static BadRequestException duplicateAllocationKeys() {
        return new BadRequestException("Duplicate category allocation keys found.");
    }

    public static EntityDeletionConflictException categoryHasActivities(Category category) {
        return new EntityDeletionConflictException("Category '" + category.getName() + "' (id: " + category.getBusinessId()
                + ") has activities assigned to it.");
    }

    public static EntityDeletionConflictException categoryHasSubCategories(Category category) {
        return new EntityDeletionConflictException("Category '" + category.getName() + "' (id: " + category.getBusinessId()
                + ") has sub categories assigned to it.");
    }
}