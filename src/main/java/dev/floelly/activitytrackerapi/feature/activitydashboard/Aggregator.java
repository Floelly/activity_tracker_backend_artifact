package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
class Aggregator {
    @SuppressWarnings("PMD.LooseCoupling")
    Map<PeriodKey, Map<String, Long>> aggregateSecondsByPeriodAndCategory(List<Activity> activities, List<PeriodKey> periods) {
        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = new HashMap<>();

        for (Activity activity : activities) {
            for (PeriodKey period : periods) {
                boolean activityOverlapsPeriod = activity.getStartAt().isBefore(period.to()) && activity.getEndAt().isAfter(period.from());
                if (activityOverlapsPeriod) {
                    Instant effectiveStart = ObjectUtils.max(activity.getStartAt(), period.from());
                    Instant effectiveEnd = ObjectUtils.min(activity.getEndAt(), period.to());
                    long overlapSeconds = Duration.between(effectiveStart, effectiveEnd).toSeconds();
                    for (CategoryAllocation allocation : activity.getCategoryAllocations()) {
                        String categoryId = allocation.getCategory().getBusinessId();
                        long effectiveOverlapSeconds = (overlapSeconds * allocation.getPercentage() + 50) / 100;
                        secondsByPeriodAndCategory
                                .computeIfAbsent(period, key -> new HashMap<>())
                                .merge(categoryId, effectiveOverlapSeconds, Long::sum);
                    }
                }
            }
        }
        return secondsByPeriodAndCategory;
    }
}
