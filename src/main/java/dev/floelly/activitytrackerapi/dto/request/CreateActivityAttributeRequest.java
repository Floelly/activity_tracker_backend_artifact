package dev.floelly.activitytrackerapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateActivityAttributeRequest(
        @NotBlank
        @Size(max = 50)
        String key,

        @Size(max = 255)
        String value,

        @NotNull
        Boolean showInOverview,

        @PositiveOrZero
        int sortOrder
) {
}
