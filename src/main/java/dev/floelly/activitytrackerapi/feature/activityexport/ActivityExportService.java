package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.feature.activityexport.dto.ActivityExportFilterDTO;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityExportService {

    private static final String CSV_HEADER = "businessId,title,startAt,endAt,durationMinutes,categories,tags,notes";
    private static final String LINE_SEPARATOR = "\r\n";

    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public String exportCsv(ActivityExportFilterDTO filter) {
        validateFilter(filter);

        List<Activity> activities = activityRepository.findAll(ActivityExportSpecifications.withFilter(filter));

        activities.sort(Comparator.comparing(Activity::getStartAt));

        StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER).append(LINE_SEPARATOR);

        for (Activity activity : activities) {
            csv.append(escapeCsv(activity.getBusinessId())).append(",");
            csv.append(escapeCsv(activity.getTitle())).append(",");
            csv.append(escapeCsv(formatInstant(activity.getStartAt()))).append(",");
            csv.append(escapeCsv(formatInstant(activity.getEndAt()))).append(",");
            csv.append(calculateDurationMinutes(activity)).append(",");
            csv.append(escapeCsv(formatCategories(activity))).append(",");
            csv.append(escapeCsv(formatTags(activity))).append(",");
            csv.append(escapeCsv(activity.getNotes()));
            csv.append(LINE_SEPARATOR);
        }

        return csv.toString();
    }

    private void validateFilter(ActivityExportFilterDTO filter) {
        if (filter.categoryId() != null && !filter.categoryId().isBlank()) {
            categoryRepository.findByBusinessId(filter.categoryId())
                    .orElseThrow(() -> new BadRequestException(
                            "Category with id '" + filter.categoryId() + "' not found."));
        }

        Instant startDate = parseInstant(filter.startDate());
        Instant endDate = parseInstant(filter.endDate());

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
    }

    private static Instant parseInstant(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return Instant.parse(dateStr);
    }

    private static String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return instant.toString();
    }

    private static long calculateDurationMinutes(Activity activity) {
        if (activity.getStartAt() == null || activity.getEndAt() == null) {
            return 0;
        }
        return Duration.between(activity.getStartAt(), activity.getEndAt()).toMinutes();
    }

    private static String formatCategories(Activity activity) {
        if (activity.getCategoryAllocations() == null || activity.getCategoryAllocations().isEmpty()) {
            return "";
        }
        return activity.getCategoryAllocations().stream()
                .map(ca -> ca.getCategory().getBusinessId())
                .sorted()
                .collect(Collectors.joining(","));
    }

    private static String formatTags(Activity activity) {
        if (activity.getTags() == null || activity.getTags().isEmpty()) {
            return "";
        }
        return activity.getTags().stream()
                .map(Tag::getBusinessId)
                .sorted()
                .collect(Collectors.joining(","));
    }

    static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}