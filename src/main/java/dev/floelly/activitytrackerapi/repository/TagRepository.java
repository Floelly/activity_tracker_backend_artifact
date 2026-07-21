package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Set<Tag> findByBusinessIdIn(List<String> businessIds);
}
