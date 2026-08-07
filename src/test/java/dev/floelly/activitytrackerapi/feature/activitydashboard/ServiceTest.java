package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.*;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTest {

    @Captor
    private ArgumentCaptor<List<Category>> categoriesCaptor;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Aggregator aggregator;

    @Mock
    private PeriodFactory periodFactory;

    @Mock
    private ResponseMapper responseMapper;

    @InjectMocks
    private Service service;

    @Test
    void getActivitiesDashboardResponse_shouldThrowNotFoundException_whenRequestedCategoryIsMissing() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                List.of("cat-1", "cat-2")
        );

        when(categoryRepository.findAllByBusinessIdInAndDeletedAtIsNull(filter.category()))
                .thenReturn(List.of(createCategory("cat-1", "Sport")));

        assertThrows(NotFoundException.class, () -> service.getActivitiesDashboardResponse(filter));

        verify(categoryRepository).findAllByBusinessIdInAndDeletedAtIsNull(filter.category());
        verifyNoInteractions(activityRepository, aggregator, periodFactory, responseMapper);
    }

    @Test
    void getActivitiesDashboardResponse_shouldThrowNotFoundException_whenRequestedCategoryIsDeleted() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                List.of("cat-deleted")
        );

        Category deletedCategory = createCategory("cat-deleted", "Old Category");
        deletedCategory.setDeletedAt(Instant.parse("2026-06-01T00:00:00Z"));

        when(categoryRepository.findAllByBusinessIdInAndDeletedAtIsNull(filter.category()))
                .thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> service.getActivitiesDashboardResponse(filter));

        verify(categoryRepository).findAllByBusinessIdInAndDeletedAtIsNull(filter.category());
        verifyNoInteractions(activityRepository, aggregator, periodFactory, responseMapper);
    }

    @Test
    void getActivitiesDashboardResponse_shouldUseRequestedCategoriesForSummary_whenCategoryFilterPresent() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                List.of("cat-1", "cat-2")
        );

        Category category1 = createCategory("cat-1", "Sport");
        Category category2 = createCategory("cat-2", "Work");
        List<Category> requestedCategories = List.of(category1, category2);

        List<Activity> activities = List.of(createActivity("activity-1", category1));
        List<PeriodKey> orderedPeriods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of();
        SummaryResponse summary = new SummaryResponse(10, 10, List.of(
                new CategoryResponse("cat-2", "Work", 6, 0.6f, null, null),
                new CategoryResponse("cat-1", "Sport", 4, 0.4f, null, null)
        ));
        TimeSeriesResponse timeSeries = new TimeSeriesResponse(TimeGranularity.DAY, List.of());

        when(categoryRepository.findAllByBusinessIdInAndDeletedAtIsNull(filter.category())).thenReturn(requestedCategories);
        when(activityRepository.findAll(any(Specification.class))).thenReturn(activities);
        when(periodFactory.buildPeriods(filter)).thenReturn(orderedPeriods);
        when(aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods)).thenReturn(aggregated);
        when(responseMapper.buildSummary(requestedCategories, aggregated, orderedPeriods.size())).thenReturn(summary);
        when(responseMapper.buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), anyList())).thenReturn(timeSeries);

        Response result = service.getActivitiesDashboardResponse(filter);

        verify(activityRepository).findAll(any(Specification.class));
        verify(responseMapper).buildSummary(requestedCategories, aggregated, 1);
        assertThat(result.filters()).isEqualTo(filter);
        assertThat(result.summary()).isEqualTo(summary);
        assertThat(result.timeSeries()).isEqualTo(timeSeries);
    }

    @Test
    void getActivitiesDashboardResponse_shouldResolveRelevantCategoriesFromActivities_whenNoCategoryFilter() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Category category1 = createCategory("cat-1", "Sport");
        Category category2 = createCategory("cat-2", "Work");

        Activity activity1 = createActivity("activity-1", category1);
        Activity activity2 = createActivity("activity-2", category2);

        List<Activity> activities = List.of(activity1, activity2);
        List<PeriodKey> orderedPeriods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of();
        SummaryResponse summary = new SummaryResponse(10, 10, List.of());
        TimeSeriesResponse timeSeries = new TimeSeriesResponse(TimeGranularity.DAY, List.of());

        when(activityRepository.findAll(any(Specification.class))).thenReturn(activities);
        when(periodFactory.buildPeriods(filter)).thenReturn(orderedPeriods);
        when(aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods)).thenReturn(aggregated);
        when(responseMapper.buildSummary(anyList(), eq(aggregated), eq(1))).thenReturn(summary);
        when(responseMapper.buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), anyList())).thenReturn(timeSeries);

        service.getActivitiesDashboardResponse(filter);

        verify(responseMapper).buildSummary(categoriesCaptor.capture(), eq(aggregated), eq(1));

        assertThat(categoriesCaptor.getValue())
                .extracting(Category::getBusinessId)
                .containsExactlyInAnyOrder("cat-1", "cat-2");
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getActivitiesDashboardResponse_shouldExcludeDeletedCategories_whenResolvingRelevantCategories() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Category activeCategory = createCategory("cat-active", "Active");
        Category deletedCategory = createCategory("cat-deleted", "Deleted");
        deletedCategory.setDeletedAt(Instant.parse("2026-06-01T00:00:00Z"));

        Activity activity1 = createActivity("activity-1", activeCategory);
        Activity activity2 = createActivity("activity-2", deletedCategory);

        List<Activity> activities = List.of(activity1, activity2);
        List<PeriodKey> orderedPeriods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of();
        SummaryResponse summary = new SummaryResponse(10, 10, List.of());
        TimeSeriesResponse timeSeries = new TimeSeriesResponse(TimeGranularity.DAY, List.of());

        when(activityRepository.findAll(any(Specification.class))).thenReturn(activities);
        when(periodFactory.buildPeriods(filter)).thenReturn(orderedPeriods);
        when(aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods)).thenReturn(aggregated);
        when(responseMapper.buildSummary(anyList(), eq(aggregated), eq(1))).thenReturn(summary);
        when(responseMapper.buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), anyList())).thenReturn(timeSeries);

        service.getActivitiesDashboardResponse(filter);

        verify(responseMapper).buildSummary(categoriesCaptor.capture(), eq(aggregated), eq(1));

        assertThat(categoriesCaptor.getValue())
                .extracting(Category::getBusinessId)
                .containsExactly("cat-active");
    }

    @Test
    void getActivitiesDashboardResponse_shouldDeduplicateRelevantCategories_whenSameCategoryOccursMultipleTimes() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Category category1 = createCategory("cat-1", "Sport");

        Activity activity1 = createActivity("activity-1", category1);
        Activity activity2 = createActivity("activity-2", category1);

        List<Activity> activities = List.of(activity1, activity2);
        List<PeriodKey> orderedPeriods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of();
        SummaryResponse summary = new SummaryResponse(10, 10, List.of());
        TimeSeriesResponse timeSeries = new TimeSeriesResponse(TimeGranularity.DAY, List.of());

        when(activityRepository.findAll(any(Specification.class))).thenReturn(activities);
        when(periodFactory.buildPeriods(filter)).thenReturn(orderedPeriods);
        when(aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods)).thenReturn(aggregated);
        when(responseMapper.buildSummary(anyList(), eq(aggregated), eq(1))).thenReturn(summary);
        when(responseMapper.buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), anyList())).thenReturn(timeSeries);

        service.getActivitiesDashboardResponse(filter);

        verify(responseMapper).buildSummary(categoriesCaptor.capture(), eq(aggregated), eq(1));

        assertThat(categoriesCaptor.getValue())
                .extracting(Category::getBusinessId)
                .containsExactly("cat-1");
    }

    @Test
    void getActivitiesDashboardResponse_shouldPassActivitiesAndPeriodsToAggregator() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Category category = createCategory("cat-1", "Sport");
        List<Activity> activities = List.of(createActivity("activity-1", category));
        List<PeriodKey> orderedPeriods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of();
        SummaryResponse summary = new SummaryResponse(10, 10, List.of());
        TimeSeriesResponse timeSeries = new TimeSeriesResponse(TimeGranularity.DAY, List.of());

        when(activityRepository.findAll(any(Specification.class))).thenReturn(activities);
        when(periodFactory.buildPeriods(filter)).thenReturn(orderedPeriods);
        when(aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods)).thenReturn(aggregated);
        when(responseMapper.buildSummary(anyList(), eq(aggregated), eq(1))).thenReturn(summary);
        when(responseMapper.buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), anyList())).thenReturn(timeSeries);

        service.getActivitiesDashboardResponse(filter);

        verify(periodFactory).buildPeriods(filter);
        verify(aggregator).aggregateSecondsByPeriodAndCategory(activities, orderedPeriods);
    }

    @Test
    void getActivitiesDashboardResponse_shouldSortCategoriesForTimeSeriesLikeSummary() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Category category1 = createCategory("cat-1", "Sport");
        Category category2 = createCategory("cat-2", "Work");

        List<Activity> activities = List.of(
                createActivity("activity-1", category1),
                createActivity("activity-2", category2)
        );
        List<PeriodKey> orderedPeriods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of();
        SummaryResponse summary = new SummaryResponse(10, 10, List.of(
                new CategoryResponse("cat-2", "Work", 6, 0.6f, null, null),
                new CategoryResponse("cat-1", "Sport", 4, 0.4f, null, null)
        ));
        TimeSeriesResponse timeSeries = new TimeSeriesResponse(TimeGranularity.DAY, List.of());

        when(activityRepository.findAll(any(Specification.class))).thenReturn(activities);
        when(periodFactory.buildPeriods(filter)).thenReturn(orderedPeriods);
        when(aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods)).thenReturn(aggregated);
        when(responseMapper.buildSummary(anyList(), eq(aggregated), eq(1))).thenReturn(summary);
        when(responseMapper.buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), anyList())).thenReturn(timeSeries);

        service.getActivitiesDashboardResponse(filter);

        verify(responseMapper).buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), categoriesCaptor.capture());

        assertThat(categoriesCaptor.getValue())
                .extracting(Category::getBusinessId)
                .containsExactly("cat-2", "cat-1");
    }

    @Test
    void getActivitiesDashboardResponse_shouldReturnComposedResponse() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Category category = createCategory("cat-1", "Sport");
        List<Activity> activities = List.of(createActivity("activity-1", category));
        List<PeriodKey> orderedPeriods = List.of(
                new PeriodKey(Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-02T00:00:00Z"))
        );
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of();
        SummaryResponse summary = new SummaryResponse(120, 120, List.of(
                new CategoryResponse("cat-1", "Sport", 120, 1.0f, "#fff", "icon")
        ));
        TimeSeriesResponse timeSeries = new TimeSeriesResponse(TimeGranularity.DAY, List.of());

        when(activityRepository.findAll(any(Specification.class))).thenReturn(activities);
        when(periodFactory.buildPeriods(filter)).thenReturn(orderedPeriods);
        when(aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods)).thenReturn(aggregated);
        when(responseMapper.buildSummary(anyList(), eq(aggregated), eq(1))).thenReturn(summary);
        when(responseMapper.buildTimeSeries(eq(TimeGranularity.DAY), eq(orderedPeriods), eq(aggregated), anyList())).thenReturn(timeSeries);

        Response response = service.getActivitiesDashboardResponse(filter);

        assertThat(response.filters()).isEqualTo(filter);
        assertThat(response.summary()).isEqualTo(summary);
        assertThat(response.timeSeries()).isEqualTo(timeSeries);
    }

    @Test
    void getActivitiesDashboardResponse_shouldThrowArgumentNullPointerException_whenFilterIsNull() {
        assertThatThrownBy(() -> service.getActivitiesDashboardResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    private Category createCategory(String businessId, String name) {
        Category category = new Category();
        category.setBusinessId(businessId);
        category.setName(name);
        return category;
    }

    private Activity createActivity(String businessId, Category category) {
        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setStartAt(Instant.parse("2026-06-01T10:00:00Z"));
        activity.setEndAt(Instant.parse("2026-06-01T11:00:00Z"));

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setCategory(category);
        allocation.setPercentage(100);
        allocation.setActivity(activity);

        Set<CategoryAllocation> allocations = new LinkedHashSet<>();
        allocations.add(allocation);
        activity.setCategoryAllocations(allocations);

        return activity;
    }
}