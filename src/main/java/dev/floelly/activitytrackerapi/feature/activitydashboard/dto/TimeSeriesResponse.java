package dev.floelly.activitytrackerapi.feature.activitydashboard.dto;

import dev.floelly.activitytrackerapi.feature.activitydashboard.TimeGranularity;

import java.util.List;

public record TimeSeriesResponse(
        TimeGranularity granularity,

        List<PeriodResponse> periods
) {
}
