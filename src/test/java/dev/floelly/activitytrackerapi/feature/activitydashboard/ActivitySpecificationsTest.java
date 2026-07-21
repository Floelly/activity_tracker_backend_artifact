package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import dev.floelly.activitytrackerapi.repository.MySQLContainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActivitySpecificationsTest extends MySQLContainerInitializer {

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    private ActivityRepository activityRepository;

    @Test
    void withFilter_shouldReturnActivitiesOverlappingTimeRange() {
        Activity activity = createActivity("2026-06-01T06:00:00Z", "2026-06-01T08:00:00Z");
        activityRepository.save(activity);

        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T05:00:00Z"),
                Instant.parse("2026-06-02T09:00:00Z"),
                TimeGranularity.DAY,
                null
        );
        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(activity);
    }

    @Test
    void withFilter_shouldNotReturnActivitiesBeforeAndAfterTimeRange() {
        Activity activity1 = createActivity("2026-06-01T06:00:00Z", "2026-06-01T08:00:00Z");
        Activity activity2 = createActivity("2026-06-01T10:00:00Z", "2026-06-01T12:00:00Z");
        activityRepository.saveAll(List.of(activity1, activity2));

        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T08:00:00Z"),
                Instant.parse("2026-06-01T10:00:00Z"),
                TimeGranularity.DAY,
                null
        );
        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).isEmpty();
    }

    @Test
    void withFilter_shouldReturnActivitiesStraddlingPeriodBoundary() {
        Activity activity1 = createActivity("2026-06-01T06:00:00Z", "2026-06-01T08:00:00Z");
        Activity activity2 = createActivity("2026-06-01T10:00:00Z", "2026-06-01T12:00:00Z");
        Activity activity3 = createActivity("2026-06-01T06:00:00Z", "2026-06-01T12:00:00Z");
        activityRepository.saveAll(List.of(activity1, activity2, activity3));

        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T07:00:00Z"),
                Instant.parse("2026-06-02T11:00:00Z"),
                TimeGranularity.DAY,
                null
        );
        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(activity1, activity2, activity3);
    }

    @Test
    void withFilter_shouldFilterByCategoryIds() {
        Activity activity1 = createActivity("2026-06-01T06:00:00Z", "2026-06-01T08:00:00Z");
        Category category1 = createCategory();
        addCategoryAllocation(activity1, category1, 100);
        Activity activity2 = createActivity("2026-06-01T10:00:00Z", "2026-06-01T12:00:00Z");
        Category category2 = createCategory();
        addCategoryAllocation(activity2, category2, 100);
        activityRepository.saveAll(List.of(activity1, activity2));

        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-03T00:00:00Z"),
                TimeGranularity.DAY,
                List.of(category1.getBusinessId())
        );
        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result)
                .containsExactlyInAnyOrder(activity1);
    }

    @Test
    void withFilter_shouldReturnActivitiesWithMultipleCategoryAllocationsMatchingFilter() {
        Activity activity = createActivity("2026-06-01T00:00:00Z", "2026-06-02T00:00:00Z");
        Category category = createCategory();
        addCategoryAllocation(activity, category, 60);
        addCategoryAllocation(activity, createCategory(), 40);
        activityRepository.save(activity);

        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                List.of(category.getBusinessId())
        );
        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo(activity.getBusinessId());
    }

    @Test
    void withFilter_shouldReturnNoDuplicateActivities_whenMultipleCategoryAllocationsMatchFilter() {
        Activity activity = createActivity("2026-06-01T00:00:00Z", "2026-06-02T00:00:00Z");
        Category category1 = createCategory();
        addCategoryAllocation(activity, category1, 60);
        Category category2 = createCategory();
        addCategoryAllocation(activity, category2, 40);
        activityRepository.save(activity);

        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                List.of(category1.getBusinessId(), category2.getBusinessId())
        );
        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo(activity.getBusinessId());
    }

    @Test
    void withFilter_shouldReturnAllActivities_whenNoCategoryFilter() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-03T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Activity activity1 = createActivity("2026-06-01T06:00:00Z", "2026-06-01T08:00:00Z");
        addCategoryAllocation(activity1, createCategory(), 100);

        Activity activity2 = createActivity("2026-06-01T10:00:00Z", "2026-06-01T12:00:00Z");
        addCategoryAllocation(activity2, createCategory(), 100);

        activityRepository.saveAll(List.of(activity1, activity2));

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(activity1, activity2);
    }

    private Activity createActivity(String startAt, String endAt) {
        Activity activity = new Activity();
        activity.setBusinessId(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
        activity.setTitle("Test Activity");
        activity.setStartAt(Instant.parse(startAt));
        activity.setEndAt(Instant.parse(endAt));
        activity.setCategoryAllocations(new HashSet<>());
        activity.setTags(new HashSet<>());
        activity.setAttributes(new HashSet<>());
        activity.setCreatedAt(Instant.parse("2026-05-25T07:00:00Z"));
        return activity;
    }

    private void addCategoryAllocation(Activity activity, Category category, int percentage) {
        CategoryAllocation categoryAllocation = new CategoryAllocation();
        categoryAllocation.setActivity(activity);
        categoryAllocation.setCategory(category);
        categoryAllocation.setPercentage(percentage);

        activity.getCategoryAllocations().add(categoryAllocation);
    }

    private Category createCategory() {
        Category category = new Category();
        category.setName("irrelevant");
        category.setBusinessId(randomBusinessId());
        categoryRepository.save(category);
        return category;
    }

    private String randomBusinessId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }
}