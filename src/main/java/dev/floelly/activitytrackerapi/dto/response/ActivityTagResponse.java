package dev.floelly.activitytrackerapi.dto.response;

public record ActivityTagResponse(String id, String label, String color, String description, int sortOrder) {
}
