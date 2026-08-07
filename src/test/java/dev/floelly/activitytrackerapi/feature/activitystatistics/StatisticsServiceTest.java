package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.request.StatisticsFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.response.StatisticsResponse;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getStatistics_shouldReturnZeroMetrics_whenNoActivitiesMatch() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("1900-01-01T00:00:00Z"),
                Instant.parse("1900-12-31T23:59:59Z"),
                null
        );

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StatisticsResponse result = statisticsService.getStatistics(filter);

        assertThat(result.totalActivities()).isZero();
        assertThat(result.totalDurationInSeconds()).isZero();
        assertThat(result.averageDurationInSeconds()).isZero();

        verify(activityRepository).findAll(any(Specification.class));
        verify(categoryRepository, never()).findAllByBusinessIdIn(any());
    }

    @Test
    void getStatistics_shouldReturnAggregatedMetrics_whenFilteredByDateRange() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-03T00:00:00Z"),
                null
        );

        Activity activity1 = createActivity(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z"));
        Activity activity2 = createActivity(Instant.parse("2026-01-02T10:00:00Z"), Instant.parse("2026-01-02T12:00:00Z"));

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));

        StatisticsResponse result = statisticsService.getStatistics(filter);

        assertThat(result.totalActivities()).isEqualTo(2);
        assertThat(result.totalDurationInSeconds()).isEqualTo(10800);
        assertThat(result.averageDurationInSeconds()).isEqualTo(5400);

        verify(activityRepository).findAll(any(Specification.class));
        verify(categoryRepository, never()).findAllByBusinessIdIn(any());
    }

    @Test
    void getStatistics_shouldReturnAggregatedMetrics_whenFilteredByCategory() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("cat-1")
        );

        Activity activity1 = createActivity(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z"));
        Activity activity2 = createActivity(Instant.parse("2026-01-02T10:00:00Z"), Instant.parse("2026-01-02T12:00:00Z"));

        when(categoryRepository.findAllByBusinessIdIn(List.of("cat-1"))).thenReturn(List.of(category));
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));

        StatisticsResponse result = statisticsService.getStatistics(filter);

        assertThat(result.totalActivities()).isEqualTo(2);
        assertThat(result.totalDurationInSeconds()).isEqualTo(10800);
        assertThat(result.averageDurationInSeconds()).isEqualTo(5400);

        verify(activityRepository).findAll(any(Specification.class));
        verify(categoryRepository).findAllByBusinessIdIn(List.of("cat-1"));
    }

    @Test
    void getStatistics_shouldReturnAggregatedMetrics_whenFilteredByDateRangeAndCategory() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-03T00:00:00Z"),
                List.of("cat-1")
        );

        Activity activity1 = createActivity(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z"));
        Activity activity2 = createActivity(Instant.parse("2026-01-02T10:00:00Z"), Instant.parse("2026-01-02T12:00:00Z"));

        when(categoryRepository.findAllByBusinessIdIn(List.of("cat-1"))).thenReturn(List.of(category));
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));

        StatisticsResponse result = statisticsService.getStatistics(filter);

        assertThat(result.totalActivities()).isEqualTo(2);
        assertThat(result.totalDurationInSeconds()).isEqualTo(10800);
        assertThat(result.averageDurationInSeconds()).isEqualTo(5400);

        verify(activityRepository).findAll(any(Specification.class));
        verify(categoryRepository).findAllByBusinessIdIn(List.of("cat-1"));
    }

    @Test
    void getStatistics_shouldReturnAggregatedMetrics_whenMultipleActivities() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, null);

        Activity activity1 = createActivity(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z"));
        Activity activity2 = createActivity(Instant.parse("2026-01-02T10:00:00Z"), Instant.parse("2026-01-02T12:00:00Z"));
        Activity activity3 = createActivity(Instant.parse("2026-01-03T10:00:00Z"), Instant.parse("2026-01-03T13:00:00Z"));

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2, activity3));

        StatisticsResponse result = statisticsService.getStatistics(filter);

        assertThat(result.totalActivities()).isEqualTo(3);
        assertThat(result.totalDurationInSeconds()).isEqualTo(21600);
        assertThat(result.averageDurationInSeconds()).isEqualTo(7200);

        verify(activityRepository).findAll(any(Specification.class));
    }

    @Test
    void getStatistics_shouldThrowNotFoundException_whenCategoryDoesNotExist() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("non-existent-category")
        );

        when(categoryRepository.findAllByBusinessIdIn(List.of("non-existent-category"))).thenReturn(List.of());

        assertThatThrownBy(() -> statisticsService.getStatistics(filter))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("non-existent-category");

        verify(categoryRepository).findAllByBusinessIdIn(List.of("non-existent-category"));
        verify(activityRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void getStatistics_shouldThrowNotFoundException_whenOneOfMultipleCategoriesDoesNotExist() {
        Category existingCategory = new Category();
        existingCategory.setBusinessId("existing-cat");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("existing-cat", "non-existent-cat")
        );

        when(categoryRepository.findAllByBusinessIdIn(List.of("existing-cat", "non-existent-cat")))
                .thenReturn(List.of(existingCategory));

        assertThatThrownBy(() -> statisticsService.getStatistics(filter))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("non-existent-cat");

        verify(categoryRepository).findAllByBusinessIdIn(List.of("existing-cat", "non-existent-cat"));
        verify(activityRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void getStatistics_shouldReturnZeroMetrics_whenNoActivitiesMatchForCategory() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                null,
                null,
                List.of("cat-1")
        );

        when(categoryRepository.findAllByBusinessIdIn(List.of("cat-1"))).thenReturn(List.of(category));
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StatisticsResponse result = statisticsService.getStatistics(filter);

        assertThat(result.totalActivities()).isZero();
        assertThat(result.totalDurationInSeconds()).isZero();
        assertThat(result.averageDurationInSeconds()).isZero();

        verify(categoryRepository).findAllByBusinessIdIn(List.of("cat-1"));
        verify(activityRepository).findAll(any(Specification.class));
    }

    private static Activity createActivity(Instant startAt, Instant endAt) {
        Activity activity = new Activity();
        activity.setStartAt(startAt);
        activity.setEndAt(endAt);
        return activity;
    }
}