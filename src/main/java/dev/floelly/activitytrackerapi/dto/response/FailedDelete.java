package dev.floelly.activitytrackerapi.dto.response;

public record FailedDelete(
        String id,
        String reason
) {
}