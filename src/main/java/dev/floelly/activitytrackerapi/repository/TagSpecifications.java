package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Tag;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public final class TagSpecifications {

    public static Specification<Tag> labelContains(String query) {
        return (root, queryObj, cb) ->
                cb.like(cb.lower(root.get("label")), "%" + query.toLowerCase() + "%");
    }
}
