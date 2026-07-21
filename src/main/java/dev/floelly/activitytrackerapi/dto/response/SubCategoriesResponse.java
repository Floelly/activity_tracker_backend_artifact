package dev.floelly.activitytrackerapi.dto.response;

import java.util.List;

public record SubCategoriesResponse(
        List<SubCategoryResponse> subcategories
) {
}
