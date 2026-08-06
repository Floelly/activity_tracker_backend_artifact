package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Tag;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public final class TagSpecifications {

    public static Specification<Tag> labelContains(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("label")), pattern);
    }
}
