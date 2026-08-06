package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.feature.activityexport.dto.ActivityExportFilterDTO;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class ActivityExportSpecifications {

    public static Specification<Activity> withFilter(ActivityExportFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.categoryId() != null && !filter.categoryId().isBlank()) {
                Join<Object, Object> categoryAllocationJoin = root.join("categoryAllocations", JoinType.INNER);
                Join<Object, Object> categoryJoin = categoryAllocationJoin.join("category", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("businessId"), filter.categoryId()));
                query.distinct(true);
            }

            Instant startDate = parseInstant(filter.startDate());
            Instant endDate = parseInstant(filter.endDate());

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Instant parseInstant(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return Instant.parse(dateStr);
    }
}