package dev.floelly.activitytrackerapi.dto.request;

import java.time.Instant;

public interface TimeRangeRequest {
    Instant startAt();

    Instant endAt();
}
