package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationsValidatorTest {

    private final AllocationsValidator validator = new AllocationsValidator();

    @Test
    void nullValue_shouldBeValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void emptyList_shouldBeValid() {
        assertThat(validator.isValid(List.of(), null)).isTrue();
    }

    @Test
    void allUniqueKeys_shouldBeValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-2")
        );

        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void sameCategoryDifferentSubCategories_shouldBeValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(40, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(60, "cat-1", "sub-2")
        );

        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void differentCategoriesNullSubCategories_shouldBeValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-2", null)
        );

        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void sameCategoryAndSameSubCategory_shouldBeInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1")
        );

        assertThat(validator.isValid(allocations, null)).isFalse();
    }

    @Test
    void sameCategoryBothNullSubCategory_shouldBeInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-1", null)
        );

        assertThat(validator.isValid(allocations, null)).isFalse();
    }

    @Test
    void sameCategoryOneNullOneNonNullSubCategory_shouldBeValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1")
        );

        assertThat(validator.isValid(allocations, null)).isTrue();
    }
}