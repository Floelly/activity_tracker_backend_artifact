package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubCategoryRepositoryIT extends MySQLContainerInitializer {

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByBusinessId_shouldReturnSubCategoryWhenExists() {
        Category category = categoryRepository.save(new Category(
                null,
                "CAT1234567890",
                "Sports",
                "#123456",
                "shoe",
                "some description",
                List.of()
        ));

        SubCategory subCategory = subCategoryRepository.save(new SubCategory(
                null,
                "SUB1234567890",
                "Running",
                "some description",
                category
        ));

        var result = subCategoryRepository.findByBusinessId("SUB1234567890");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(subCategory.getId());
        assertThat(result.get().getBusinessId()).isEqualTo("SUB1234567890");
        assertThat(result.get().getName()).isEqualTo("Running");
        assertThat(result.get().getDescription()).isEqualTo("some description");
        assertThat(result.get().getCategory().getBusinessId()).isEqualTo("CAT1234567890");
    }

    @Test
    void findByBusinessId_shouldReturnEmptyWhenNotFound() {
        var result = subCategoryRepository.findByBusinessId("SUB9999999999");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByCategory_BusinessId_shouldReturnAllMatchingSubCategories() {
        Category sports = categoryRepository.save(new Category(
                null,
                "CAT1234567891",
                "Sports",
                "#123456",
                "shoe",
                "some description",
                List.of()
        ));

        Category work = categoryRepository.save(new Category(
                null,
                "CAT1234567892",
                "Work",
                "#654321",
                "briefcase",
                "other description",
                List.of()
        ));

        subCategoryRepository.save(new SubCategory(
                null,
                "SUB1234567891",
                "Running",
                "running description",
                sports
        ));

        subCategoryRepository.save(new SubCategory(
                null,
                "SUB1234567892",
                "Cycling",
                "cycling description",
                sports
        ));

        subCategoryRepository.save(new SubCategory(
                null,
                "SUB1234567893",
                "Meetings",
                "meetings description",
                work
        ));

        List<SubCategory> result = subCategoryRepository.findAllByCategory_BusinessId("CAT1234567891");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(SubCategory::getBusinessId)
                .containsExactlyInAnyOrder("SUB1234567891", "SUB1234567892");
        assertThat(result)
                .extracting(SubCategory::getName)
                .containsExactlyInAnyOrder("Running", "Cycling");
        assertThat(result)
                .extracting(subCategory -> subCategory.getCategory().getBusinessId())
                .containsOnly("CAT1234567891");
    }

    @Test
    void findAllByCategory_BusinessId_shouldReturnEmptyListWhenNoSubCategoriesMatch() {
        Category sports = categoryRepository.save(new Category(
                null,
                "CAT1234567894",
                "Sports",
                "#123456",
                "shoe",
                "some description",
                List.of()
        ));

        subCategoryRepository.save(new SubCategory(
                null,
                "SUB1234567894",
                "Running",
                "running description",
                sports
        ));

        List<SubCategory> result = subCategoryRepository.findAllByCategory_BusinessId("CAT9999999999");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByCategory_shouldBeTruthyWhenNoSubCategoriesMatch() {
        Category sports = categoryRepository.save(new Category(
                null,
                "CAT1234567894",
                "Sports",
                "#123456",
                "shoe",
                "some description",
                List.of()
        ));

        subCategoryRepository.save(new SubCategory(
                null,
                "SUB1234567894",
                "Running",
                "running description",
                sports
        ));

        boolean result = subCategoryRepository.existsByCategory(sports);

        assertThat(result).isTrue();
    }

    @Test
    void existsByCategory_shouldBeFalsyWhenNoSubCategoriesMatch() {
        Category sports = categoryRepository.save(new Category(
                null,
                "CAT1234567894",
                "Sports",
                "#123456",
                "shoe",
                "some description",
                List.of()
        ));
        Category work = categoryRepository.save(new Category(
                null,
                "CAT1234567896",
                "Work",
                "#123456",
                "shoe",
                "some description",
                List.of()
        ));

        subCategoryRepository.save(new SubCategory(
                null,
                "SUB1234567894",
                "Running",
                "running description",
                sports
        ));

        boolean result = subCategoryRepository.existsByCategory(work);

        assertThat(result).isFalse();
    }
}