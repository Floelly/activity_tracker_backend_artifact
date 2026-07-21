package dev.floelly.activitytrackerapi.feature.activitydashboard.dto;

public record CategoryResponse(
        String id,
        String name,
        long minutes,
        float percentage,
        String color,
        String icon
) {
}
