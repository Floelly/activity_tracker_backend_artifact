package dev.floelly.activitytrackerapi.feature.activitydashboard;

import java.time.Duration;

public enum TimeGranularity {
    DAY,
    WEEK;

    public Duration toDuration() {
        if (this == DAY) {
            return Duration.ofDays(1);
        } else {
            return Duration.ofDays(7);
        }
    }
}
