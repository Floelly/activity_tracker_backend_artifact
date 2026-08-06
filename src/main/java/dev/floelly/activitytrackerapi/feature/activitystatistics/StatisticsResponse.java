package dev.floelly.activitytrackerapi.feature.activitystatistics;

public record StatisticsResponse(
        int totalActivities,
        long totalDurationInSeconds,
        long averageDurationInSeconds
) {
}