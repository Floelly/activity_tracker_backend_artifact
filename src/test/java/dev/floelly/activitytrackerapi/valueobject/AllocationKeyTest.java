package dev.floelly.activitytrackerapi.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationKeyTest {

    @Test
    void of_shouldCreateAllocationKeyWithCategoryIdAndSubCategoryId() {
        AllocationKey key = AllocationKey.of("cat-1", "sub-1");

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isEqualTo("sub-1");
    }

    @Test
    void of_shouldCreateAllocationKeyWithCategoryIdAndNullSubCategoryId() {
        AllocationKey key = AllocationKey.of("cat-1", null);

        assertThat(key.categoryId()).isEqualTo("cat-1");
        assertThat(key.subCategoryId()).isNull();
    }

    @Test
    void equals_shouldBeTrueWhenSameCategoryIdAndSubCategoryId() {
        AllocationKey key1 = AllocationKey.of("cat-1", "sub-1");
        AllocationKey key2 = AllocationKey.of("cat-1", "sub-1");

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void equals_shouldBeFalseWhenDifferentCategoryId() {
        AllocationKey key1 = AllocationKey.of("cat-1", "sub-1");
        AllocationKey key2 = AllocationKey.of("cat-2", "sub-1");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void equals_shouldBeFalseWhenDifferentSubCategoryId() {
        AllocationKey key1 = AllocationKey.of("cat-1", "sub-1");
        AllocationKey key2 = AllocationKey.of("cat-1", "sub-2");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void hashCode_shouldBeEqualWhenSameCategoryIdAndSubCategoryId() {
        AllocationKey key1 = AllocationKey.of("cat-1", "sub-1");
        AllocationKey key2 = AllocationKey.of("cat-1", "sub-1");

        assertThat(key1).hasSameHashCodeAs(key2);
    }

    @Test
    void hashCode_shouldBeDifferentWhenDifferentCategoryId() {
        AllocationKey key1 = AllocationKey.of("cat-1", "sub-1");
        AllocationKey key2 = AllocationKey.of("cat-2", "sub-1");

        assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode());
    }

    @Test
    void of_shouldPreserveNullSubCategoryId() {
        AllocationKey key = AllocationKey.of("cat-1", null);

        assertThat(key.subCategoryId()).isNull();
        assertThat(key.categoryId()).isEqualTo("cat-1");
    }
}