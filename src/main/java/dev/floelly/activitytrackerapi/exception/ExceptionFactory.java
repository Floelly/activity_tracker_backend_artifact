package dev.floelly.activitytrackerapi.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Centralized factory for creating exceptions.
 * <p>
 * This utility class consolidates all exception creation logic to ensure
 * consistent error messages and types across the application.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionFactory {

    // ---- NotFoundException ----

    public static NotFoundException notFound(String resource, String id) {
        return new NotFoundException(resource + " with id '" + id + "' not found.");
    }

    public static NotFoundException notFound(String resource, List<String> ids) {
        return new NotFoundException(String.format("One or more %s with the given ids were not found! %s", resource, ids));
    }

    public static NotFoundException categoryNotFound(String businessId) {
        return new NotFoundException("Category not found " + businessId);
    }

    // ---- BadRequestException ----

    public static BadRequestException idMismatch(String resource) {
        return new BadRequestException(resource + " id in request does not match the path variable");
    }

    public static BadRequestException invalidSubCategoryRelation(String subCategoryName, String subCategoryId,
                                                                  String categoryName, String categoryId) {
        return new BadRequestException("SubCategory '" + subCategoryName
                + "' (id: " + subCategoryId + ") is not related to Category '" + categoryName
                + "' (id: " + categoryId + ").");
    }

    public static BadRequestException allocationSumNot100() {
        return new BadRequestException("Sum of category allocations must be 100%");
    }

    public static BadRequestException duplicateAllocationKeys() {
        return new BadRequestException("Duplicate category allocation keys found.");
    }

    // ---- EntityDeletionConflictException ----

    public static EntityDeletionConflictException categoryHasActivities(String categoryName, String categoryId) {
        return new EntityDeletionConflictException("Category '" + categoryName + "' (id: " + categoryId
                + ") has activities assigned to it.");
    }

    public static EntityDeletionConflictException categoryHasSubCategories(String categoryName, String categoryId) {
        return new EntityDeletionConflictException("Category '" + categoryName + "' (id: " + categoryId
                + ") has sub categories assigned to it.");
    }
}
