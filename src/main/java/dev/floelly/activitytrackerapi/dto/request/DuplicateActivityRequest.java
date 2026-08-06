package dev.floelly.activitytrackerapi.dto.request;

import dev.floelly.activitytrackerapi.validation.ValidTimeRange;

import java.time.Instant;

@ValidTimeRange
public record DuplicateActivityRequest(
        Instant startAt,
        Instant endAt
) implements TimeRangeRequest {
}