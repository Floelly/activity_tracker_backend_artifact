package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryAllocationRepository extends JpaRepository<CategoryAllocation, Long> {
    boolean existsByCategory(Category category);
}
