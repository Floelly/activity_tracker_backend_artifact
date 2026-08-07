package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Category;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryIT extends MySQLContainerInitializer {

    @Autowired
    private CategoryRepository categoryRepository;

    private static @NotNull Category createCategory(String businessId) {
        return new Category(
                null,
                businessId,
                "NAME",
                "#123456",
                "default",
                "some description",
                null,
                List.of()
        );
    }

    @Test
    void findByBusinessId_shouldReturnCategoryWhenExists() {
        Category category = new Category(
                null,
                "ABC123456789",
                "Running",
                "#123456",
                "shoe",
                "some description",
                null,
                List.of()
        );

        Category saved = categoryRepository.save(category);

        var result = categoryRepository.findByBusinessId("ABC123456789");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getBusinessId()).isEqualTo("ABC123456789");
        assertThat(result.get().getName()).isEqualTo("Running");
        assertThat(result.get().getColorCode()).isEqualTo("#123456");
        assertThat(result.get().getIconName()).isEqualTo("shoe");
        assertThat(result.get().getDescription()).isEqualTo("some description");
    }

    @Test
    void findByBusinessId_shouldReturnEmptyWhenNotFound() {
        var result = categoryRepository.findByBusinessId("ZZZ123456789");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByBusinessIdIn_shouldReturnAllMatchingCategories() {
        Category allowedCategory1 = createCategory("0000123456787");
        Category allowedCategory2 = createCategory("0000123456786");
        Category notAllowedCategory = createCategory("0000123456785");

        categoryRepository.saveAll(List.of(allowedCategory1, allowedCategory2, notAllowedCategory));

        var result = categoryRepository.findAllByBusinessIdIn(
                List.of(allowedCategory1.getBusinessId(), allowedCategory2.getBusinessId()));

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .containsExactlyInAnyOrder(allowedCategory1, allowedCategory2)
                .doesNotContain(notAllowedCategory);
    }

    @Test
    void findAllByBusinessIdIn_shouldReturnEmptyWhenNotFound() {
        var result = categoryRepository.findAllByBusinessIdIn(List.of("0000123456789", "0000123456788"));

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByBusinessIdIn_shouldReturnEmptyWhenGibberish() {
        var result = categoryRepository.findAllByBusinessIdIn(List.of("miau", "wuff"));

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByDeletedAtIsNull_shouldReturnOnlyNonDeletedCategories() {
        Category active = categoryRepository.save(createCategory("0000000000001"));
        Category deleted = categoryRepository.save(createCategory("0000000000002"));
        deleted.setDeletedAt(Instant.now());
        categoryRepository.save(deleted);

        var result = categoryRepository.findAllByDeletedAtIsNull();

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting(Category::getBusinessId)
                .containsExactly("0000000000001");
    }

    @Test
    void findByBusinessIdAndDeletedAtIsNull_shouldReturnCategory_whenNotDeleted() {
        Category active = categoryRepository.save(createCategory("0000000000003"));

        var result = categoryRepository.findByBusinessIdAndDeletedAtIsNull("0000000000003");

        assertThat(result).isPresent();
        assertThat(result.get().getBusinessId()).isEqualTo("0000000000003");
    }

    @Test
    void findByBusinessIdAndDeletedAtIsNull_shouldReturnEmpty_whenDeleted() {
        Category deleted = categoryRepository.save(createCategory("0000000000004"));
        deleted.setDeletedAt(Instant.now());
        categoryRepository.save(deleted);

        var result = categoryRepository.findByBusinessIdAndDeletedAtIsNull("0000000000004");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByBusinessIdInAndDeletedAtIsNull_shouldExcludeDeletedCategories() {
        Category active1 = categoryRepository.save(createCategory("0000000000005"));
        Category active2 = categoryRepository.save(createCategory("0000000000006"));
        Category deleted = categoryRepository.save(createCategory("0000000000007"));
        deleted.setDeletedAt(Instant.now());
        categoryRepository.save(deleted);

        var result = categoryRepository.findAllByBusinessIdInAndDeletedAtIsNull(
                List.of("0000000000005", "0000000000006", "0000000000007"));

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(Category::getBusinessId)
                .containsExactlyInAnyOrder("0000000000005", "0000000000006");
    }
}