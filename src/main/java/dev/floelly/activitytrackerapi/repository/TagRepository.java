package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {
    Set<Tag> findByBusinessIdIn(List<String> businessIds);
}
