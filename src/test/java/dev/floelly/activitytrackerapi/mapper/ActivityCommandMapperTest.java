package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateActivityAttributeRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateActivityRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateActivityRequest;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.ActivityAttribute;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringJUnitConfig(classes = {
        ActivityCommandMapperImpl.class,
        StringNormalizer.class
})
class ActivityCommandMapperTest {

    @Autowired
    private ActivityCommandMapper mapper;

    @Test
    void toEntity_ActivityRequest_shouldMapAndNormalizeFieldsCorrectly() {
        CreateActivityRequest request = new CreateActivityRequest(
                "title    ",
                "notes    ",
                Instant.parse("2023-01-01T00:00:00Z"),
                Instant.parse("2023-01-01T00:30:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(
                                100,
                                "category-id",
                                "sub-category-id"
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest("key-1", "val-1", true, 0),
                        new CreateActivityAttributeRequest("key-1", "val-1", true, 1)
                ),
                List.of("tag-id-1", "tag-id-2")
        );

        Activity activity = mapper.toEntity(request);

        assertThat(activity.getId()).isNull();
        assertThat(activity.getBusinessId()).isNull();
        assertThat(activity.getTitle()).isEqualTo("title");
        assertThat(activity.getNotes()).isEqualTo("notes");
        assertThat(activity.getCategoryAllocations()).isNull();
        assertThat(activity.getAttributes()).isNull();
        assertThat(activity.getTags()).isNull();
        assertThat(activity.getCreatedAt()).isNull();
        assertThat(activity.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_ActivityRequest_shouldReturnNullForNullRequest() {
        assertThat(mapper.toEntity((CreateActivityRequest) null)).isNull();
    }

    @Test
    void toEntity_CategoryAllocationRequest_shouldMapFieldsCorrectly() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(
                100,
                "category-id",
                "sub-category-id"
        );

        CategoryAllocation categoryAllocation = mapper.toEntity(request);

        assertThat(categoryAllocation.getId()).isNull();
        assertThat(categoryAllocation.getPercentage()).isEqualTo(request.percentage());
        assertThat(categoryAllocation.getCategory()).isNull();
        assertThat(categoryAllocation.getSubCategory()).isNull();
        assertThat(categoryAllocation.getActivity()).isNull();
    }

    @Test
    void toEntity_CategoryAllocation_shouldReturnNullForNullRequest() {
        assertThat(mapper.toEntity((CreateCategoryAllocationRequest) null)).isNull();
    }

    @Test
    void toEntity_ActivityAttribute_shouldMapAndNormalizeFieldsCorrectly() {
        CreateActivityAttributeRequest request = new CreateActivityAttributeRequest(
                "key      ",
                "     value",
                false,
                5
        );

        ActivityAttribute attribute = mapper.toEntity(request);

        assertThat(attribute.getId()).isNull();
        assertThat(attribute.getLabel()).isEqualTo("key");
        assertThat(attribute.getValue()).isEqualTo("value");
        assertThat(attribute.isShowInOverview()).isEqualTo(request.showInOverview());
        assertThat(attribute.getSortOrder()).isEqualTo(request.sortOrder());
        assertThat(attribute.getActivity()).isNull();
    }

    @Test
    void toEntity_ActivityAttribute_shouldReturnNullForNullRequest() {
        assertThat(mapper.toEntity((CreateActivityAttributeRequest) null)).isNull();
    }

    @Test
    void updateEntity_shouldReturnUnchangedEntityForNullRequest() {
        Activity activity = new Activity(
                1L,
                "activity-id",
                "Activity title",
                "Activity notes",
                Instant.parse("2023-01-01T00:00:00Z"),
                Instant.parse("2023-01-01T01:00:00Z"),
                Set.of(),
                Set.of(),
                Set.of(),
                Instant.parse("2023-01-01T00:00:00Z"),
                null
        );

        mapper.updateEntity(null, activity);

        assertThat(activity.getId()).isEqualTo(1L);
        assertThat(activity.getBusinessId()).isEqualTo("activity-id");
        assertThat(activity.getTitle()).isEqualTo("Activity title");
        assertThat(activity.getNotes()).isEqualTo("Activity notes");
        assertThat(activity.getStartAt()).hasToString("2023-01-01T00:00:00Z");
        assertThat(activity.getEndAt()).hasToString("2023-01-01T01:00:00Z");
        assertThat(activity.getCategoryAllocations()).isEmpty();
        assertThat(activity.getAttributes()).isEmpty();
        assertThat(activity.getTags()).isEmpty();
        assertThat(activity.getCreatedAt()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
        assertThat(activity.getUpdatedAt()).isNull();
    }

    @Test
    void updateEntity_shouldMapFieldsCorrectly() {
        UpdateActivityRequest activityDTO = new UpdateActivityRequest(
                "ignored-id",
                "\t\rActivity\n\n Title Update    ",
                "Some new notes\n\n\n",
                Instant.parse("2023-01-03T00:00:00Z"),
                Instant.parse("2023-01-03T01:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(100, "ignored-anyways", null))
        );
        Activity activity = new Activity(
                1L,
                "activity-id",
                "Activity title",
                "Activity notes",
                Instant.parse("2023-01-01T00:00:00Z"),
                Instant.parse("2023-01-01T01:00:00Z"),
                Set.of(),
                Set.of(),
                Set.of(),
                Instant.parse("2023-01-01T00:00:00Z"),
                null
        );

        mapper.updateEntity(activityDTO, activity);

        assertThat(activity.getId()).isEqualTo(1L);
        assertThat(activity.getBusinessId()).isEqualTo("activity-id");
        assertThat(activity.getTitle()).isEqualTo("Activity Title Update");
        assertThat(activity.getNotes()).isEqualTo("Some new notes");
        assertThat(activity.getStartAt()).hasToString("2023-01-03T00:00:00Z");
        assertThat(activity.getEndAt()).hasToString("2023-01-03T01:00:00Z");
        assertThat(activity.getCategoryAllocations()).isEmpty();
        assertThat(activity.getAttributes()).isEmpty();
        assertThat(activity.getTags()).isEmpty();
        assertThat(activity.getCreatedAt()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
        assertThat(activity.getUpdatedAt()).isNull();
    }
}