package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.request.StatisticsFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.response.StatisticsResponse;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(StatisticsFilterDTO filter) {
        validateCategoriesExist(filter);

        List<Activity> activities = activityRepository.findAll(StatisticsSpecifications.withFilter(filter));

        if (activities.isEmpty()) {
            return new StatisticsResponse(0, 0, 0);
        }

        long totalDurationInSeconds = activities.stream()
                .mapToLong(activity -> Duration.between(activity.getStartAt(), activity.getEndAt()).toSeconds())
                .sum();

        int totalActivities = activities.size();
        long averageDurationInSeconds = totalDurationInSeconds / totalActivities;

        return new StatisticsResponse(totalActivities, totalDurationInSeconds, averageDurationInSeconds);
    }

    private void validateCategoriesExist(StatisticsFilterDTO filter) {
        if (filter.category() != null && !filter.category().isEmpty()) {
            List<String> existingCategoryIds = categoryRepository.findAllByBusinessIdIn(filter.category())
                    .stream()
                    .map(category -> category.getBusinessId())
                    .toList();

            for (String categoryId : filter.category()) {
                if (!existingCategoryIds.contains(categoryId)) {
                    throw new NotFoundException("Category with id '" + categoryId + "' not found.");
                }
            }
        }
    }
}