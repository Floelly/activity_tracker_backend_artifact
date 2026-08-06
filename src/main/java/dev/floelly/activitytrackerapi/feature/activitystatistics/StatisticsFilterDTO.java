package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.dto.request.TimeRangeRequest;
import dev.floelly.activitytrackerapi.validation.ValidTSID;
import dev.floelly.activitytrackerapi.validation.ValidTimeRange;

import java.time.Instant;
import java.util.List;

@ValidTimeRange(message = "Bad time range: fromStartAt must be before or equal to toStartAt")
public record StatisticsFilterDTO(
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

    public boolean hasCategoryFilter() {
        return category != null && !category.isEmpty();
    }
}