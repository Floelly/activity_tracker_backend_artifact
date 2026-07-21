package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.dto.request.ActivityFilterDTO;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActivitySpecificationTest extends MySQLContainerInitializer {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findAll_withFilter_shouldReturnActivitiesMatchingFromStartAt() {
        Activity activity1 = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        Activity activity2 = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");
        Activity activity3 = createActivity("2026-05-25T12:00:00Z", "2026-05-25T13:00:00Z");

        activityRepository.saveAll(List.of(activity1, activity2, activity3));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T09:00:00Z"),
                null,
                null
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getStartAt)
                .containsExactlyInAnyOrder(
                        Instant.parse("2026-05-25T10:00:00Z"),
                        Instant.parse("2026-05-25T12:00:00Z")
                );
    }

    @Test
    void findAll_withFilter_shouldReturnActivitiesMatchingToStartAt() {
        Activity activity1 = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        Activity activity2 = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");
        Activity activity3 = createActivity("2026-05-25T12:00:00Z", "2026-05-25T13:00:00Z");

        activityRepository.saveAll(List.of(activity1, activity2, activity3));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                Instant.parse("2026-05-25T11:00:00Z"),
                null
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getStartAt)
                .containsExactlyInAnyOrder(
                        Instant.parse("2026-05-25T08:00:00Z"),
                        Instant.parse("2026-05-25T10:00:00Z")
                );
    }

    @Test
    void findAll_withFilter_shouldReturnActivitiesMatchingFromAndToStartAt() {
        Activity activity1 = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        Activity activity2 = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");
        Activity activity3 = createActivity("2026-05-25T12:00:00Z", "2026-05-25T13:00:00Z");

        activityRepository.saveAll(List.of(activity1, activity2, activity3));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T09:00:00Z"),
                Instant.parse("2026-05-25T11:00:00Z"),
                null
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result)
                .extracting(Activity::getStartAt)
                .containsExactly(Instant.parse("2026-05-25T10:00:00Z"));
    }

    @Test
    void findAll_withFilter_shouldReturnEmptyWhenNoActivitiesMatch() {
        Activity activity = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        activityRepository.save(activity);

        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T10:00:00Z"),
                null
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_withEmptyFilter_shouldReturnAllActivities() {
        Activity activity1 = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        Activity activity2 = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");

        activityRepository.saveAll(List.of(activity1, activity2));

        ActivityFilterDTO filter = new ActivityFilterDTO(null, null, null);

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_withFilter_shouldReturnActivitiesMatchingSingleCategoryBusinessId() {
        Category sports = createCategory();

        Activity matchingActivity = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        addCategoryAllocation(matchingActivity, sports, 100);

        Activity nonMatchingActivity = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");
        addCategoryAllocation(nonMatchingActivity, createCategory(), 100);

        Activity activityWithoutCategory = createActivity("2026-05-25T12:00:00Z", "2026-05-25T13:00:00Z");

        activityRepository.saveAll(List.of(matchingActivity, nonMatchingActivity, activityWithoutCategory));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of(sports.getBusinessId())
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result)
                .extracting(Activity::getBusinessId)
                .containsExactly(matchingActivity.getBusinessId());
    }

    @Test
    void findAll_withFilter_shouldReturnActivitiesMatchingAnyCategoryBusinessId() {
        Category sports = createCategory();
        Category work = createCategory();

        Activity sportsActivity = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        addCategoryAllocation(sportsActivity, sports, 100);

        Activity workActivity = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");
        addCategoryAllocation(workActivity, work, 100);

        Activity nonMatchingActivity = createActivity("2026-05-25T12:00:00Z", "2026-05-25T13:00:00Z");
        addCategoryAllocation(nonMatchingActivity, createCategory(), 100);

        activityRepository.saveAll(List.of(sportsActivity, workActivity, nonMatchingActivity));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of(sports.getBusinessId(), work.getBusinessId())
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getBusinessId)
                .containsExactlyInAnyOrder(
                        sportsActivity.getBusinessId(),
                        workActivity.getBusinessId()
                );
    }

    @Test
    void findAll_withFilter_shouldReturnActivitiesMatchingCategoryAndDateRange() {
        Category sports = createCategory();
        Category work = createCategory();

        Activity matchingActivity = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");
        addCategoryAllocation(matchingActivity, sports, 100);

        Activity wrongDateActivity = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        addCategoryAllocation(wrongDateActivity, sports, 100);

        Activity wrongCategoryActivity = createActivity("2026-05-25T10:30:00Z", "2026-05-25T11:30:00Z");
        addCategoryAllocation(wrongCategoryActivity, work, 100);

        activityRepository.saveAll(List.of(matchingActivity, wrongDateActivity, wrongCategoryActivity));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T09:00:00Z"),
                Instant.parse("2026-05-25T11:00:00Z"),
                List.of(sports.getBusinessId())
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result)
                .extracting(Activity::getBusinessId)
                .containsExactly(matchingActivity.getBusinessId());
    }

    @Test
    void findAll_withFilter_shouldReturnActivityOnlyOnceWhenMultipleCategoryAllocationsMatch() {
        Category sports = createCategory();
        Category run = createCategory();

        Activity matchingActivity = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");
        addCategoryAllocation(matchingActivity, sports, 60);
        addCategoryAllocation(matchingActivity, run, 40);

        Activity nonMatchingActivity = createActivity("2026-05-25T12:00:00Z", "2026-05-25T13:00:00Z");
        addCategoryAllocation(nonMatchingActivity, createCategory(), 100);

        activityRepository.saveAll(List.of(matchingActivity, nonMatchingActivity));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of(sports.getBusinessId(), run.getBusinessId())
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBusinessId()).isEqualTo(matchingActivity.getBusinessId());
    }

    @Test
    void findAll_withFilter_shouldReturnEmptyWhenCategoryDoesNotMatch() {
        Activity activity = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        addCategoryAllocation(activity, createCategory(), 100);

        activityRepository.save(activity);

        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of("work123456789")
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_withFilter_shouldIgnoreCategoryWhenCategoryListIsEmpty() {
        Activity activity1 = createActivity("2026-05-25T08:00:00Z", "2026-05-25T09:00:00Z");
        addCategoryAllocation(activity1, createCategory(), 100);

        Activity activity2 = createActivity("2026-05-25T10:00:00Z", "2026-05-25T11:00:00Z");

        activityRepository.saveAll(List.of(activity1, activity2));

        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of()
        );

        List<Activity> result = activityRepository.findAll(ActivitySpecifications.withFilter(filter));

        assertThat(result).hasSize(2);
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