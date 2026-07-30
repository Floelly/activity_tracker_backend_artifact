package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryAllocationRepositoryIT extends MySQLContainerInitializer {

    @Autowired
    private CategoryAllocationRepository categoryAllocationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ActivityRepository activityRepository;


    @Test
    void existsByCategory_shouldReturnTrue_whenAllocationExistsForCategory() {
        Category category = new Category();
        category.setBusinessId("0123456789ABC");
        category.setName("Sport");
        categoryRepository.save(category);

        Activity activity = new Activity();
        activity.setBusinessId("0123456789ABD");
        activity.setTitle("Morning Run");
        activity.setStartAt(Instant.parse("2026-05-25T08:00:00Z"));
        activity.setEndAt(Instant.parse("2026-05-25T09:00:00Z"));
        activity.setCreatedAt(Instant.parse("2026-05-25T07:00:00Z"));
        activityRepository.save(activity);

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setPercentage(100);
        allocation.setCategory(category);
        allocation.setActivity(activity);
        categoryAllocationRepository.save(allocation);

        boolean exists = categoryAllocationRepository.existsByCategory(category);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCategory_shouldReturnFalse_whenAllocationDoesNotExistForCategory() {
        Category category = new Category();
        category.setBusinessId("0123456789ABC");
        category.setName("Sport");
        categoryRepository.save(category);

        boolean exists = categoryAllocationRepository.existsByCategory(category);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByCategory_shouldReturnFalse_whenOnlyOtherCategoryHasAllocation() {
        Category category = new Category();
        category.setBusinessId("0123456789ABC");
        category.setName("Sport");
        categoryRepository.save(category);

        Category otherCategory = new Category();
        otherCategory.setBusinessId("0123456789ABE");
        otherCategory.setName("Work");
        categoryRepository.save(otherCategory);

        Activity activity = new Activity();
        activity.setBusinessId("0123456789ABD");
        activity.setTitle("Morning Run");
        activity.setStartAt(Instant.parse("2026-05-25T08:00:00Z"));
        activity.setEndAt(Instant.parse("2026-05-25T09:00:00Z"));
        activity.setCreatedAt(Instant.parse("2026-05-25T07:00:00Z"));
        activityRepository.save(activity);

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setPercentage(100);
        allocation.setCategory(otherCategory);
        allocation.setActivity(activity);
        categoryAllocationRepository.save(allocation);

        boolean exists = categoryAllocationRepository.existsByCategory(category);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFail() {
        fail("For testing purposes only.");
    }
}