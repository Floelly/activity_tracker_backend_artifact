package dev.floelly.activitytrackerapi.feature.activitydashboard.dto;

import java.time.Instant;
import java.util.List;

public record PeriodResponse(
        Instant from,
        Instant to,
        long totalMinutes,
        List<CategoryResponse> categories
) {
}
