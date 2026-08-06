package dev.floelly.activitytrackerapi.feature.activitystatistics.dto;

public record StatisticsResponse(
        int totalActivities,
        long totalDurationInSeconds,
        long averageDurationInSeconds
) {
}