package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.ValidTSID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateCategoryAllocationRequest(

        @Min(1) @Max(100)
        int percentage,

        @ValidTSID
        String categoryId,

        @ValidTSID(nullable = true)
        String subCategoryId
) {
}
