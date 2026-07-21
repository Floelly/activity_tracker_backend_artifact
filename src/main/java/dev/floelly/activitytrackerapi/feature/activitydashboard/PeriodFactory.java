package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
class PeriodFactory {
    List<PeriodKey> buildPeriods(ActivitiesDashboardFilterDTO filter) {
        List<PeriodKey> periods = new ArrayList<>();
        Duration periodDuration = filter.granularity().toDuration();

        PeriodKey restPeriod = new PeriodKey(filter.from(), filter.to());
        while (restPeriod.isLongerThan(periodDuration)) {
            periods.add(restPeriod.takeFromStart(periodDuration));
            restPeriod = restPeriod.removeFromStart(periodDuration);
        }
        periods.add(restPeriod);
        return periods;
    }
}
