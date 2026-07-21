package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.Color;
import dev.floelly.activitytrackerapi.validation.ValidTSID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        
        @NotNull
        @ValidTSID
        String id,

        @NotBlank
        @Size(max = 50)
        String name,

        @Color
        String color,

        @Size(max = 30)
        String icon,

        @Size(max = 255)
        String description
) {
}
