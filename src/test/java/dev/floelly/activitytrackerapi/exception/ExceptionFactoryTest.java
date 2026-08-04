package dev.floelly.activitytrackerapi.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionFactoryTest {

    @Test
    void notFound_shouldCreateNotFoundExceptionWithCorrectMessage() {
        NotFoundException exception = ExceptionFactory.notFound("Activity", "act-123");

        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Activity with id 'act-123' not found.");
    }

    @Test
    void categoryNotFound_shouldCreateNotFoundExceptionWithCorrectMessage() {
        NotFoundException exception = ExceptionFactory.categoryNotFound("cat-456");

        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found cat-456");
    }

    @Test
    void categoriesNotFound_shouldCreateNotFoundExceptionWithCorrectMessage() {
        List<String> missingIds = List.of("cat-1", "cat-2");
        NotFoundException exception = ExceptionFactory.categoriesNotFound(missingIds);

        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("One or more categories with the given ids were not found! [cat-1, cat-2]");
    }

    @Test
    void badRequestIdMismatch_shouldCreateBadRequestExceptionWithCorrectMessage() {
        BadRequestException exception = ExceptionFactory.badRequestIdMismatch("Activity");

        assertThat(exception)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Activity id in request does not match the path variable");
    }

    @Test
    void badRequestSubCategoryNotRelated_shouldCreateBadRequestExceptionWithCorrectMessage() {
        BadRequestException exception = ExceptionFactory.badRequestSubCategoryNotRelated(
                "Sub A", "sub-1", "Category A", "cat-1");

        assertThat(exception)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("SubCategory 'Sub A' (id: sub-1) is not related to Category 'Category A' (id: cat-1).");
    }

    @Test
    void badRequestAllocationSumNot100_shouldCreateBadRequestExceptionWithCorrectMessage() {
        BadRequestException exception = ExceptionFactory.badRequestAllocationSumNot100();

        assertThat(exception)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sum of category allocations must be 100%");
    }

    @Test
    void badRequestDuplicateAllocationKeys_shouldCreateBadRequestExceptionWithCorrectMessage() {
        BadRequestException exception = ExceptionFactory.badRequestDuplicateAllocationKeys();

        assertThat(exception)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate category allocation keys found.");
    }

    @Test
    void badRequest_shouldCreateBadRequestExceptionWithGivenMessage() {
        BadRequestException exception = ExceptionFactory.badRequest("Custom error message");

        assertThat(exception)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Custom error message");
    }

    @Test
    void entityDeletionConflictCategoryHasActivities_shouldCreateEntityDeletionConflictExceptionWithCorrectMessage() {
        EntityDeletionConflictException exception = ExceptionFactory.entityDeletionConflictCategoryHasActivities("Sport", "cat-1");

        assertThat(exception)
                .isInstanceOf(EntityDeletionConflictException.class)
                .hasMessage("Category 'Sport' (id: cat-1) has activities assigned to it.");
    }

    @Test
    void entityDeletionConflictCategoryHasSubCategories_shouldCreateEntityDeletionConflictExceptionWithCorrectMessage() {
        EntityDeletionConflictException exception = ExceptionFactory.entityDeletionConflictCategoryHasSubCategories("Work", "cat-2");

        assertThat(exception)
                .isInstanceOf(EntityDeletionConflictException.class)
                .hasMessage("Category 'Work' (id: cat-2) has sub categories assigned to it.");
    }
}
