package dev.floelly.activitytrackerapi.feature.activitydashboard;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TimeGranularityTest {

    @Test
    void toDuration_shouldReturnOneDay_whenGranularityIsDay() {
        Duration duration = TimeGranularity.DAY.toDuration();

        assertThat(duration).isEqualTo(Duration.ofDays(1));
        assertThat(duration.toHours()).isEqualTo(24);
    }

    @Test
    void toDuration_shouldReturnSevenDays_whenGranularityIsWeek() {
        Duration duration = TimeGranularity.WEEK.toDuration();

        assertThat(duration).isEqualTo(Duration.ofDays(7));
        assertThat(duration.toHours()).isEqualTo(168);
    }

    @Test
    void DAY_constant_shouldExist() {
        assertThat(TimeGranularity.DAY).isNotNull();
    }

    @Test
    void WEEK_constant_shouldExist() {
        assertThat(TimeGranularity.WEEK).isNotNull();
    }
}