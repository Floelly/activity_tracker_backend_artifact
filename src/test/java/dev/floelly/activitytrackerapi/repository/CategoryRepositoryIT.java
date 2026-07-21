package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Category;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

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
}