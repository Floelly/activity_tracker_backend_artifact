package dev.floelly.activitytrackerapi.valueobject;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationKeyTest {

    @Test
    void fromCreateCategoryAllocationRequest_shouldCreateKeyWithCategoryAndSubCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", "sub-1");

        AllocationKey key = AllocationKey.from(request);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isEqualTo("sub-1");
    }

    @Test
    void fromCreateCategoryAllocationRequest_shouldCreateKeyWithCategoryAndNullSubCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", null);

        AllocationKey key = AllocationKey.from(request);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isNull();
    }

    @Test
    void fromCategoryAllocation_shouldCreateKeyWithCategoryAndSubCategory() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        SubCategory subCategory = new SubCategory();
        subCategory.setBusinessId("sub-1");

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setCategory(category);
        allocation.setSubCategory(subCategory);

        AllocationKey key = AllocationKey.from(allocation);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isEqualTo("sub-1");
    }

    @Test
    void fromCategoryAllocation_shouldCreateKeyWithCategoryAndNullSubCategory() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setCategory(category);
        allocation.setSubCategory(null);

        AllocationKey key = AllocationKey.from(allocation);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isNull();
    }

    @Test
    void equals_shouldBeTrueForSameCategoryAndSubCategory() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-1", "sub-1");

        assertThat(key1).isEqualTo(key2);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    void equals_shouldBeTrueForSameCategoryAndNullSubCategory() {
        AllocationKey key1 = new AllocationKey("cat-1", null);
        AllocationKey key2 = new AllocationKey("cat-1", null);

        assertThat(key1).isEqualTo(key2);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    void equals_shouldBeFalseForDifferentCategory() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-2", "sub-1");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void equals_shouldBeFalseForDifferentSubCategory() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-1", "sub-2");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void equals_shouldBeFalseWhenOneHasNullSubCategory() {
        AllocationKey key1 = new AllocationKey("cat-1", null);
        AllocationKey key2 = new AllocationKey("cat-1", "sub-1");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void equals_shouldBeFalseForNull() {
        AllocationKey key = new AllocationKey("cat-1", "sub-1");

        assertThat(key).isNotEqualTo(null);
    }

    @Test
    void hashCode_shouldBeConsistentWithEquals() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-1", "sub-1");

        Set<AllocationKey> set = new HashSet<>();
        set.add(key1);

        assertThat(set).contains(key2);
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentKeys() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-1", "sub-2");

        Set<AllocationKey> set = new HashSet<>();
        set.add(key1);

        assertThat(set).doesNotContain(key2);
    }

    @Test
    void toString_shouldReturnFormattedStringWithCategoryAndSubCategory() {
        AllocationKey key = new AllocationKey("cat-1", "sub-1");

        assertThat(key).hasToString("cat-1::sub-1");
    }

    @Test
    void toString_shouldReturnFormattedStringWithCategoryAndNullSubCategory() {
        AllocationKey key = new AllocationKey("cat-1", null);

        assertThat(key).hasToString("cat-1::null");
    }

    @Test
    void record_shouldBeImmutable() {
        AllocationKey key = new AllocationKey("cat-1", "sub-1");

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isEqualTo("sub-1");
    }
}
