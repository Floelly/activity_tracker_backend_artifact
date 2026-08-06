package dev.floelly.activitytrackerapi.dto.request;

import jakarta.validation.constraints.AssertTrue;

import java.time.Instant;

public record DuplicateActivityRequest(
        Instant startAt,
        Instant endAt
) {

    /**
     * If endAt is provided, startAt must also be provided.
     */
    @AssertTrue(message = "endAt can only be provided together with startAt")
    public boolean isEndAtOnlyWithStartAt() {
        return endAt == null || startAt != null;
    }

    /**
     * If both are provided, endAt must be after startAt.
     */
    @AssertTrue(message = "endAt must be after startAt")
    public boolean isEndAtAfterStartAt() {
        if (startAt == null || endAt == null) {
            return true;
        }
        return endAt.isAfter(startAt);
    }
}
