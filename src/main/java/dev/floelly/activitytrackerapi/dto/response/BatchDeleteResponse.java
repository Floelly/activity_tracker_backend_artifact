package dev.floelly.activitytrackerapi.dto.response;

import java.util.List;

public record BatchDeleteResponse(
        int totalRequested,
        int totalDeleted,
        List<FailedDelete> failed
) {
}
