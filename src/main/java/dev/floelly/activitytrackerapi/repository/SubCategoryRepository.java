package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    Optional<SubCategory> findByBusinessId(String businessId);

    //CHECKSTYLE:OFF
    @SuppressWarnings("PMD.MethodNamingConventions")
    List<SubCategory> findAllByCategory_BusinessId(String categoryBusinessId);
    //CHECKSTYLE:ON

    boolean existsByCategory(Category category);
}
