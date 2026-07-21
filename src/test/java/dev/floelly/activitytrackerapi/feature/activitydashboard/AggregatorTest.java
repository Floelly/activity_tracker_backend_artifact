package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AggregatorTest {

    private final Aggregator aggregator = new Aggregator();

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldReturnEmptyMap_whenNoActivities() {
        List<Activity> activities = List.of();
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        assertThat(result).isEmpty();
    }

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldReturnEmptyMap_whenNoPeriods() {
        List<Activity> activities = List.of(createActivity(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                "cat-1",
                100
        ));
        List<PeriodKey> periods = List.of();

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        assertThat(result).isEmpty();
    }

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldSumSecondsWhenActivityFullyInsidePeriod() {
        List<Activity> activities = List.of(createActivity(
                Instant.parse("2026-06-01T06:00:00Z"),
                Instant.parse("2026-06-01T08:00:00Z"),
                "cat-1",
                100
        ));
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        PeriodKey period = periods.getFirst();
        assertThat(result).containsKey(period);
        assertThat(result.get(period)).containsEntry("cat-1", 7200L);
    }

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldCalculateOverlapForActivityStraddlingPeriodBoundary() {
        List<Activity> activities = List.of(createActivity(
                Instant.parse("2026-06-01T22:00:00Z"),
                Instant.parse("2026-06-02T02:00:00Z"),
                "cat-1",
                100
        ));
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z")),
                new PeriodKey(Instant.parse("2026-06-02T00:00:00Z"), Instant.parse("2026-06-03T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        assertThat(result.get(periods.get(0))).containsEntry("cat-1", 7200L);
        assertThat(result.get(periods.get(1))).containsEntry("cat-1", 7200L);
    }

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldHandleMultipleActivitiesSameCategory() {
        List<Activity> activities = List.of(
                createActivity(
                        Instant.parse("2026-06-01T06:00:00Z"),
                        Instant.parse("2026-06-01T08:00:00Z"),
                        "cat-1",
                        100
                ),
                createActivity(
                        Instant.parse("2026-06-01T10:00:00Z"),
                        Instant.parse("2026-06-01T12:00:00Z"),
                        "cat-1",
                        100
                )
        );
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        PeriodKey period = periods.getFirst();
        assertThat(result.get(period)).containsEntry("cat-1", 14400L);
    }

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldHandleMultipleCategoriesWithPercentage() {
        List<Activity> activities = List.of(createActivityWithMultipleCategories(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-01T02:00:00Z"),
                Map.of("cat-1", 60, "cat-2", 40)
        ));
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        PeriodKey period = periods.getFirst();
        assertThat(result.get(period)).containsEntry("cat-1", 4320L);
        assertThat(result.get(period)).containsEntry("cat-2", 2880L);
    }

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldHandleActivityNotOverlappingPeriod() {
        List<Activity> activities = List.of(createActivity(
                Instant.parse("2026-06-05T00:00:00Z"),
                Instant.parse("2026-06-06T00:00:00Z"),
                "cat-1",
                100
        ));
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        assertThat(result).isEmpty();
    }

    @Test
    void aggregateSecondsByPeriodAndCategory_shouldSplitActivityAcrossMultiplePeriods() {
        List<Activity> activities = List.of(createActivity(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-03T00:00:00Z"),
                "cat-1",
                100
        ));
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z")),
                new PeriodKey(Instant.parse("2026-06-02T00:00:00Z"), Instant.parse("2026-06-03T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        assertThat(result.get(periods.get(0))).containsEntry("cat-1", 86400L);
        assertThat(result.get(periods.get(1))).containsEntry("cat-1", 86400L);
    }

    @ParameterizedTest(name = "minutes = 1, percentage = {0}, expectedSeconds = 2")
    @ValueSource(ints = {3, 4})
    void aggregateSecondsByPeriodAndCategory_shouldHandlePercentageRounding(int percentage) {
        List<Activity> activities = List.of(createActivity(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:01:00Z"),
                "test-cat",
                percentage
        ));
        List<PeriodKey> periods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );

        Map<PeriodKey, Map<String, Long>> result = aggregator.aggregateSecondsByPeriodAndCategory(activities, periods);

        PeriodKey period = periods.getFirst();
        assertThat(result.get(period)).containsEntry("test-cat", 2L);
    }

    private Activity createActivity(Instant start, Instant end, String categoryId, int percentage) {
        Activity activity = new Activity();
        activity.setStartAt(start);
        activity.setEndAt(end);

        Category category = new Category();
        category.setBusinessId(categoryId);

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setCategory(category);
        allocation.setPercentage(percentage);
        activity.setCategoryAllocations(Set.of(allocation));

        return activity;
    }

    private Activity createActivityWithMultipleCategories(Instant start, Instant end, Map<String, Integer> categories) {
        Activity activity = new Activity();
        activity.setStartAt(start);
        activity.setEndAt(end);

        Set<CategoryAllocation> allocations = categories.entrySet().stream()
                .map(entry -> {
                    Category category = new Category();
                    category.setBusinessId(entry.getKey());

                    CategoryAllocation allocation = new CategoryAllocation();
                    allocation.setCategory(category);
                    allocation.setPercentage(entry.getValue());
                    return allocation;
                })
                .collect(Collectors.toSet());

        activity.setCategoryAllocations(allocations);
        return activity;
    }
}