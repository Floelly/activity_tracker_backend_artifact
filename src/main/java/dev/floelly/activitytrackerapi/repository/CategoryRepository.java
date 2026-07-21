package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByBusinessId(String businessId);

    List<Category> findAllByBusinessIdIn(List<String> strings);
}
