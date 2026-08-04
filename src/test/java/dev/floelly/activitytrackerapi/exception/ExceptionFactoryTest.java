package dev.floelly.activitytrackerapi.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionFactoryTest {

    @Test
    void resourceNotFound_shouldReturnNotFoundExceptionWithCorrectMessage() {
        NotFoundException exception = ExceptionFactory.resourceNotFound("Activity", "abc-123");

        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Activity with id 'abc-123' not found.");
    }

    @Test
    void categoryNotFound_shouldReturnNotFoundExceptionWithCorrectMessage() {
        NotFoundException exception = ExceptionFactory.categoryNotFound("abc-123");

        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found abc-123");
    }

    @Test
    void categoriesNotFound_shouldReturnNotFoundExceptionWithCorrectMessage() {
        NotFoundException exception = ExceptionFactory.categoriesNotFound(List.of("cat-1", "cat-2"));

        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("One or more categories with the given ids were not found! [cat-1, cat-2]");
    }

    @Test
    void badRequest_shouldReturnBadRequestExceptionWithCorrectMessage() {
        BadRequestException exception = ExceptionFactory.badRequest("Test message");

        assertThat(exception)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Test message");
    }

    @Test
    void entityDeletionConflict_shouldReturnEntityDeletionConflictExceptionWithCorrectMessage() {
        EntityDeletionConflictException exception = ExceptionFactory.entityDeletionConflict("Test conflict message");

        assertThat(exception)
                .isInstanceOf(EntityDeletionConflictException.class)
                .hasMessage("Test conflict message");
    }
}