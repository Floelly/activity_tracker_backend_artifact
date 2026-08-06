package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.ValidTSID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchDeleteActivityRequest(
        @NotEmpty(message = "activityIds must not be empty")
        List<@Valid @ValidTSID String> activityIds
) {
}