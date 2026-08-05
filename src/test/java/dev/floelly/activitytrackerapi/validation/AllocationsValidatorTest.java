package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationsValidatorTest {

    private final AllocationsValidator validator = new AllocationsValidator();

    @Test
    void nullListIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void emptyListIsValid() {
        assertThat(validator.isValid(List.of(), null)).isTrue();
    }

    @Test
    void singleAllocationIsValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(100, "cat-1", null)
        );
        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void twoAllocationsWithDifferentKeysIsValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-2", null)
        );
        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void twoAllocationsWithSameCategoryIdAndNullSubCategoryIdIsInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-1", null)
        );
        assertThat(validator.isValid(allocations, null)).isFalse();
    }

    @Test
    void twoAllocationsWithSameCategoryIdAndSameSubCategoryIdIsInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1")
        );
        assertThat(validator.isValid(allocations, null)).isFalse();
    }

    @Test
    void twoAllocationsWithSameCategoryIdButDifferentSubCategoryIdIsValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-2")
        );
        assertThat(validator.isValid(allocations, null)).isTrue();
    }
}