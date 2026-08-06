package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.entity.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringJUnitConfig(TagResponseMapperImpl.class)
class TagResponseMapperTest {

    @Autowired
    private TagResponseMapper mapper;

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        Tag tag = new Tag(
                1L,
                "businessId1",
                "Morning Run",
                "#123456",
                "some description",
                5);

        var response = mapper.toResponse(tag);

        assertThat(response.id()).isEqualTo(tag.getBusinessId());
        assertThat(response.label()).isEqualTo(tag.getLabel());
        assertThat(response.color()).isEqualTo(tag.getColorCode());
        assertThat(response.description()).isEqualTo(tag.getDescription());
        assertThat(response.sortOrder()).isEqualTo(tag.getSortOrder());
    }

    @Test
    void toResponse_shouldReturnNullForNullTag() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toTagsResponse_shouldSortTagsByLabel() {
        Tag tag1 = new Tag(
                1L,
                "businessId1",
                "Morning Run A",
                "#123456",
                "some description",
                5);
        Tag tag2 = new Tag(
                2L,
                "businessId2",
                "Morning Run C",
                "#123456",
                "some description",
                6);
        Tag tag3 = new Tag(
                3L,
                "businessId3",
                "Morning Run B",
                "#123456",
                "some description",
                6);
        Tag tag4 = new Tag(
                4L,
                "businessId4",
                "Morning Run D",
                "#123456",
                "some description",
                4);

        var response = mapper.toTagsResponse(List.of(tag1, tag2, tag3, tag4));

        assertThat(response).isNotNull();
        assertThat(response.tags()).isNotEmpty();
        assertThat(response.tags()).hasSize(4);
        assertThat(response.tags().stream().map(TagResponse::label))
                .containsExactly("Morning Run A", "Morning Run B", "Morning Run C", "Morning Run D");
    }

    @Test
    void toTagsResponse_shouldReturnEmptyListForEmptyIterable() {
        var response = mapper.toTagsResponse(List.of());
        assertThat(response).isNotNull();
        assertThat(response.tags()).isEmpty();
    }

    @Test
    void toTagsResponse_shouldReturnNullForNullIterable() {
        assertThat(mapper.toTagsResponse(null)).isNull();
    }
}