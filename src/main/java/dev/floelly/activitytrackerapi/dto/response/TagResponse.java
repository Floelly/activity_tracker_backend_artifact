package dev.floelly.activitytrackerapi.dto.response;

public record TagResponse(String id, String label, String color, String description, int sortOrder) {
}
