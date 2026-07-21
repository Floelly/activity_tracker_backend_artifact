package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedParagraphText;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedSingleLineText;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = StringNormalizer.class)
public interface TagCommandMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "label", qualifiedBy = NormalizedSingleLineText.class)
    @Mapping(target = "colorCode", source = "color")
    @Mapping(target = "description", qualifiedBy = NormalizedParagraphText.class)
    Tag toEntity(CreateTagRequest request);
}
