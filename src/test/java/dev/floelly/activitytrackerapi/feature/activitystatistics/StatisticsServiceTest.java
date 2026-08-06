package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.StatisticsFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.StatisticsResponse;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Captor
    private ArgumentCaptor<Specification<Activity>> specCaptor;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getStatistics_shouldReturnAggregatedMetrics() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2041-01-01T00:00:00Z"),
                Instant.parse("2041-01-03T00:00:00Z"),
                null
        );

        Activity activity1 = createActivity("act-1", Instant.parse("2041-01-01T10:00:00Z"), Instant.parse("2041-01-01T11:00:00Z")); // 3600s
        Activity activity2 = createActivity("act-2", Instant.parse("2041-01-02T10:00:00Z"), Instant.parse("2041-01-02T12:00:00Z")); // 7200s

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(2);
        assertThat(response.totalDurationInSeconds()).isEqualTo(10800);
        assertThat(response.averageDurationInSeconds()).isEqualTo(5400);

        verify(activityRepository).findAll(any(Specification.class));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getStatistics_shouldReturnZeroMetrics_whenNoActivities() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("1900-01-01T00:00:00Z"),
                Instant.parse("1900-12-31T23:59:59Z"),
                null
        );

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(0);
        assertThat(response.totalDurationInSeconds()).isEqualTo(0);
        assertThat(response.averageDurationInSeconds()).isEqualTo(0);

        verify(activityRepository).findAll(any(Specification.class));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getStatistics_shouldThrowNotFoundException_whenCategoryNotFound() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2041-01-01T00:00:00Z"),
                Instant.parse("2041-01-03T00:00:00Z"),
                List.of("cat-1", "cat-2")
        );

        when(categoryRepository.findAllByBusinessIdIn(filter.category()))
                .thenReturn(List.of(createCategory("cat-1", "Sport")));

        assertThrows(NotFoundException.class, () -> statisticsService.getStatistics(filter));

        verify(categoryRepository).findAllByBusinessIdIn(filter.category());
        verifyNoInteractions(activityRepository);
    }

    @Test
    void getStatistics_shouldFilterByDateRange() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2042-02-01T00:00:00Z"),
                Instant.parse("2042-02-03T00:00:00Z"),
                null
        );

        Activity activity1 = createActivity("act-1", Instant.parse("2042-02-01T10:00:00Z"), Instant.parse("2042-02-01T11:00:00Z")); // 3600s
        Activity activity2 = createActivity("act-2", Instant.parse("2042-02-02T10:00:00Z"), Instant.parse("2042-02-02T13:00:00Z")); // 10800s

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(2);
        assertThat(response.totalDurationInSeconds()).isEqualTo(14400);
        assertThat(response.averageDurationInSeconds()).isEqualTo(7200);

        verify(activityRepository).findAll(specCaptor.capture());
        assertThat(specCaptor.getValue()).isNotNull();
        verifyNoInteractions(categoryRepository);
    }

    private Activity createActivity(String businessId, Instant startAt, Instant endAt) {
        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setStartAt(startAt);
        activity.setEndAt(endAt);
        return activity;
    }

    private Category createCategory(String businessId, String name) {
        Category category = new Category();
        category.setBusinessId(businessId);
        category.setName(name);
        return category;
    }
}