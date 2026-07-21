package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateSubCategoryRequest;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedParagraphText;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedSingleLineText;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = StringNormalizer.class)
public interface SubCategoryCommandMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "name", qualifiedBy = NormalizedSingleLineText.class)
    @Mapping(target = "description", qualifiedBy = NormalizedParagraphText.class)
    @Mapping(target = "category", ignore = true)
    SubCategory toEntity(CreateSubCategoryRequest request);
}
