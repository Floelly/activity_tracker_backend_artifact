package dev.floelly.activitytrackerapi.feature.activitydashboard.dto;

import dev.floelly.activitytrackerapi.dto.request.TimeRangeRequest;
import dev.floelly.activitytrackerapi.feature.activitydashboard.TimeGranularity;
import dev.floelly.activitytrackerapi.validation.ValidTSID;
import dev.floelly.activitytrackerapi.validation.ValidTimeRange;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

@ValidTimeRange(message = "Bad time range: 'from' must be before or equal to 'to'")
public record ActivitiesDashboardFilterDTO(
        @NotNull
        Instant from,

        @NotNull
        Instant to,

        @NotNull
        TimeGranularity granularity,

        List<@ValidTSID String> category
) implements TimeRangeRequest {

    @Override
    public Instant startAt() {
        return this.from();
    }

    @Override
    public Instant endAt() {
        return this.to();
    }

    public boolean hasCategoryFilter() {
        return this.category() != null && !this.category().isEmpty();
    }

    public List<@ValidTSID String> categoryIds() {
        return this.category();
    }
}
