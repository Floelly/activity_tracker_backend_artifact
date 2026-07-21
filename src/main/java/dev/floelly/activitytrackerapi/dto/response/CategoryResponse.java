package dev.floelly.activitytrackerapi.dto.response;

import java.util.List;

public record CategoryResponse(
        String id,
        String name,
        String color,
        String icon,
        String description,
        List<SubCategoryResponse> subcategories
) {
}
