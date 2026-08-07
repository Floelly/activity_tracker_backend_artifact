package dev.floelly.activitytrackerapi.feature.activitystatistics.dto.response;

public record StatisticsResponse(
        int totalActivities,
        long totalDurationInSeconds,
        long averageDurationInSeconds
) {
}