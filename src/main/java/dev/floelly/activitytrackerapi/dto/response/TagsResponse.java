package dev.floelly.activitytrackerapi.dto.response;

import java.util.List;

public record TagsResponse(
        List<TagResponse> tags
) {
}
