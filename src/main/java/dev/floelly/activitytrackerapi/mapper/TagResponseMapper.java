package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.dto.response.TagsResponse;
import dev.floelly.activitytrackerapi.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring")
public interface TagResponseMapper {
    @Mapping(target = "id", source = "businessId")
    @Mapping(target = "color", source = "colorCode")
    TagResponse toResponse(Tag tag);

    default List<TagResponse> toResponseList(Iterable<Tag> tags) {
        return StreamSupport.stream(tags.spliterator(), false)
                .sorted(Comparator.comparingInt(Tag::getSortOrder).thenComparing(Tag::getLabel))
                .map(this::toResponse)
                .toList();
    }

    default TagsResponse toTagsResponse(Iterable<Tag> tags) {
        if (tags == null) {
            return null;
        }
        return new TagsResponse(toResponseList(tags));
    }
}
