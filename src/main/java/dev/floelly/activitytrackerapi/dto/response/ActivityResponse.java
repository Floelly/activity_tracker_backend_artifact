package dev.floelly.activitytrackerapi.dto.response;

import java.time.Instant;
import java.util.List;

public record ActivityResponse(
        String id,
        String title,
        String notes,
        Instant startAt,
        Instant endAt,
        int durationInSeconds,
        List<ActivityCategoryAllocationResponse> categoryAllocations,
        List<ActivityAttributeResponse> customValues,
        List<ActivityTagResponse> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
