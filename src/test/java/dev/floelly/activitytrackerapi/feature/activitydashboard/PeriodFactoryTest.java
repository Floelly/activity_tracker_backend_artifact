package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class PeriodFactoryTest {

    private final PeriodFactory factory = new PeriodFactory();

    @Test
    void buildPeriods_shouldCreateSinglePeriod_whenFilterDurationEqualsGranularity() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        List<PeriodKey> periods = factory.buildPeriods(filter);

        assertThat(periods).hasSize(1);
        assertThat(periods.getFirst().from()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(periods.getFirst().to()).isEqualTo(Instant.parse("2026-06-02T00:00:00Z"));
    }

    @Test
    void buildPeriods_shouldCreateMultiplePeriods_whenFilterDurationIsMultipleOfGranularity() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-04T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        List<PeriodKey> periods = factory.buildPeriods(filter);

        assertThat(periods).hasSize(3);
        assertThat(periods.getFirst().from()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(periods.getFirst().to()).isEqualTo(Instant.parse("2026-06-02T00:00:00Z"));
        assertThat(periods.get(1).from()).isEqualTo(Instant.parse("2026-06-02T00:00:00Z"));
        assertThat(periods.get(1).to()).isEqualTo(Instant.parse("2026-06-03T00:00:00Z"));
        assertThat(periods.get(2).from()).isEqualTo(Instant.parse("2026-06-03T00:00:00Z"));
        assertThat(periods.get(2).to()).isEqualTo(Instant.parse("2026-06-04T00:00:00Z"));
    }

    @Test
    void buildPeriods_shouldCreateLastPeriodSmaller_thanGranularity_whenFilterDurationNotMultiple() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-10T00:00:00Z"),
                TimeGranularity.WEEK,
                null
        );

        List<PeriodKey> periods = factory.buildPeriods(filter);

        assertThat(periods).hasSize(2);
        assertThat(periods.getFirst().from()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(periods.getFirst().to()).isEqualTo(Instant.parse("2026-06-08T00:00:00Z"));
        assertThat(periods.get(1).from()).isEqualTo(Instant.parse("2026-06-08T00:00:00Z"));
        assertThat(periods.get(1).to()).isEqualTo(Instant.parse("2026-06-10T00:00:00Z"));
    }

    @Test
    void buildPeriods_shouldCreateOnlyOnePeriod_whenFilterDurationIsLessThanGranularity() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-05T00:00:00Z"),
                TimeGranularity.WEEK,
                null
        );

        List<PeriodKey> periods = factory.buildPeriods(filter);

        assertThat(periods).hasSize(1);
        assertThat(periods.getFirst().from()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(periods.getFirst().to()).isEqualTo(Instant.parse("2026-06-05T00:00:00Z"));
    }

    @Test
    void buildPeriods_shouldCreateDailyPeriods_whenGranularityIsDay() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-06T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        List<PeriodKey> periods = factory.buildPeriods(filter);

        assertThat(periods).hasSize(5);
        assertThat(periods)
                .extracting(PeriodKey::from)
                .containsExactly(
                        Instant.parse("2026-06-01T00:00:00Z"),
                        Instant.parse("2026-06-02T00:00:00Z"),
                        Instant.parse("2026-06-03T00:00:00Z"),
                        Instant.parse("2026-06-04T00:00:00Z"),
                        Instant.parse("2026-06-05T00:00:00Z")
                );
    }

    @Test
    void buildPeriods_shouldCreateWeeklyPeriods_whenGranularityIsWeek() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-15T00:00:00Z"),
                TimeGranularity.WEEK,
                null
        );

        List<PeriodKey> periods = factory.buildPeriods(filter);

        assertThat(periods).hasSizeGreaterThanOrEqualTo(2);
        assertThat(periods.getFirst().from()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(periods.getFirst().to()).isEqualTo(Instant.parse("2026-06-08T00:00:00Z"));
    }
}