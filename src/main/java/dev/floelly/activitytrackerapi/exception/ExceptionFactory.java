package dev.floelly.activitytrackerapi.exception;

import java.util.List;

/**
 * Central factory for creating exceptions to avoid inline exception creation across services.
 */
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

    public static BadRequestException badRequest(String message) {
        return new BadRequestException(message);
    }

    public static EntityDeletionConflictException entityDeletionConflict(String message) {
        return new EntityDeletionConflictException(message);
    }
}