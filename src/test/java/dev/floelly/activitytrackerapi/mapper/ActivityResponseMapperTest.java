package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.response.*;
import dev.floelly.activitytrackerapi.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringJUnitConfig(ActivityResponseMapperImpl.class)
class ActivityResponseMapperTest {

    @Autowired
    private ActivityResponseMapper mapper;

    private Activity createValidBaseActivity() {
        return new Activity(
                1L,
                "business-id-1",
                "title",
                "notes",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Set.of(),
                Set.of(),
                Set.of(),
                Instant.now().plusSeconds(120),
                null
        );
    }

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        Activity activity = createValidBaseActivity();

        ActivityResponse response = mapper.toResponse(activity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(activity.getBusinessId());
        assertThat(response.title()).isEqualTo(activity.getTitle());
        assertThat(response.notes()).isEqualTo(activity.getNotes());
        assertThat(response.startAt()).isEqualTo(activity.getStartAt());
        assertThat(response.endAt()).isEqualTo(activity.getEndAt());
        assertThat(response.categoryAllocations()).isEmpty();
        assertThat(response.customValues()).isEmpty();
        assertThat(response.tags()).isEmpty();
        assertThat(response.createdAt()).isEqualTo(activity.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(activity.getUpdatedAt());
    }

    @Test
    void toResponse_shouldMapAndSortCategoryAllocations() {
        Activity activity = createValidBaseActivity();
        activity.setCategoryAllocations(Set.of(
                new CategoryAllocation(
                        1L,
                        50,
                        new Category(),
                        new SubCategory(),
                        activity
                ),
                new CategoryAllocation(
                        1L,
                        1,
                        new Category(),
                        new SubCategory(),
                        activity
                ),
                new CategoryAllocation(
                        1L,
                        4,
                        new Category(),
                        new SubCategory(),
                        activity
                ),
                new CategoryAllocation(
                        1L,
                        30,
                        new Category(),
                        new SubCategory(),
                        activity
                ),
                new CategoryAllocation(
                        1L,
                        15,
                        new Category(),
                        new SubCategory(),
                        activity
                )
        ));

        ActivityResponse response = mapper.toResponse(activity);

        assertThat(response.categoryAllocations()).hasSize(5);
        assertThat(response.categoryAllocations().stream()
                .map(ActivityCategoryAllocationResponse::percentage)).containsExactly(50, 30, 15, 4, 1);
    }

    @Test
    void toResponse_shouldMapAndSortActivityAttributes() {
        Activity activity = createValidBaseActivity();
        activity.setAttributes(Set.of(
                new ActivityAttribute(
                        5L,
                        "label A",
                        "value A",
                        false,
                        1,
                        activity
                ),
                new ActivityAttribute(
                        4L,
                        "label C",
                        "value C",
                        true,
                        2,
                        activity
                ),
                new ActivityAttribute(
                        3L,
                        "label D",
                        "value D",
                        false,
                        1,
                        activity
                ),
                new ActivityAttribute(
                        2L,
                        "label B",
                        "value B",
                        true,
                        2,
                        activity
                ),
                new ActivityAttribute(
                        1L,
                        "label E",
                        "value E",
                        false,
                        0,
                        activity
                )
        ));

        ActivityResponse response = mapper.toResponse(activity);

        assertThat(response.customValues()).hasSize(5);
        assertThat(response.customValues().stream()
                .map(ActivityAttributeResponse::key))
                .containsExactly("label E", "label A", "label D", "label B", "label C");
    }

    @Test
    void toResponse_shouldMapAndSortTags() {
        Activity activity = createValidBaseActivity();
        activity.setTags(Set.of(
                new Tag(
                        1L,
                        "tag-1",
                        "Tag A",
                        "#123456",
                        "some tag description",
                        5
                ),
                new Tag(
                        2L,
                        "tag-2",
                        "Tag B",
                        "#123456",
                        "some tag description",
                        4
                ),
                new Tag(
                        3L,
                        "tag-3",
                        "Tag C",
                        "#123456",
                        "some tag description",
                        4
                ),
                new Tag(
                        4L,
                        "tag-4",
                        "Tag D",
                        "#123456",
                        "some tag description",
                        2
                ),
                new Tag(
                        5L,
                        "tag-5",
                        "Tag E",
                        "#123456",
                        "some tag description",
                        1
                )
        ));

        ActivityResponse response = mapper.toResponse(activity);

        assertThat(response.tags()).hasSize(5);
        assertThat(response.tags().stream()
                .map(ActivityTagResponse::label))
                .containsExactly("Tag E", "Tag D", "Tag B", "Tag C", "Tag A");
    }

    @Test
    void toResponse_shouldReturnEmptyListsOnNullProps() {
        Activity activity = createValidBaseActivity();
        activity.setCategoryAllocations(null);
        activity.setAttributes(null);
        activity.setTags(null);

        ActivityResponse response = mapper.toResponse(activity);

        assertThat(response).isNotNull();
        assertThat(response.categoryAllocations()).isEmpty();
        assertThat(response.customValues()).isEmpty();
        assertThat(response.tags()).isEmpty();
    }

    @Test
    void toResponse_shouldReturnNullOnNullEntity() {
        assertThat(mapper.toResponse((Activity) null)).isNull();
    }

    @Test
    void toActivitiesResponse_shouldSortActivitiesByStartTime() {
        Activity activity1 = new Activity(
                1L,
                "id-1",
                "title-1",
                "notes",
                Instant.parse("2023-01-02T00:00:00Z"),
                Instant.parse("2023-01-02T00:01:00Z"),
                Set.of(),
                Set.of(),
                Set.of(),
                Instant.now(),
                null
        );
        Activity activity2 = new Activity(
                1L,
                "id-1",
                "title-1",
                "notes",
                Instant.parse("2023-01-03T00:00:00Z"),
                Instant.parse("2023-01-03T00:01:00Z"),
                Set.of(),
                Set.of(),
                Set.of(),
                Instant.now(),
                null
        );

        ActivitiesResponse response = mapper.toActivitiesResponse(new ArrayList<>(List.of(activity1, activity2)));

        assertThat(response).isNotNull();
        assertThat(response.activities()).isNotEmpty();
        assertThat(response.activities()).hasSize(2);
        assertThat(response.activities().stream().map(ActivityResponse::startAt))
                .containsExactly(Instant.parse("2023-01-02T00:00:00Z"), Instant.parse("2023-01-03T00:00:00Z"));

    }

    @Test
    void toActivitiesResponse_shouldReturnEmptyListForEmptyList() {
        ActivitiesResponse response = mapper.toActivitiesResponse(new ArrayList<>());
        assertThat(response).isNotNull();
        assertThat(response.activities()).isEmpty();
    }

    @Test
    void toActivitiesResponse_shouldReturnNullForNullList() {
        assertThat(mapper.toActivitiesResponse(null)).isNull();
    }
}