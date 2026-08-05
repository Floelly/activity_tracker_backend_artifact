package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.ActivitiesResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityAttributeResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityCategoryAllocationResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityCategoryResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivitySubCategoryResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityTagResponse;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.ActivityAttribute;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import dev.floelly.activitytrackerapi.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityResponseMapper {

    @Mapping(target = "id", source = "businessId")
    @Mapping(target = "durationInSeconds", expression =
            "java((int) java.time.Duration.between(activity.getStartAt(), activity.getEndAt()).getSeconds())")
    @Mapping(target = "customValues", ignore = true)
    @Mapping(target = "categoryAllocations", ignore = true)
    @Mapping(target = "tags", ignore = true)
    ActivityResponse mapBase(Activity activity);

    @Mapping(target = "key", source = "label")
    ActivityAttributeResponse toResponse(ActivityAttribute attribute);

    @Mapping(target = "subcategory", source = "subCategory")
    ActivityCategoryAllocationResponse toResponse(CategoryAllocation allocation);

    @Mapping(target = "id", source = "businessId")
    @Mapping(target = "color", source = "colorCode")
    ActivityTagResponse toResponse(Tag tag);

    @Mapping(target = "id", source = "businessId")
    @Mapping(target = "color", source = "colorCode")
    @Mapping(target = "icon", source = "iconName")
    ActivityCategoryResponse toResponse(Category category);

    @Mapping(target = "id", source = "businessId")
    ActivitySubCategoryResponse toResponse(SubCategory subCategory);

    // SORTING FOR TAGS, ATTRIBUTES, AND CATEGORY ALLOCATIONS
    default ActivityResponse toResponse(Activity activity) {
        if (activity == null) {
            return null;
        }
        ActivityResponse base = mapBase(activity);

        List<ActivityAttributeResponse> sortedCustomValues =
                activity.getAttributes() == null ? List.of()
                        : activity.getAttributes().stream()
                                .sorted(
                                        Comparator.comparingInt(ActivityAttribute::getSortOrder)
                                                .thenComparing(ActivityAttribute::getLabel)
                                )
                                .map(this::toResponse)
                                .toList();

        List<ActivityCategoryAllocationResponse> sortedAllocations =
                activity.getCategoryAllocations() == null ? List.of()
                        : activity.getCategoryAllocations().stream()
                                .sorted(Comparator.comparingInt(CategoryAllocation::getPercentage).reversed())
                                .map(this::toResponse)
                                .toList();

        List<ActivityTagResponse> sortedTags =
                activity.getTags() == null ? List.of()
                        : activity.getTags().stream()
                                .sorted(
                                        Comparator.comparingInt(Tag::getSortOrder)
                                                .thenComparing(Tag::getLabel)
                                )
                                .map(this::toResponse)
                                .toList();

        return new ActivityResponse(
                base.id(),
                base.title(),
                base.notes(),
                base.startAt(),
                base.endAt(),
                base.durationInSeconds(),
                sortedAllocations,
                sortedCustomValues,
                sortedTags,
                base.createdAt(),
                base.updatedAt()
        );
    }

    default ActivitiesResponse toActivitiesResponse(List<Activity> activities) {
        if (activities == null) {
            return new ActivitiesResponse(false, List.of());
        }
        List<Activity> mutableList = new ArrayList<>(activities);
        mutableList.sort(Comparator.comparing(Activity::getStartAt));
        return new ActivitiesResponse(
                false,
                mutableList.stream()
                        .map(this::toResponse)
                        .toList()
        );
    }
}
