package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateCategoryRequest;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringJUnitConfig({
        CategoryCommandMapperImpl.class,
        StringNormalizer.class
})
class CategoryCommandMapperTest {

    @Autowired
    private CategoryCommandMapper mapper;

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        CreateCategoryRequest category = new CreateCategoryRequest(
                "\t\rCategory\n\n Name      ",
                "#123456",
                "cat-icon",
                "some\n\n\r\n\n ran\n\n\tdom        \tdescription       \t"
        );

        Category result = mapper.toEntity(category);

        assertThat(result.getId()).isNull();
        assertThat(result.getBusinessId()).isNull();
        assertThat(result.getName()).isEqualTo("Category Name");
        assertThat(result.getColorCode()).isEqualTo("#123456");
        assertThat(result.getIconName()).isEqualTo("cat-icon");
        assertThat(result.getDescription()).isEqualTo("some\n\nran\n\ndom description");
    }

    @Test
    void updateEntity_shouldReturnUnchangedEntityForNullRequest() {
        Category category = new Category(
                1L,
                "cat-id",
                "Cat name",
                "#123456",
                "cat-icon",
                "cat-description",
                List.of()
        );

        mapper.updateEntity(null, category);

        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getBusinessId()).isEqualTo("cat-id");
        assertThat(category.getName()).isEqualTo("Cat name");
        assertThat(category.getColorCode()).isEqualTo("#123456");
        assertThat(category.getIconName()).isEqualTo("cat-icon");
        assertThat(category.getDescription()).isEqualTo("cat-description");
        assertThat(category.getSubCategories()).isEmpty();
    }

    @Test
    void updateEntity_shouldMapFieldsCorrectly() {
        UpdateCategoryRequest categoryDto = new UpdateCategoryRequest(
                "ignored-id",
                "\t\rCategory\n\n Name Update    ",
                "#000000",
                "cat-icon-update",
                "some\n\n\r\n\n ran\n\n\tdom        \tdescription       \t"
        );
        Category category = new Category(
                1L,
                "cat-id",
                "Cat name",
                "#123456",
                "cat-icon",
                "cat-description",
                List.of()
        );

        mapper.updateEntity(categoryDto, category);

        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getBusinessId()).isEqualTo("cat-id");
        assertThat(category.getName()).isEqualTo("Category Name Update");
        assertThat(category.getColorCode()).isEqualTo("#000000");
        assertThat(category.getIconName()).isEqualTo("cat-icon-update");
        assertThat(category.getDescription()).isEqualTo("some\n\nran\n\ndom description");
    }

    @Test
    void toEntity_shouldReturnNullForNullRequest() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}