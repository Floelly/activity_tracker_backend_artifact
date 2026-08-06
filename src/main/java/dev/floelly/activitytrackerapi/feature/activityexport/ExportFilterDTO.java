package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.dto.request.TimeRangeRequest;
import dev.floelly.activitytrackerapi.validation.ValidTimeRange;

import java.time.Instant;

@ValidTimeRange(message = "startDate must be before or equal to endDate")
public record ExportFilterDTO(
        String categoryId,
        Instant startDate,
        Instant endDate
) implements TimeRangeRequest {
    @Override
    public Instant startAt() {
        return startDate;
    }

    @Override
    public Instant endAt() {
        return endDate;
    }
}