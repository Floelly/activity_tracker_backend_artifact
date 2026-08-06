package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.ActivitySpecifications;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;

    public String exportCsv(String categoryId, String startDateStr, String endDateStr) {
        if (categoryId != null && !categoryId.isBlank()) {
            categoryRepository.findByBusinessId(categoryId)
                    .orElseThrow(() -> new BadRequestException("Category not found: " + categoryId));
        }

        Instant startDate = null;
        if (startDateStr != null && !startDateStr.isBlank()) {
            startDate = Instant.parse(startDateStr);
        }

        Instant endDate = null;
        if (endDateStr != null && !endDateStr.isBlank()) {
            endDate = Instant.parse(endDateStr);
        }

        if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
            throw new BadRequestException("startDate must be before endDate");
        }

        Specification<Activity> spec = ActivitySpecifications.withExportFilter(categoryId, startDate, endDate);
        List<Activity> activities = activityRepository.findAll(spec);

        return toCsv(activities);
    }

    private String toCsv(List<Activity> activities) {
        StringBuilder sb = new StringBuilder();
        sb.append("businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\n");

        for (Activity activity : activities) {
            sb.append(activity.getBusinessId()).append(",");
            sb.append(escapeCsv(activity.getTitle())).append(",");
            sb.append(activity.getStartAt().toString()).append(",");
            sb.append(activity.getEndAt().toString()).append(",");
            sb.append(ChronoUnit.MINUTES.between(activity.getStartAt(), activity.getEndAt())).append(",");

            String categories = activity.getCategoryAllocations().stream()
                    .map(CategoryAllocation::getCategory)
                    .map(cat -> cat.getBusinessId())
                    .sorted()
                    .collect(Collectors.joining(","));
            sb.append(categories).append(",");

            String tags = activity.getTags().stream()
                    .map(Tag::getBusinessId)
                    .sorted()
                    .collect(Collectors.joining(","));
            sb.append(tags).append(",");

            sb.append(escapeCsv(activity.getNotes()));
            sb.append("\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("\"") || value.contains(",") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}