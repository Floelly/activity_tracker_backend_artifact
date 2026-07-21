package dev.floelly.activitytrackerapi.dto.response;

import java.util.List;

public record CategoriesResponse(
        List<CategoryResponse> categories
) {
}
