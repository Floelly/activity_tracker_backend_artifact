package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.CategoryResponse;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.PeriodResponse;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.Response;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.SummaryResponse;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.TimeSeriesResponse;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
class ResponseMapper {

    Response buildResponse(
            @NonNull ActivitiesDashboardFilterDTO filter,
            @NonNull List<Category> categories,
            @NonNull Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory,
            @NonNull List<PeriodKey> orderedPeriods) {
        SummaryResponse summary = buildSummary(categories, secondsByPeriodAndCategory, orderedPeriods.size());
        List<Category> orderedCategories = sortCategoriesLikeSummary(categories, summary);
        TimeSeriesResponse timeSeries = buildTimeSeries(filter.granularity(), orderedPeriods, secondsByPeriodAndCategory, orderedCategories);
        return new Response(filter, summary, timeSeries);
    }

    @SuppressWarnings("PMD.LooseCoupling")
    SummaryResponse buildSummary(List<Category> categories, Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory, int periodCount) {
        Map<String, Long> secondsPerCategory = new HashMap<>();
        for (Map<String, Long> secondsByCategory : secondsByPeriodAndCategory.values()) {
            for (Map.Entry<String, Long> entry : secondsByCategory.entrySet()) {
                secondsPerCategory.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
        }
        long totalSeconds = secondsPerCategory.values().stream().mapToLong(Long::longValue).sum();
        long totalMinutes = totalSeconds / 60;
        long averageMinutesPerPeriod = periodCount == 0 ? 0L : totalMinutes / periodCount;
        List<CategoryResponse> categoryDTO = categories.stream()
                .map(category -> {
                    long seconds = secondsPerCategory.getOrDefault(category.getBusinessId(), 0L);
                    long minutes = seconds / 60;
                    float percentage = totalSeconds == 0 ? 0f : (float) seconds / (float) totalSeconds;
                    return toDashboardCategory(category, minutes, percentage);
                })
                .sorted(Comparator.comparingLong(CategoryResponse::minutes).reversed())
                .toList();
        return new SummaryResponse(totalMinutes, averageMinutesPerPeriod, categoryDTO);
    }

    @SuppressWarnings("PMD.LooseCoupling")
    TimeSeriesResponse buildTimeSeries(
            @NonNull TimeGranularity granularity,
            @NonNull List<PeriodKey> orderedPeriods,
            @NonNull Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory,
            @NonNull List<Category> orderedCategories) {
        List<PeriodResponse> periodResponses = orderedPeriods.stream()
                .map(period -> {
                            Map<String, Long> secondsByCategory = secondsByPeriodAndCategory.getOrDefault(period, Collections.emptyMap());
                            long secondsPerPeriod = secondsByCategory.values().stream().reduce(0L, Long::sum);
                            long minutesPerPeriod = secondsPerPeriod / 60;
                            return new PeriodResponse(
                                    period.from(),
                                    period.to(),
                                    minutesPerPeriod,
                                    orderedCategories.stream()
                                            .map((Category category) -> {
                                                long seconds = secondsByCategory.getOrDefault(category.getBusinessId(), 0L);
                                                long minutes = seconds / 60;
                                                float percentage = secondsPerPeriod == 0 ? 0f : (float) seconds / (float) secondsPerPeriod;
                                                return toDashboardCategory(category, minutes, percentage);
                                            })
                                            .toList());
                        }
                )
                .toList();

        return new TimeSeriesResponse(granularity, periodResponses);
    }

    CategoryResponse toDashboardCategory(Category category, long minutesPerCategory, float percentage) {
        return new CategoryResponse(
                category.getBusinessId(),
                category.getName(),
                minutesPerCategory,
                percentage,
                category.getColorCode(),
                category.getIconName()
        );
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
