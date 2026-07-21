package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.Color;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(

        @NotBlank
        @Size(max = 30)
        String label,

        @Color
        String color,

        @Size(max = 255)
        String description,

        @PositiveOrZero
        int sortOrder
) {
}
