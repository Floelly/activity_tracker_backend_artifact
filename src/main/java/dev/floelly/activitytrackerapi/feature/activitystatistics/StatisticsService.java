package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
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
        validateCategoryExistence(filter);

        List<Activity> activities = activityRepository.findAll(StatisticsSpecifications.withFilter(filter));

        int totalActivities = activities.size();
        long totalDurationInSeconds = activities.stream()
                .mapToLong(activity -> Duration.between(activity.getStartAt(), activity.getEndAt()).toSeconds())
                .sum();
        long averageDurationInSeconds = totalActivities > 0
                ? totalDurationInSeconds / totalActivities
                : 0L;

        return new StatisticsResponse(totalActivities, totalDurationInSeconds, averageDurationInSeconds);
    }

    private void validateCategoryExistence(StatisticsFilterDTO filter) {
        if (!filter.hasCategoryFilter()) {
            return;
        }
        List<String> requestedCategoryIds = filter.category();
        List<String> foundCategoryIds = categoryRepository.findAllByBusinessIdIn(requestedCategoryIds)
                .stream()
                .map(category -> category.getBusinessId())
                .toList();

        List<String> missingIds = requestedCategoryIds.stream()
                .filter(id -> !foundCategoryIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NotFoundException(
                    "One or more categories with the given ids were not found! " + missingIds
            );
        }
    }
}