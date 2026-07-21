package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.SubCategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoryResponse;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring")
public interface SubCategoryResponseMapper {

    @Mapping(target = "id", source = "businessId")
    SubCategoryResponse toResponse(SubCategory subCategory);

    default List<SubCategoryResponse> toSubCategoryResponseList(Iterable<SubCategory> subCategories) {
        return StreamSupport.stream(subCategories.spliterator(), false)
                .sorted(Comparator.comparing(SubCategory::getName))
                .map(this::toResponse)
                .toList();
    }

    default SubCategoriesResponse toSubCategoriesResponse(Iterable<SubCategory> subCategories) {
        if (subCategories == null) {
            return null;
        }
        return new SubCategoriesResponse(toSubCategoryResponseList(subCategories));
    }
}
