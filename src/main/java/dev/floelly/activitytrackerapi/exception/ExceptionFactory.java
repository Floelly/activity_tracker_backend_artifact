package dev.floelly.activitytrackerapi.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionFactory {

    public static NotFoundException notFound(String resource, String id) {
        return new NotFoundException(resource + " with id '" + id + "' not found.");
    }

    public static NotFoundException categoryNotFound(String businessId) {
        return new NotFoundException("Category not found " + businessId);
    }

    public static NotFoundException categoriesNotFound(List<String> missingIds) {
        return new NotFoundException(String.format("One or more categories with the given ids were not found! %s", missingIds));
    }

    public static BadRequestException badRequestIdMismatch(String resource) {
        return new BadRequestException(resource + " id in request does not match the path variable");
    }

    public static BadRequestException badRequestSubCategoryNotRelated(String subName, String subId, String catName, String catId) {
        return new BadRequestException("SubCategory '" + subName
                + "' (id: " + subId + ") is not related to Category '" + catName
                + "' (id: " + catId + ").");
    }

    public static BadRequestException badRequestAllocationSumNot100() {
        return new BadRequestException("Sum of category allocations must be 100%");
    }

    public static BadRequestException badRequestDuplicateAllocationKeys() {
        return new BadRequestException("Duplicate category allocation keys found.");
    }

    public static BadRequestException badRequest(String message) {
        return new BadRequestException(message);
    }

    public static EntityDeletionConflictException entityDeletionConflictCategoryHasActivities(String name, String id) {
        return new EntityDeletionConflictException("Category '" + name + "' (id: " + id + ") has activities assigned to it.");
    }

    public static EntityDeletionConflictException entityDeletionConflictCategoryHasSubCategories(String name, String id) {
        return new EntityDeletionConflictException("Category '" + name + "' (id: " + id + ") has sub categories assigned to it.");
    }
}
