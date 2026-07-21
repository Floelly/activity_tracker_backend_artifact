package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.Response;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.SummaryResponse;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.TimeSeriesResponse;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {
    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;
    private final Aggregator aggregator;
    private final PeriodFactory periodFactory;
    private final ResponseMapper responseMapper;

    @Transactional(readOnly = true)
    @SuppressWarnings("PMD.LooseCoupling")
    public Response getActivitiesDashboardResponse(ActivitiesDashboardFilterDTO filter) {
        List<Category> requestedCategories = loadRequestedCategories(filter);
        List<Activity> activities = activityRepository.findAll(ActivitySpecifications.withFilter(filter));
        List<Category> categories = filter.hasCategoryFilter()
                ? requestedCategories
                : resolveRelevantCategories(activities);

        List<PeriodKey> orderedPeriods = periodFactory.buildPeriods(filter);
        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory =
                aggregator.aggregateSecondsByPeriodAndCategory(activities, orderedPeriods);

        SummaryResponse summary = responseMapper.buildSummary(categories, secondsByPeriodAndCategory, orderedPeriods.size());
        List<Category> orderedCategories = sortCategoriesLikeSummary(categories, summary);
        TimeSeriesResponse timeSeries =
                responseMapper.buildTimeSeries(filter.granularity(), orderedPeriods, secondsByPeriodAndCategory, orderedCategories);

        return new Response(filter, summary, timeSeries);
    }

    private List<Category> loadRequestedCategories(ActivitiesDashboardFilterDTO filter) {
        if (!filter.hasCategoryFilter()) {
            return List.of();
        }
        List<String> allowedCategoryIds = filter.categoryIds();
        List<Category> categories = categoryRepository.findAllByBusinessIdIn(allowedCategoryIds);
        if (categories.size() != allowedCategoryIds.size()) {
            List<String> missingIds = allowedCategoryIds.stream()
                    .filter(id -> categories.stream().noneMatch(category -> category.getBusinessId().equals(id)))
                    .toList();
            throw new NotFoundException(String.format("One or more categories with the given ids were not found! %s", missingIds));
        }
        return categories;
    }

    private List<Category> resolveRelevantCategories(List<Activity> activities) {
        return activities.stream()
                .flatMap(activity -> activity.getCategoryAllocations().stream()).map(CategoryAllocation::getCategory)
                .distinct()
                .toList();
    }

    @SuppressWarnings("PMD.LooseCoupling")
    private List<Category> sortCategoriesLikeSummary(Collection<Category> categories, SummaryResponse summary) {
        Map<String, Integer> orderByCategoryId = new HashMap<>();

        for (int i = 0; i < summary.categories().size(); i++) {
            orderByCategoryId.put(summary.categories().get(i).id(), i);
        }

        return categories.stream()
                .sorted(Comparator.comparingInt(category -> orderByCategoryId.getOrDefault(category.getBusinessId(), Integer.MAX_VALUE)))
                .toList();
    }
}
