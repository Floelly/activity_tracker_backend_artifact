package dev.floelly.activitytrackerapi.feature.activitydashboard;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class PeriodKeyTest {

    @Test
    void isLongerThan_shouldReturnTrue_whenPeriodIsLongerThanGivenDuration() {
        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:01Z")
        );

        boolean result = period.isLongerThan(Duration.ofDays(1));

        assertThat(result).isTrue();
    }

    @Test
    void isLongerThan_shouldReturnFalse_whenPeriodEqualsGivenDuration() {
        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z")
        );

        boolean result = period.isLongerThan(Duration.ofDays(1));

        assertThat(result).isFalse();
    }

    @Test
    void isLongerThan_shouldReturnFalse_whenPeriodIsShorterThanGivenDuration() {
        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-01T23:59:59Z")
        );

        boolean result = period.isLongerThan(Duration.ofDays(1));

        assertThat(result).isFalse();
    }

    @Test
    void takeFromStart_shouldReturnNewPeriodFromStartWithGivenDuration() {
        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-10T00:00:00Z")
        );

        PeriodKey result = period.takeFromStart(Duration.ofDays(3));

        assertThat(result.from()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(result.to()).isEqualTo(Instant.parse("2026-06-04T00:00:00Z"));
    }

    @Test
    void removeFromStart_shouldReturnNewPeriodWithoutInitialDuration() {
        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-10T00:00:00Z")
        );

        PeriodKey result = period.removeFromStart(Duration.ofDays(3));

        assertThat(result.from()).isEqualTo(Instant.parse("2026-06-04T00:00:00Z"));
        assertThat(result.to()).isEqualTo(Instant.parse("2026-06-10T00:00:00Z"));
    }

    @Test
    void takeFromStart_andRemoveFromStart_shouldSplitPeriodCorrectly() {
        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-10T00:00:00Z")
        );

        PeriodKey firstPart = period.takeFromStart(Duration.ofDays(4));
        PeriodKey restPart = period.removeFromStart(Duration.ofDays(4));

        assertThat(firstPart.from()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(firstPart.to()).isEqualTo(Instant.parse("2026-06-05T00:00:00Z"));
        assertThat(restPart.from()).isEqualTo(Instant.parse("2026-06-05T00:00:00Z"));
        assertThat(restPart.to()).isEqualTo(Instant.parse("2026-06-10T00:00:00Z"));
    }
}