package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.ValidTSID;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BatchDeleteRequest(
        @NotNull
        @NotEmpty
        List<@NotNull @ValidTSID String> activityIds
) {
}