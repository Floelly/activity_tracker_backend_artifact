package dev.floelly.activitytrackerapi.dto.response;

public record ActivityAttributeResponse(String key, String value, Boolean showInOverview, int sortOrder) {
}
