package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.request.StatisticsFilterDTO;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import dev.floelly.activitytrackerapi.repository.MySQLContainerInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StatisticsSpecificationsTest extends MySQLContainerInitializer {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void withFilter_shouldReturnAllActivities_whenFilterIsNull() {
        createAndSaveActivity("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z");
        createAndSaveActivity("act-2", "2031-01-02T10:00:00Z", "2031-01-02T12:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, null);
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(2);
    }

    @Test
    void withFilter_shouldFilterByFromStartAt() {
        createAndSaveActivity("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z");
        createAndSaveActivity("act-2", "2031-01-03T10:00:00Z", "2031-01-03T12:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2031-01-02T00:00:00Z"),
                null,
                null
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo("act-2");
    }

    @Test
    void withFilter_shouldFilterByToStartAt() {
        createAndSaveActivity("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z");
        createAndSaveActivity("act-2", "2031-01-03T10:00:00Z", "2031-01-03T12:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                Instant.parse("2031-01-02T00:00:00Z"),
                null
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo("act-1");
    }

    @Test
    void withFilter_shouldFilterByDateRange() {
        createAndSaveActivity("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z");
        createAndSaveActivity("act-2", "2031-01-15T10:00:00Z", "2031-01-15T12:00:00Z");
        createAndSaveActivity("act-3", "2031-02-01T10:00:00Z", "2031-02-01T13:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2031-01-10T00:00:00Z"),
                Instant.parse("2031-01-31T23:59:59Z"),
                null
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo("act-2");
    }

    @Test
    void withFilter_shouldFilterByCategory() {
        Category category = createAndSaveCategory("cat-1");
        createAndSaveActivityWithCategory("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z", category);
        createAndSaveActivity("act-2", "2031-01-02T10:00:00Z", "2031-01-02T12:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("cat-1")
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo("act-1");
    }

    @Test
    void withFilter_shouldFilterByMultipleCategories() {
        Category categoryA = createAndSaveCategory("cat-a");
        Category categoryB = createAndSaveCategory("cat-b");
        Category categoryC = createAndSaveCategory("cat-c");

        createAndSaveActivityWithCategory("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z", categoryA);
        createAndSaveActivityWithCategory("act-2", "2031-01-02T10:00:00Z", "2031-01-02T12:00:00Z", categoryB);
        createAndSaveActivityWithCategory("act-3", "2031-01-03T10:00:00Z", "2031-01-03T13:00:00Z", categoryC);

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("cat-a", "cat-b")
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Activity::getBusinessId).containsExactlyInAnyOrder("act-1", "act-2");
    }

    @Test
    void withFilter_shouldFilterByDateRangeAndCategory() {
        Category category = createAndSaveCategory("cat-1");

        createAndSaveActivityWithCategory("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z", category);
        createAndSaveActivityWithCategory("act-2", "2031-01-15T10:00:00Z", "2031-01-15T12:00:00Z", category);
        createAndSaveActivity("act-3", "2031-01-20T10:00:00Z", "2031-01-20T13:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2031-01-10T00:00:00Z"),
                Instant.parse("2031-01-31T23:59:59Z"),
                List.of("cat-1")
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo("act-2");
    }

    @Test
    void withFilter_shouldReturnEmpty_whenNoActivityMatchesDateRange() {
        createAndSaveActivity("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2050-01-01T00:00:00Z"),
                Instant.parse("2050-12-31T23:59:59Z"),
                null
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void withFilter_shouldReturnEmpty_whenNoActivityMatchesCategory() {
        createAndSaveCategory("cat-1");
        createAndSaveActivity("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("cat-1")
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void withFilter_shouldReturnDistinctActivities_whenFilteringByCategory() {
        Category category = createAndSaveCategory("cat-1");

        // Activity with multiple allocations to the same category - should be returned only once
        Activity activity = createActivity("act-1", "2031-01-01T10:00:00Z", "2031-01-01T11:00:00Z");

        CategoryAllocation allocation1 = new CategoryAllocation();
        allocation1.setPercentage(50);
        allocation1.setCategory(category);
        allocation1.setActivity(activity);

        CategoryAllocation allocation2 = new CategoryAllocation();
        allocation2.setPercentage(50);
        allocation2.setCategory(category);
        allocation2.setActivity(activity);

        activity.setCategoryAllocations(Set.of(allocation1, allocation2));
        activityRepository.save(activity);

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("cat-1")
        );
        Specification<Activity> spec = StatisticsSpecifications.withFilter(filter);

        List<Activity> result = activityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo("act-1");
    }

    // ============ Helper methods ============

    private Activity createActivity(String businessId, String startAt, String endAt) {
        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setTitle("Test Activity " + businessId);
        activity.setStartAt(Instant.parse(startAt));
        activity.setEndAt(Instant.parse(endAt));
        activity.setCreatedAt(Instant.now());
        return activity;
    }

    private Activity createAndSaveActivity(String businessId, String startAt, String endAt) {
        Activity activity = createActivity(businessId, startAt, endAt);
        activity.setCategoryAllocations(Set.of());
        return activityRepository.save(activity);
    }

    private Activity createAndSaveActivityWithCategory(String businessId, String startAt, String endAt, Category category) {
        Activity activity = createActivity(businessId, startAt, endAt);

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setPercentage(100);
        allocation.setCategory(category);
        allocation.setActivity(activity);

        activity.setCategoryAllocations(Set.of(allocation));
        return activityRepository.save(activity);
    }

    private Category createAndSaveCategory(String businessId) {
        Category category = new Category();
        category.setBusinessId(businessId);
        category.setName("Category " + businessId);
        return categoryRepository.save(category);
    }
}