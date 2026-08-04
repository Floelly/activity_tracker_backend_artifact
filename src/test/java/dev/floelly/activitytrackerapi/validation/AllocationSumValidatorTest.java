package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationSumValidatorTest {

    private final AllocationSumValidator validator = new AllocationSumValidator();

    @Test
    void nullValue_shouldBeValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void emptyList_shouldBeValid() {
        assertThat(validator.isValid(List.of(), null)).isTrue();
    }

    @Test
    void sumExactly100_shouldBeValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(70, "cat-1", null),
                new CreateCategoryAllocationRequest(30, "cat-2", null)
        );

        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void singleAllocation100_shouldBeValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(100, "cat-1", null)
        );

        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void sumLessThan100_shouldBeInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(60, "cat-1", null),
                new CreateCategoryAllocationRequest(30, "cat-2", null)
        );

        assertThat(validator.isValid(allocations, null)).isFalse();
    }

    @Test
    void sumMoreThan100_shouldBeInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(60, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-2", null)
        );

        assertThat(validator.isValid(allocations, null)).isFalse();
    }
}