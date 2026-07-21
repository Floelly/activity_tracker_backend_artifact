package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.CategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoryResponse;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring")
public interface CategoryResponseMapper {
    @Mapping(target = "id", source = "businessId")
    @Mapping(target = "color", source = "colorCode")
    @Mapping(target = "icon", source = "iconName")
    @Mapping(target = "subcategories", source = "subCategories")
    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", source = "businessId")
    SubCategoryResponse toResponse(SubCategory subCategory);

    default List<SubCategoryResponse> mapSubCategories(Iterable<SubCategory> subCategories) {
        if (subCategories == null) {
            return List.of();
        }
        return StreamSupport.stream(subCategories.spliterator(), false)
                .sorted(Comparator.comparing(SubCategory::getName))
                .map(this::toResponse)
                .toList();
    }

    default List<CategoryResponse> toCategoryResponseList(Iterable<Category> categories) {
        return StreamSupport.stream(categories.spliterator(), false)
                .sorted(Comparator.comparing(Category::getName))
                .map(this::toResponse)
                .toList();
    }

    default CategoriesResponse toCategoriesResponse(Iterable<Category> categories) {
        if (categories == null) {
            return null;
        }
        return new CategoriesResponse(toCategoryResponseList(categories));
    }
}
