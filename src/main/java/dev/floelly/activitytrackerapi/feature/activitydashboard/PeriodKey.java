package dev.floelly.activitytrackerapi.feature.activitydashboard;

import java.time.Duration;
import java.time.Instant;

record PeriodKey(
        Instant from,
        Instant to
) {
    boolean isLongerThan(Duration periodDuration) {
        return Duration.between(this.from(), this.to()).compareTo(periodDuration) > 0;
    }

    PeriodKey takeFromStart(Duration duration) {
        return new PeriodKey(this.from(), this.from().plus(duration));
    }

    PeriodKey removeFromStart(Duration duration) {
        return new PeriodKey(this.from().plus(duration), this.to());
    }
}
