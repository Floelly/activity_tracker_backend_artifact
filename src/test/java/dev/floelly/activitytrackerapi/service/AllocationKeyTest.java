package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationKeyTest {

    @Test
    void shouldBeEqual_whenSameCategoryIdAndSubCategoryId() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-1", "sub-1");

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void shouldBeEqual_whenBothHaveNullSubCategoryId() {
        AllocationKey key1 = new AllocationKey("cat-1", null);
        AllocationKey key2 = new AllocationKey("cat-1", null);

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void shouldNotBeEqual_whenDifferentCategoryId() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-2", "sub-1");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldNotBeEqual_whenDifferentSubCategoryId() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-1", "sub-2");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldNotBeEqual_whenOneHasNullSubCategoryIdAndOtherHasNonNull() {
        AllocationKey key1 = new AllocationKey("cat-1", null);
        AllocationKey key2 = new AllocationKey("cat-1", "sub-1");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldHaveSameHashCode_whenEqual() {
        AllocationKey key1 = new AllocationKey("cat-1", "sub-1");
        AllocationKey key2 = new AllocationKey("cat-1", "sub-1");

        assertThat(key1).hasSameHashCodeAs(key2);
    }

    @Test
    void shouldBeUsableAsSetElement() {
        Set<AllocationKey> set = new HashSet<>();
        set.add(new AllocationKey("cat-1", "sub-1"));
        set.add(new AllocationKey("cat-1", "sub-1"));

        assertThat(set).hasSize(1);
    }

    @Test
    void shouldBeUsableAsMapKey() {
        Map<AllocationKey, String> map = new HashMap<>();
        AllocationKey key = new AllocationKey("cat-1", "sub-1");
        map.put(key, "value");

        assertThat(map.get(new AllocationKey("cat-1", "sub-1"))).isEqualTo("value");
    }

    @Test
    void shouldCreateFromCreateCategoryAllocationRequest() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(80, "cat-1", "sub-1");
        AllocationKey key = AllocationKey.from(request);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isEqualTo("sub-1");
    }

    @Test
    void shouldCreateFromCreateCategoryAllocationRequest_withNullSubCategoryId() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", null);
        AllocationKey key = AllocationKey.from(request);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isNull();
    }

    @Test
    void shouldCreateFromCategoryAllocation() {
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
    void shouldCreateFromCategoryAllocation_withNullSubCategory() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setCategory(category);
        allocation.setSubCategory(null);

        AllocationKey key = AllocationKey.from(allocation);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isNull();
    }
}