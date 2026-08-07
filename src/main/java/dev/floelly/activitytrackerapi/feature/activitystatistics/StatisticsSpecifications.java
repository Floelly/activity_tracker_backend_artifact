package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.request.StatisticsFilterDTO;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class StatisticsSpecifications {

    public static Specification<Activity> withFilter(StatisticsFilterDTO filter) {
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
}