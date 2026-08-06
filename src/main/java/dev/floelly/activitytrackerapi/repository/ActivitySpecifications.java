package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.dto.request.ActivityFilterDTO;
import dev.floelly.activitytrackerapi.entity.Activity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class ActivitySpecifications {

    public static Specification<Activity> withFilter(ActivityFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.fromStartAt() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("startAt"), filter.fromStartAt())
                );
            }

            if (filter.toStartAt() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("startAt"), filter.toStartAt())
                );
            }

            if (filter.category() != null && !filter.category().isEmpty()) {
                Join<Object, Object> categoryAllocationJoin = root.join("categoryAllocations", JoinType.INNER);
                Join<Object, Object> categoryJoin = categoryAllocationJoin.join("category", JoinType.INNER);

                predicates.add(categoryJoin.get("businessId").in(filter.category()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Activity> withExportFilter(String categoryId, Instant startDate, Instant endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null && !categoryId.isBlank()) {
                Join<Object, Object> categoryAllocationJoin = root.join("categoryAllocations", JoinType.INNER);
                Join<Object, Object> categoryJoin = categoryAllocationJoin.join("category", JoinType.INNER);

                predicates.add(cb.equal(categoryJoin.get("businessId"), categoryId));
                query.distinct(true);
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
