package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.CategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringJUnitConfig(CategoryResponseMapperImpl.class)
class CategoryResponseMapperTest {

    @Autowired
    private CategoryResponseMapper mapper;

    @Test
    void toResponse_shouldMapFieldsCorrectly_OnEmptySubCategoriesList() {
        Category category = new Category(
                1L,
                "businessId1",
                "Category Name",
                "#123456",
                "cat-icon",
                "some description",
                List.of()
        );

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(category.getBusinessId());
        assertThat(response.name()).isEqualTo(category.getName());
        assertThat(response.color()).isEqualTo(category.getColorCode());
        assertThat(response.icon()).isEqualTo(category.getIconName());
        assertThat(response.description()).isEqualTo(category.getDescription());
        assertThat(response.subcategories()).isNotNull();
        assertThat(response.subcategories()).isEmpty();
    }

    @Test
    void toResponse_shouldMapFieldsCorrectly_OnNonEmptySubCategoriesList() {
        Category category = new Category(
                1L,
                "businessId1",
                "Category Name",
                "#123456",
                "cat-icon",
                "some description",
                List.of()
        );
        SubCategory subCategory = new SubCategory(
                10L,
                "sub-business-id",
                "sub-name",
                "description",
                category
        );
        category.setSubCategories(List.of(subCategory));

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.subcategories()).isNotNull();
        assertThat(response.subcategories()).hasSize(1);
        assertThat(response.subcategories().getFirst().id()).isEqualTo(subCategory.getBusinessId());
        assertThat(response.subcategories().getFirst().name()).isEqualTo(subCategory.getName());
        assertThat(response.subcategories().getFirst().description()).isEqualTo(subCategory.getDescription());
    }

    @Test
    void toResponse_shouldSortSubCategoriesByName() {
        Category category = new Category(
                1L,
                "businessId1",
                "Category Name",
                "#123456",
                "cat-icon",
                "some description",
                List.of()
        );
        category.setSubCategories(List.of(
                new SubCategory(
                        10L,
                        "sub1-business-id",
                        "sub-C",
                        "description",
                        category
                ),
                new SubCategory(
                        11L,
                        "sub2-business-id",
                        "sub-A",
                        "description",
                        category
                ),
                new SubCategory(
                        12L,
                        "sub3-business-id",
                        "sub-B",
                        "description",
                        category
                )));

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.subcategories()).isNotNull();
        assertThat(response.subcategories()).hasSize(3);
        assertThat(response.subcategories().getFirst().name()).isEqualTo("sub-A");
        assertThat(response.subcategories().get(1).name()).isEqualTo("sub-B");
        assertThat(response.subcategories().get(2).name()).isEqualTo("sub-C");
    }

    @Test
    void toResponse_shouldReturnNullForNullCategory() {
        assertThat(mapper.toResponse((Category) null)).isNull();
    }

    @Test
    void toCategoriesResponse_shouldSortCategoriesByName() {
        Category category1 = new Category(
                1L,
                "businessId1",
                "Cat B",
                "#123456",
                "cat-icon",
                "some description",
                List.of()
        );
        Category category2 = new Category(
                2L,
                "businessId2",
                "Cat A",
                "#123456",
                "cat-icon",
                "some description",
                List.of()
        );

        CategoriesResponse response = mapper.toCategoriesResponse(List.of(category1, category2));

        assertThat(response).isNotNull();
        assertThat(response.categories()).isNotEmpty();
        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories().stream().map(CategoryResponse::name))
                .containsExactly("Cat A", "Cat B");
    }

    @Test
    void toCategoriesResponse_shouldReturnEmptyListForEmptyIterable() {
        CategoriesResponse response = mapper.toCategoriesResponse(List.of());
        assertThat(response).isNotNull();
        assertThat(response.categories()).isEmpty();
    }

    @Test
    void toCategoriesResponse_shouldReturnNullForNullIterable() {
        assertThat(mapper.toCategoriesResponse(null)).isNull();
    }
}