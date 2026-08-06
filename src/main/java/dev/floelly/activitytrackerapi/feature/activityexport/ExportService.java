package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportService {

    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;

    public String exportCsv(ExportFilterDTO filter) {
        // Validate category existence
        if (filter.categoryId() != null && !filter.categoryId().isBlank()) {
            if (categoryRepository.findByBusinessId(filter.categoryId()).isEmpty()) {
                throw new BadRequestException("Category not found: " + filter.categoryId());
            }
        }

        // Validate date range
        if (filter.startDate() != null && filter.endDate() != null && filter.startDate().isAfter(filter.endDate())) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }

        // Build specification
        Specification<Activity> spec = buildSpecification(filter);

        // Query activities
        List<Activity> activities = activityRepository.findAll(spec);

        // Generate CSV
        return buildCsv(activities);
    }

    private Specification<Activity> buildSpecification(ExportFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.categoryId() != null && !filter.categoryId().isBlank()) {
                var categoryAllocationJoin = root.join("categoryAllocations", JoinType.INNER);
                var categoryJoin = categoryAllocationJoin.join("category", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("businessId"), filter.categoryId()));
                query.distinct(true);
            }

            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), filter.startDate()));
            }

            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), filter.endDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String buildCsv(List<Activity> activities) {
        StringBuilder csv = new StringBuilder();
        csv.append("businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\n");

        for (Activity activity : activities) {
            csv.append(escapeCsvField(activity.getBusinessId())).append(',');
            csv.append(escapeCsvField(activity.getTitle())).append(',');
            csv.append(escapeCsvField(activity.getStartAt().toString())).append(',');
            csv.append(escapeCsvField(activity.getEndAt().toString())).append(',');
            csv.append(ChronoUnit.MINUTES.between(activity.getStartAt(), activity.getEndAt())).append(',');

            // Categories: sorted comma-separated businessIds
            String categories = activity.getCategoryAllocations().stream()
                    .map(ca -> ca.getCategory().getBusinessId())
                    .sorted()
                    .collect(Collectors.joining(","));
            csv.append(escapeCsvField(categories)).append(',');

            // Tags: sorted comma-separated businessIds
            String tags = activity.getTags().stream()
                    .map(tag -> tag.getBusinessId())
                    .sorted()
                    .collect(Collectors.joining(","));
            csv.append(escapeCsvField(tags)).append(',');

            // Notes: nullable
            String notes = activity.getNotes() != null ? activity.getNotes() : "";
            csv.append(escapeCsvField(notes)).append('\n');
        }

        return csv.toString();
    }

    private String escapeCsvField(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}