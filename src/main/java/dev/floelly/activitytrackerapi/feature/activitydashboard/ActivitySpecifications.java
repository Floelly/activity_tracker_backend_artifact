package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class ActivitySpecifications {

    public static Specification<Activity> withFilter(ActivitiesDashboardFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    cb.greaterThan(root.get("endAt"), filter.from())
            );

            predicates.add(
                    cb.lessThan(root.get("startAt"), filter.to())
            );

            if (filter.hasCategoryFilter()) {
                Join<Object, Object> categoryAllocationJoin = root.join("categoryAllocations", JoinType.INNER);
                Join<Object, Object> categoryJoin = categoryAllocationJoin.join("category", JoinType.INNER);

                predicates.add(categoryJoin.get("businessId").in(filter.categoryIds()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
