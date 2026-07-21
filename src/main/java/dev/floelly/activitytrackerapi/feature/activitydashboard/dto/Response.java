package dev.floelly.activitytrackerapi.feature.activitydashboard.dto;

public record Response(
        ActivitiesDashboardFilterDTO filters,

        SummaryResponse summary,

        TimeSeriesResponse timeSeries
) {
}
