package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.ValidTSID;
import dev.floelly.activitytrackerapi.validation.ValidTimeRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

@ValidTimeRange
public record UpdateActivityRequest(

        @NotNull
        @ValidTSID
        String id,

        @NotNull
        @Size(max = 120)
        String title,

        @Size(max = 255)
        String notes,

        @NotNull
        Instant startAt,

        @NotNull
        Instant endAt,

        @NotNull
        List<@NotNull @Valid CreateCategoryAllocationRequest> categoryAllocations

//        @NotNull
//        List<@Valid CreateActivityAttributeRequest> customValues,
//
//        @NotNull
//        List<@ValidTSID String> tagIds
) implements TimeRangeRequest {
}
