package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateCategoryRequest;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedParagraphText;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedSingleLineText;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = StringNormalizer.class)
public interface CategoryCommandMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "name", qualifiedBy = NormalizedSingleLineText.class)
    @Mapping(target = "colorCode", source = "color")
    @Mapping(target = "iconName", source = "icon")
    @Mapping(target = "description", qualifiedBy = NormalizedParagraphText.class)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "name", qualifiedBy = NormalizedSingleLineText.class)
    @Mapping(target = "colorCode", source = "color")
    @Mapping(target = "iconName", source = "icon")
    @Mapping(target = "description", qualifiedBy = NormalizedParagraphText.class)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    void updateEntity(UpdateCategoryRequest categoryRequest, @MappingTarget Category category);
}
