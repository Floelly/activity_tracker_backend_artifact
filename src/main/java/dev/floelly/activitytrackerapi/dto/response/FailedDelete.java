package dev.floelly.activitytrackerapi.dto.response;

public record FailedDelete(
        String activityId,
        String reason
) {
}