package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationValidatorTest {

    private final AllocationValidator validator = new AllocationValidator();

    @Test
    void validateAllocationSum_shouldPassWhenSumIs100() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(60, "cat-1", null),
                new CreateCategoryAllocationRequest(40, "cat-2", null)
        );

        validator.validateAllocationSum(allocations);
        // no exception expected
    }

    @Test
    void validateAllocationSum_shouldPassWithSingleAllocationOf100() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(100, "cat-1", null)
        );

        validator.validateAllocationSum(allocations);
        // no exception expected
    }

    @Test
    void validateAllocationSum_shouldThrowWhenSumIsLessThan100() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(30, "cat-1", null),
                new CreateCategoryAllocationRequest(30, "cat-2", null)
        );

        assertThatThrownBy(() -> validator.validateAllocationSum(allocations))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sum of category allocations must be 100%");
    }

    @Test
    void validateAllocationSum_shouldThrowWhenSumIsMoreThan100() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(80, "cat-1", null),
                new CreateCategoryAllocationRequest(40, "cat-2", null)
        );

        assertThatThrownBy(() -> validator.validateAllocationSum(allocations))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sum of category allocations must be 100%");
    }

    @Test
    void validateAllocationSum_shouldThrowWhenSumIsZero() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(0, "cat-1", null)
        );

        assertThatThrownBy(() -> validator.validateAllocationSum(allocations))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sum of category allocations must be 100%");
    }

    @Test
    void validateAllocations_shouldPassWhenAllKeysAreDistinct() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-2", null)
        );

        validator.validateAllocations(allocations);
        // no exception expected
    }

    @Test
    void validateAllocations_shouldPassWhenSameCategoryButDifferentSubCategories() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-2")
        );

        validator.validateAllocations(allocations);
        // no exception expected
    }

    @Test
    void validateAllocations_shouldThrowWhenDuplicateKeysExist() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", null),
                new CreateCategoryAllocationRequest(50, "cat-1", null)
        );

        assertThatThrownBy(() -> validator.validateAllocations(allocations))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate category allocation keys found.");
    }

    @Test
    void validateAllocations_shouldThrowWhenDuplicateKeysWithSubCategory() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(30, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(30, "cat-1", "sub-2"),
                new CreateCategoryAllocationRequest(40, "cat-1", "sub-1")
        );

        assertThatThrownBy(() -> validator.validateAllocations(allocations))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate category allocation keys found.");
    }

    @Test
    void validateAllocations_shouldPassWithSingleAllocation() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(100, "cat-1", null)
        );

        validator.validateAllocations(allocations);
        // no exception expected
    }

    @Test
    void allocationKey_shouldReturnCombinedKey() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", "sub-1");

        String key = validator.allocationKey(request);

        assertThat(key).isEqualTo("cat-1::sub-1");
    }

    @Test
    void allocationKey_shouldReturnCombinedKeyWithNullSubCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", null);

        String key = validator.allocationKey(request);

        assertThat(key).isEqualTo("cat-1::null");
    }
}
