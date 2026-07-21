package dev.floelly.activitytrackerapi.dto.response;

import java.util.List;

public record ActivitiesResponse(boolean hasMore, List<ActivityResponse> activities) {
}
