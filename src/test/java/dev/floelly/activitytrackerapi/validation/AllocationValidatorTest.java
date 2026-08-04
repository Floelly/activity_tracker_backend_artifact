package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AllocationValidatorTest {

    private final AllocationValidator validator = new AllocationValidator();

    @Test
    void validateAllocationSum_shouldPass_whenSumIs100() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(60, "cat-1", null),
                new CreateCategoryAllocationRequest(40, "cat-2", null)
        );

        validator.validateAllocationSum(allocations);
    }

    @Test
    void validateAllocationSum_shouldThrow_whenSumIsNot100() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(60, "cat-1", null),
                new CreateCategoryAllocationRequest(30, "cat-2", null)
        );

        assertThatThrownBy(() -> validator.validateAllocationSum(allocations))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sum of category allocations must be 100%");
    }

    @Test
    void validateAllocations_shouldPass_whenAllKeysAreDistinct() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-2")
        );

        validator.validateAllocations(allocations);
    }

    @Test
    void validateAllocations_shouldThrow_whenKeysAreDuplicated() {
        List<CreateCategoryAllocationRequest> allocations = List.of(
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1"),
                new CreateCategoryAllocationRequest(50, "cat-1", "sub-1")
        );

        assertThatThrownBy(() -> validator.validateAllocations(allocations))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate category allocation keys found.");
    }

    @Test
    void allocationKey_shouldReturnKeyWithCategoryAndSubCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", "sub-1");

        String key = validator.allocationKey(request);

        assertThat(key).isEqualTo("cat-1::sub-1");
    }

    @Test
    void allocationKey_shouldReturnKeyWithCategoryOnly_whenSubCategoryIsNull() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", null);

        String key = validator.allocationKey(request);

        assertThat(key).isEqualTo("cat-1::null");
    }
}