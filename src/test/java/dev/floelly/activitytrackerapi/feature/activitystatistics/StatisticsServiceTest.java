package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Captor
    private ArgumentCaptor<Specification<Activity>> specificationCaptor;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getStatistics_shouldReturnAggregatedMetrics_whenNoFilter() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, null);

        Activity activity1 = createActivity("act-1", "2041-01-01T10:00:00Z", "2041-01-01T11:00:00Z");
        Activity activity2 = createActivity("act-2", "2041-01-02T10:00:00Z", "2041-01-02T12:00:00Z");

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(2);
        assertThat(response.totalDurationInSeconds()).isEqualTo(10800L);
        assertThat(response.averageDurationInSeconds()).isEqualTo(5400L);

        verify(activityRepository).findAll(any(Specification.class));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getStatistics_shouldFilterByDateRange() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2041-01-01T00:00:00Z"),
                Instant.parse("2041-01-03T00:00:00Z"),
                null
        );

        Activity activity1 = createActivity("act-1", "2041-01-01T10:00:00Z", "2041-01-01T11:00:00Z");
        Activity activity2 = createActivity("act-2", "2041-01-02T10:00:00Z", "2041-01-02T12:00:00Z");

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(2);
        assertThat(response.totalDurationInSeconds()).isEqualTo(10800L);
        assertThat(response.averageDurationInSeconds()).isEqualTo(5400L);

        verify(activityRepository).findAll(specificationCaptor.capture());
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getStatistics_shouldReturnZeroMetrics_whenNoActivitiesMatch() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("1900-01-01T00:00:00Z"),
                Instant.parse("1900-12-31T23:59:59Z"),
                null
        );

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(0);
        assertThat(response.totalDurationInSeconds()).isEqualTo(0L);
        assertThat(response.averageDurationInSeconds()).isEqualTo(0L);

        verify(activityRepository).findAll(any(Specification.class));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getStatistics_shouldFilterByCategory() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, List.of("0000000000001"));

        when(categoryRepository.findAllByBusinessIdIn(List.of("0000000000001")))
                .thenReturn(List.of(createCategory("0000000000001")));

        Activity activity1 = createActivity("act-1", "2041-01-01T10:00:00Z", "2041-01-01T11:00:00Z");

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1));

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(1);
        assertThat(response.totalDurationInSeconds()).isEqualTo(3600L);
        assertThat(response.averageDurationInSeconds()).isEqualTo(3600L);

        verify(categoryRepository).findAllByBusinessIdIn(List.of("0000000000001"));
        verify(activityRepository).findAll(any(Specification.class));
    }

    @Test
    void getStatistics_shouldThrowNotFoundException_whenCategoryDoesNotExist() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, List.of("0000000000001"));

        when(categoryRepository.findAllByBusinessIdIn(List.of("0000000000001")))
                .thenReturn(List.of());

        assertThatThrownBy(() -> statisticsService.getStatistics(filter))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("0000000000001");

        verify(categoryRepository).findAllByBusinessIdIn(List.of("0000000000001"));
        verifyNoInteractions(activityRepository);
    }

    @Test
    void getStatistics_shouldThrowNotFoundException_whenSomeCategoriesDoNotExist() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, List.of("0000000000001", "0000000000002"));

        when(categoryRepository.findAllByBusinessIdIn(List.of("0000000000001", "0000000000002")))
                .thenReturn(List.of(createCategory("0000000000001")));

        assertThatThrownBy(() -> statisticsService.getStatistics(filter))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("0000000000002");

        verify(categoryRepository).findAllByBusinessIdIn(List.of("0000000000001", "0000000000002"));
        verifyNoInteractions(activityRepository);
    }

    @Test
    void getStatistics_shouldCalculateAverageCorrectly_whenSingleActivity() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, null);

        Activity activity = createActivity("act-1", "2041-01-01T10:00:00Z", "2041-01-01T12:30:00Z");

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity));

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(1);
        assertThat(response.totalDurationInSeconds()).isEqualTo(9000L);
        assertThat(response.averageDurationInSeconds()).isEqualTo(9000L);
    }

    @Test
    void getStatistics_shouldCalculateAverageCorrectly_whenMultipleActivitiesWithDifferentDurations() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(null, null, null);

        Activity activity1 = createActivity("act-1", "2041-01-01T10:00:00Z", "2041-01-01T11:00:00Z");  // 3600s
        Activity activity2 = createActivity("act-2", "2041-01-02T10:00:00Z", "2041-01-02T13:00:00Z");  // 10800s
        Activity activity3 = createActivity("act-3", "2041-01-03T10:00:00Z", "2041-01-03T10:30:00Z");  // 1800s

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2, activity3));

        StatisticsResponse response = statisticsService.getStatistics(filter);

        assertThat(response.totalActivities()).isEqualTo(3);
        assertThat(response.totalDurationInSeconds()).isEqualTo(16200L);
        assertThat(response.averageDurationInSeconds()).isEqualTo(5400L); // 16200 / 3
    }

    @Test
    void getStatistics_shouldPassSpecificationToRepository() {
        StatisticsFilterDTO filter = new StatisticsFilterDTO(
                Instant.parse("2041-01-01T00:00:00Z"),
                Instant.parse("2041-01-03T00:00:00Z"),
                null
        );

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        statisticsService.getStatistics(filter);

        verify(activityRepository).findAll(specificationCaptor.capture());
        Specification<Activity> capturedSpec = specificationCaptor.getValue();
        assertThat(capturedSpec).isNotNull();
    }

    private Activity createActivity(String businessId, String startAt, String endAt) {
        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setStartAt(Instant.parse(startAt));
        activity.setEndAt(Instant.parse(endAt));
        return activity;
    }

    private dev.floelly.activitytrackerapi.entity.Category createCategory(String businessId) {
        dev.floelly.activitytrackerapi.entity.Category category = new dev.floelly.activitytrackerapi.entity.Category();
        category.setBusinessId(businessId);
        category.setName("Test Category");
        return category;
    }
}