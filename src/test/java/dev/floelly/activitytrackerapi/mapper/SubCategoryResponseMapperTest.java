package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.SubCategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoryResponse;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringJUnitConfig(SubCategoryResponseMapperImpl.class)
class SubCategoryResponseMapperTest {

    @Autowired
    private SubCategoryResponseMapper mapper;

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        SubCategory subCategory = new SubCategory(
                1L,
                "businessId1",
                "Subcategory Name",
                "some description",
                new Category());

        SubCategoryResponse response = mapper.toResponse(subCategory);

        assertThat(response.id()).isEqualTo(subCategory.getBusinessId());
        assertThat(response.name()).isEqualTo(subCategory.getName());
        assertThat(response.description()).isEqualTo(subCategory.getDescription());
    }

    @Test
    void toResponse_shouldReturnNullForNullTag() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toSubCategoriesResponse_shouldSortSubCategoriesByName() {
        SubCategory subCategory1 = new SubCategory(
                1L,
                "businessId1",
                "SubCat B",
                "some description",
                null);
        SubCategory subCategory2 = new SubCategory(
                1L,
                "businessId3",
                "SubCat A",
                "some description",
                null);
        SubCategory subCategory3 = new SubCategory(
                1L,
                "businessId3",
                "SubCat C",
                "some description",
                null);

        SubCategoriesResponse response = mapper.toSubCategoriesResponse(List.of(subCategory1, subCategory2, subCategory3));

        assertThat(response).isNotNull();
        AssertionsForInterfaceTypes.assertThat(response.subcategories()).isNotEmpty();
        AssertionsForInterfaceTypes.assertThat(response.subcategories()).hasSize(3);
        AssertionsForInterfaceTypes.assertThat(response.subcategories().stream().map(SubCategoryResponse::name))
                .containsExactly("SubCat A", "SubCat B", "SubCat C");
    }

    @Test
    void toTagsResponse_shouldReturnEmptyListForEmptyIterable() {
        SubCategoriesResponse response = mapper.toSubCategoriesResponse(List.of());
        assertThat(response).isNotNull();
        assertThat(response.subcategories()).isEmpty();
    }

    @Test
    void toTagsResponse_shouldReturnNullForNullIterable() {
        assertThat(mapper.toSubCategoriesResponse(null)).isNull();
    }
}