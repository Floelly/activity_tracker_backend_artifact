package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.ValidTSID;
import dev.floelly.activitytrackerapi.validation.ValidTimeRange;

import java.time.Instant;
import java.util.List;

@ValidTimeRange(message = "Bad time range: fromStartAt must be before or equal to toStartAt")
public record ActivityFilterDTO(
        Instant fromStartAt,

        Instant toStartAt,

        List<@ValidTSID String> category
) implements TimeRangeRequest {
    @Override
    public Instant startAt() {
        return fromStartAt;
    }

    @Override
    public Instant endAt() {
        return toStartAt;
    }
}
