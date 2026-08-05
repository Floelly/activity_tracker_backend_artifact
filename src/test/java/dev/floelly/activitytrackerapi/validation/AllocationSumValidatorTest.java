package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationSumValidatorTest {

    private final AllocationSumValidator validator = new AllocationSumValidator();

    @Test
    void nullListIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void emptyListIsValid() {
        assertThat(validator.isValid(List.of(), null)).isTrue();
    }

    @Test
    void singleAllocationWith100IsValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(100, "cat-1", null)
        );
        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void twoAllocationsSummingTo100IsValid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(80, "cat-1", null),
                new CreateCategoryAllocationRequest(20, "cat-2", null)
        );
        assertThat(validator.isValid(allocations, null)).isTrue();
    }

    @Test
    void twoAllocationsSummingTo90IsInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(60, "cat-1", null),
                new CreateCategoryAllocationRequest(30, "cat-2", null)
        );
        assertThat(validator.isValid(allocations, null)).isFalse();
    }

    @Test
    void twoAllocationsSummingTo110IsInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(80, "cat-1", null),
                new CreateCategoryAllocationRequest(30, "cat-2", null)
        );
        assertThat(validator.isValid(allocations, null)).isFalse();
    }

    @Test
    void singleAllocationWith50IsInvalid() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null)
        );
        assertThat(validator.isValid(allocations, null)).isFalse();
    }
}