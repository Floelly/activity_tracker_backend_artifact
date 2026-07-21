package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateActivityAttributeRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateActivityRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateActivityRequest;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.ActivityAttribute;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedParagraphText;
import dev.floelly.activitytrackerapi.mapper.common.NormalizedSingleLineText;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = StringNormalizer.class)
public interface ActivityCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "title", qualifiedBy = NormalizedSingleLineText.class)
    @Mapping(target = "notes", qualifiedBy = NormalizedParagraphText.class)
    @Mapping(target = "categoryAllocations", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Activity toEntity(CreateActivityRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    @Mapping(target = "activity", ignore = true)
    CategoryAllocation toEntity(CreateCategoryAllocationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "label", source = "key", qualifiedBy = NormalizedSingleLineText.class)
    @Mapping(target = "value", qualifiedBy = NormalizedParagraphText.class)
    @Mapping(target = "activity", ignore = true)
    ActivityAttribute toEntity(CreateActivityAttributeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "title", qualifiedBy = NormalizedSingleLineText.class)
    @Mapping(target = "notes", qualifiedBy = NormalizedParagraphText.class)
    @Mapping(target = "categoryAllocations", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateActivityRequest activityRequest, @MappingTarget Activity activity);
}
