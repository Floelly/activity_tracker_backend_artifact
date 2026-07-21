package dev.floelly.activitytrackerapi.feature.activitydashboard.dto;

import java.util.List;

public record SummaryResponse(
        long totalMinutes,
        long averageMinutesPerPeriod,
        List<CategoryResponse> categories
) {
}
