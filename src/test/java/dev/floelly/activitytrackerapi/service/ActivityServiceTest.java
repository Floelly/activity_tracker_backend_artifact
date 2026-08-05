package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.request.*;
import dev.floelly.activitytrackerapi.dto.response.ActivitiesResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityResponse;
import dev.floelly.activitytrackerapi.entity.*;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.mapper.ActivityCommandMapper;
import dev.floelly.activitytrackerapi.mapper.ActivityResponseMapper;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import dev.floelly.activitytrackerapi.repository.SubCategoryRepository;
import dev.floelly.activitytrackerapi.repository.TagRepository;
import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private TSID.Factory tsidFactory;
    @Mock
    private ActivityRepository repository;
    @Mock
    private ActivityCommandMapper commandMapper;
    @Mock
    private ActivityResponseMapper responseMapper;
    @Mock
    private CategoryService categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SubCategoryRepository subCategoryRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ActivityService service;

    @Test
    void findActivityByBusinessId_shouldReturnMappedResponse() {
        String businessId = "act-1";
        Activity activity = new Activity();
        ActivityResponse expected = mock(ActivityResponse.class);

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(responseMapper.toResponse(activity)).thenReturn(expected);

        ActivityResponse result = service.findActivityByBusinessId(businessId);

        assertThat(result).isSameAs(expected);
        verify(repository).findByBusinessId(businessId);
        verify(responseMapper).toResponse(activity);
    }

    @Test
    void findActivityByBusinessId_shouldThrowWhenNotFound() {
        String businessId = "missing";

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findActivityByBusinessId(businessId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Activity")
                .hasMessageContaining(businessId);

        verify(responseMapper, never()).toResponse(any(Activity.class));
    }

    @Test
    void findAllActivities_shouldReturnMappedResponse_whenFilterHasFromStartAt() {
        Instant fromStartAt = Instant.parse("2026-05-25T09:00:00Z");
        ActivityFilterDTO filter = new ActivityFilterDTO(fromStartAt, null, null);

        Activity activity1 = new Activity();
        Activity activity2 = new Activity();
        ActivitiesResponse expected = mock(ActivitiesResponse.class);

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(activity1, activity2));
        when(responseMapper.toActivitiesResponse(List.of(activity1, activity2))).thenReturn(expected);

        ActivitiesResponse result = service.findAllActivities(filter);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll(any(Specification.class));
        verify(responseMapper).toActivitiesResponse(List.of(activity1, activity2));
    }

    @Test
    void findAllActivities_shouldReturnMappedResponse_whenFilterHasToStartAt() {
        Instant toStartAt = Instant.parse("2026-05-25T10:00:00Z");
        ActivityFilterDTO filter = new ActivityFilterDTO(null, toStartAt, null);

        Activity activity = new Activity();
        ActivitiesResponse expected = mock(ActivitiesResponse.class);

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(activity));
        when(responseMapper.toActivitiesResponse(List.of(activity))).thenReturn(expected);

        ActivitiesResponse result = service.findAllActivities(filter);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findAllActivities_shouldReturnMappedResponse_whenFilterHasBothValues() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T09:00:00Z"),
                Instant.parse("2026-05-25T11:00:00Z"),
                null
        );

        Activity activity = new Activity();
        ActivitiesResponse expected = mock(ActivitiesResponse.class);

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(activity));
        when(responseMapper.toActivitiesResponse(List.of(activity))).thenReturn(expected);

        ActivitiesResponse result = service.findAllActivities(filter);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findAllActivities_shouldReturnMappedResponse_whenFilterHasNullValues() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                null, null, null
        );

        Activity activity = new Activity();
        ActivitiesResponse expected = mock(ActivitiesResponse.class);

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(activity));
        when(responseMapper.toActivitiesResponse(List.of(activity))).thenReturn(expected);

        ActivitiesResponse result = service.findAllActivities(filter);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findAllActivities_shouldThrowArgumentNullException_whenFilterIsNull() {
        assertThatThrownBy(() -> service.findAllActivities(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void registerNewActivity_shouldSaveMappedActivityWithoutListsAndReturnResponse() {
        CreateActivityRequest request = mock(CreateActivityRequest.class);
        Activity mappedActivity = new Activity();
        ActivityResponse expected = mock(ActivityResponse.class);

        when(commandMapper.toEntity(request)).thenReturn(mappedActivity);
        when(request.categoryAllocations()).thenReturn(List.of());
        when(request.customValues()).thenReturn(List.of());
        when(request.tagIds()).thenReturn(List.of());

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("act-tsid");

        when(tagRepository.findByBusinessIdIn(argThat(Collection::isEmpty))).thenReturn(Set.of());
        when(responseMapper.toResponse(mappedActivity)).thenReturn(expected);

        ActivityResponse result = service.registerNewActivity(request);

        assertThat(result).isSameAs(expected);
        assertThat(mappedActivity.getBusinessId()).isEqualTo("act-tsid");
        assertThat(mappedActivity.getCategoryAllocations()).isEmpty();
        assertThat(mappedActivity.getAttributes()).isEmpty();
        assertThat(mappedActivity.getTags()).isEmpty();

        verify(repository).save(mappedActivity);
        verify(responseMapper).toResponse(mappedActivity);
    }

    @Test
    void registerNewActivity_shouldSaveMappedActivityWithListsAndReturnResponse() {
        CreateActivityRequest request = mock(CreateActivityRequest.class);
        Activity mappedActivity = new Activity();
        ActivityResponse expected = mock(ActivityResponse.class);

        CreateActivityAttributeRequest attributeRequest = mock(CreateActivityAttributeRequest.class);
        ActivityAttribute attribute = new ActivityAttribute();

        CreateCategoryAllocationRequest allocationRequest1 = mock(CreateCategoryAllocationRequest.class);
        CreateCategoryAllocationRequest allocationRequest2 = mock(CreateCategoryAllocationRequest.class);

        CategoryAllocation allocation1 = new CategoryAllocation();
        allocation1.setPercentage(90);

        CategoryAllocation allocation2 = new CategoryAllocation();
        allocation2.setPercentage(10);

        Category category1 = new Category();
        SubCategory subCategory1 = new SubCategory();
        Tag tag = new Tag();

        when(commandMapper.toEntity(request)).thenReturn(mappedActivity);
        when(request.categoryAllocations()).thenReturn(List.of(allocationRequest1, allocationRequest2));
        when(request.customValues()).thenReturn(List.of(attributeRequest));
        when(request.tagIds()).thenReturn(List.of("tag-1"));
        when(allocationRequest1.categoryId()).thenReturn("cat-1");
        when(allocationRequest1.subCategoryId()).thenReturn("sub-1");
        when(allocationRequest1.percentage()).thenReturn(80);
        when(allocationRequest2.categoryId()).thenReturn("cat-2");
        when(allocationRequest2.subCategoryId()).thenReturn(null);
        when(allocationRequest2.percentage()).thenReturn(20);

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("act-tsid");

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category1));
        when(categoryRepository.findByBusinessId("cat-2")).thenReturn(Optional.of(new Category()));
        when(subCategoryRepository.findByBusinessId("sub-1")).thenReturn(Optional.of(subCategory1));
        when(commandMapper.toEntity(allocationRequest1)).thenReturn(allocation1);
        when(commandMapper.toEntity(allocationRequest2)).thenReturn(allocation2);
        when(categoryService.isValidCategorySubCategoryRelation(category1, subCategory1)).thenReturn(true);

        when(commandMapper.toEntity(attributeRequest)).thenReturn(attribute);

        when(tagRepository.findByBusinessIdIn(request.tagIds())).thenReturn(Set.of(tag));
        when(responseMapper.toResponse(mappedActivity)).thenReturn(expected);

        ActivityResponse result = service.registerNewActivity(request);

        assertThat(result).isSameAs(expected);
        assertThat(mappedActivity.getBusinessId()).isEqualTo("act-tsid");
        assertThat(mappedActivity.getAttributes()).containsExactly(attribute);
        assertThat(attribute.getActivity()).isSameAs(mappedActivity);
        assertThat(mappedActivity.getCategoryAllocations()).containsExactlyInAnyOrder(allocation1, allocation2);
        assertThat(mappedActivity.getTags()).containsExactly(tag);
        assertThat(mappedActivity.getCreatedAt()).isNotNull();
        assertThat(mappedActivity.getUpdatedAt()).isNull();

        verify(repository).save(mappedActivity);
        verify(responseMapper).toResponse(mappedActivity);
        verify(tagRepository).findByBusinessIdIn(request.tagIds());
    }

    @Test
    void registerNewActivity_shouldThrowWhenCategoryAllocationSumIsNot100() {
        CreateActivityRequest request = mock(CreateActivityRequest.class);

        CreateCategoryAllocationRequest allocationRequest1 = mock(CreateCategoryAllocationRequest.class);
        CreateCategoryAllocationRequest allocationRequest2 = mock(CreateCategoryAllocationRequest.class);

        when(request.categoryAllocations()).thenReturn(List.of(allocationRequest1, allocationRequest2));
        when(allocationRequest1.categoryId()).thenReturn("cat-1");
        when(allocationRequest1.subCategoryId()).thenReturn(null);
        when(allocationRequest1.percentage()).thenReturn(60);
        when(allocationRequest2.categoryId()).thenReturn("cat-2");
        when(allocationRequest2.subCategoryId()).thenReturn(null);
        when(allocationRequest2.percentage()).thenReturn(30);

        assertThatThrownBy(() -> service.registerNewActivity(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("100%");

        verify(repository, never()).save(any());
    }

    @Test
    void registerNewActivity_shouldThrowWhenAllocationKeysAreDuplicated() {
        CreateActivityRequest request = mock(CreateActivityRequest.class);
        CreateCategoryAllocationRequest allocationRequest1 = new CreateCategoryAllocationRequest(50, "cat-1", null);
        CreateCategoryAllocationRequest allocationRequest2 = new CreateCategoryAllocationRequest(50, "cat-1", null);
        when(request.categoryAllocations()).thenReturn(List.of(allocationRequest1, allocationRequest2));

        assertThatThrownBy(() -> service.registerNewActivity(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Duplicate category allocation keys found.");

        verify(repository, never()).save(any());
    }

    @Test
    void registerNewActivity_shouldThrowWhenCategoryNotFound() {
        CreateActivityRequest request = mock(CreateActivityRequest.class);
        Activity mappedActivity = new Activity();
        CreateCategoryAllocationRequest allocationRequest = mock(CreateCategoryAllocationRequest.class);

        when(commandMapper.toEntity(request)).thenReturn(mappedActivity);
        when(request.categoryAllocations()).thenReturn(List.of(allocationRequest));

        when(allocationRequest.categoryId()).thenReturn("missing-cat");
        when(allocationRequest.subCategoryId()).thenReturn(null);
        when(allocationRequest.percentage()).thenReturn(100);

        when(categoryRepository.findByBusinessId("missing-cat")).thenReturn(Optional.empty());

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("act-tsid");

        assertThatThrownBy(() -> service.registerNewActivity(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("missing-cat");

        verify(repository, never()).save(any());
    }

    @Test
    void registerNewActivity_shouldThrowWhenSubCategoryNotFound() {
        CreateActivityRequest request = mock(CreateActivityRequest.class);
        Activity mappedActivity = new Activity();
        CreateCategoryAllocationRequest allocationRequest = mock(CreateCategoryAllocationRequest.class);

        when(commandMapper.toEntity(request)).thenReturn(mappedActivity);
        when(request.categoryAllocations()).thenReturn(List.of(allocationRequest));

        when(allocationRequest.categoryId()).thenReturn("known-cat");
        when(allocationRequest.subCategoryId()).thenReturn("missing-sub-cat");
        when(allocationRequest.percentage()).thenReturn(100);

        when(categoryRepository.findByBusinessId("known-cat")).thenReturn(Optional.of(new Category()));
        when(subCategoryRepository.findByBusinessId("missing-sub-cat")).thenReturn(Optional.empty());

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("act-tsid");

        assertThatThrownBy(() -> service.registerNewActivity(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Sub")
                .hasMessageContaining("missing-sub-cat");

        verify(repository, never()).save(any());
    }

    @Test
    void registerNewActivity_shouldThrowWhenSubCategoryNotRelatedToCategory() {
        CreateActivityRequest request = mock(CreateActivityRequest.class);
        Activity mappedActivity = new Activity();
        CreateCategoryAllocationRequest allocationRequest = mock(CreateCategoryAllocationRequest.class);

        Category category = new Category();
        category.setBusinessId("cat-1");
        category.setName("Category A");

        SubCategory subCategory = new SubCategory();
        subCategory.setBusinessId("sub-1");
        subCategory.setName("Sub A");

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setPercentage(100);

        when(commandMapper.toEntity(request)).thenReturn(mappedActivity);
        when(request.categoryAllocations()).thenReturn(List.of(allocationRequest));
        when(allocationRequest.categoryId()).thenReturn("cat-1");
        when(allocationRequest.subCategoryId()).thenReturn("sub-1");
        when(allocationRequest.percentage()).thenReturn(100);

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(subCategoryRepository.findByBusinessId("sub-1")).thenReturn(Optional.of(subCategory));
        when(categoryService.isValidCategorySubCategoryRelation(category, subCategory)).thenReturn(false);

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("act-tsid");

        assertThatThrownBy(() -> service.registerNewActivity(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not related");

        verify(repository, never()).save(any());
    }


    @Test
    void deleteActivity_shouldDeleteByBusinessId_whenEntityIsPresent() {
        String businessId = "act-1";
        Activity activity = new Activity();
        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));

        service.deleteActivity(businessId);

        verify(repository).findByBusinessId(businessId);
        verify(repository).delete(activity);
    }

    @Test
    void deleteActivity_shouldThrowWhenEntityNotFound() {
        String businessId = "act-1";
        when(repository.findByBusinessId(businessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteActivity(businessId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Activity")
                .hasMessageContaining(businessId);

        verify(repository, never()).delete((Activity) any());
    }

    @Test
    void updateActivity_shouldThrowWhenRequestIdDoesNotMatchPathId() {
        String businessId = "act-1";
        UpdateActivityRequest request = new UpdateActivityRequest(
                "act-2",
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> service.updateActivity(businessId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Activity id in request does not match the path variable");

        verify(repository, never()).findByBusinessId(any());
    }

    @Test
    void updateActivity_shouldThrowWhenActivityNotFound() {
        String businessId = "act-1";
        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(),
                List.of(),
                List.of()
        );

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateActivity(businessId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Activity")
                .hasMessageContaining(businessId);

        verify(repository, never()).delete((Activity) any());
    }

    @Test
    void updateActivity_shouldThrowWhenAllocationSumIsNot100() {
        String businessId = "act-1";
        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(60, "cat-1", null),
                        new CreateCategoryAllocationRequest(30, "cat-2", null)
                ),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> service.updateActivity(businessId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sum of category allocations must be 100%");

        verify(repository, never()).findByBusinessId(any());
    }

    @Test
    void updateActivity_shouldThrowWhenAllocationKeysAreDuplicated() {
        String businessId = "act-1";
        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(50, "cat-1", "sub-cat-1"),
                        new CreateCategoryAllocationRequest(50, "cat-1", "sub-cat-1")
                ),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> service.updateActivity(businessId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate category allocation keys found.");

        verify(repository, never()).findByBusinessId(any());
    }

    @Test
    void updateActivity_shouldUpdatePlainFieldsAndUpdatedAtWhenNoAllocationsProvided() {
        String businessId = "act-1";
        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>());

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(),
                List.of(),
                List.of()
        );

        ActivityResponse response = mock(ActivityResponse.class);

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(responseMapper.toResponse(activity)).thenReturn(response);

        ActivityResponse result = service.updateActivity(businessId, request);

        assertThat(result).isSameAs(response);
        assertThat(activity.getUpdatedAt()).isNotNull();

        verify(repository).findByBusinessId(businessId);
        verify(commandMapper).updateEntity(request, activity);
        verify(responseMapper).toResponse(activity);
    }

    @Test
    void updateActivity_shouldAddNewAllocationWhenItDoesNotExistYet() {
        String businessId = "act-1";

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>());

        Category category = new Category();
        category.setBusinessId("cat-1");
        category.setName("Sport");

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(100, "cat-1", null)),
                List.of(),
                List.of()
        );

        ActivityResponse response = mock(ActivityResponse.class);
        CategoryAllocation mappedAllocation = new CategoryAllocation();
        mappedAllocation.setPercentage(100);

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(commandMapper.toEntity(any(CreateCategoryAllocationRequest.class))).thenReturn(mappedAllocation);
        when(responseMapper.toResponse(activity)).thenReturn(response);

        ActivityResponse result = service.updateActivity(businessId, request);

        assertThat(result).isSameAs(response);
        assertThat(activity.getCategoryAllocations()).hasSize(1);

        CategoryAllocation allocation = activity.getCategoryAllocations().iterator().next();
        assertThat(allocation.getActivity()).isSameAs(activity);
        assertThat(allocation.getCategory()).isSameAs(category);
        assertThat(allocation.getSubCategory()).isNull();
        assertThat(allocation.getPercentage()).isEqualTo(100);
    }

    @Test
    void updateActivity_shouldUpdateExistingAllocationPercentage() {
        String businessId = "act-1";

        Category category = new Category();
        category.setBusinessId("cat-1");

        CategoryAllocation existingAllocation = new CategoryAllocation();
        existingAllocation.setCategory(category);
        existingAllocation.setSubCategory(null);
        existingAllocation.setPercentage(40);

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>(Set.of(existingAllocation)));
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>());

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(100, "cat-1", null)),
                List.of(),
                List.of()
        );

        ActivityResponse response = mock(ActivityResponse.class);

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(responseMapper.toResponse(activity)).thenReturn(response);

        ActivityResponse result = service.updateActivity(businessId, request);

        assertThat(result).isSameAs(response);
        assertThat(activity.getCategoryAllocations()).hasSize(1);
        assertThat(existingAllocation.getPercentage()).isEqualTo(100);
    }

    @Test
    void updateActivity_shouldUpdateTagsWhenProvided() {
        String businessId = "act-1";

        Tag existingTag = new Tag();
        existingTag.setBusinessId("tag-1");
        existingTag.setLabel("Tag 1");

        Tag newTag = new Tag();
        newTag.setBusinessId("tag-2");
        newTag.setLabel("Tag 2");

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>(Set.of(existingTag)));

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(),
                List.of(),
                List.of("tag-1", "tag-2")
        );

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(tagRepository.findByBusinessIdIn(request.tagIds())).thenReturn(Set.of(existingTag, newTag));
        when(responseMapper.toResponse(activity)).thenReturn(mock(ActivityResponse.class));

        service.updateActivity(businessId, request);

        assertThat(activity.getTags()).containsExactlyInAnyOrder(existingTag, newTag);
    }

    @Test
    void updateActivity_shouldUpdateCustomValuesWhenProvided() {
        String businessId = "act-1";

        ActivityAttribute existingAttribute = new ActivityAttribute();
        existingAttribute.setLabel("weight");
        existingAttribute.setValue("80");
        existingAttribute.setShowInOverview(true);
        existingAttribute.setSortOrder(0);

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setTags(new HashSet<>());
        activity.setAttributes(new HashSet<>(Set.of(existingAttribute)));

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(),
                List.of(
                        new CreateActivityAttributeRequest("weight", "81", true, 0),
                        new CreateActivityAttributeRequest("height", "180", false, 1)
                ),
                List.of()
        );

        ActivityResponse response = mock(ActivityResponse.class);
        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(commandMapper.toEntity(any(CreateActivityAttributeRequest.class))).thenAnswer(invocation -> {
            CreateActivityAttributeRequest source = invocation.getArgument(0);
            ActivityAttribute attribute = new ActivityAttribute();
            attribute.setLabel(source.key());
            attribute.setValue(source.value());
            attribute.setShowInOverview(source.showInOverview());
            attribute.setSortOrder(source.sortOrder());
            return attribute;
        });
        when(responseMapper.toResponse(activity)).thenReturn(response);

        service.updateActivity(businessId, request);

        assertThat(activity.getAttributes()).hasSize(2);
        assertThat(activity.getAttributes().stream().map(ActivityAttribute::getLabel))
                .containsExactlyInAnyOrder("weight", "height");
    }

    @Test
    void updateActivity_shouldRemoveTags_onUpdateRequest() {
        String businessId = "act-1";

        Tag tag1 = new Tag();
        tag1.setBusinessId("tag-1");
        tag1.setLabel("Tag 1");

        Tag tag2 = new Tag();
        tag2.setBusinessId("tag-2");
        tag2.setLabel("Tag 2");

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>(Set.of(tag1, tag2)));

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(),
                List.of(),
                List.of("tag-1")
        );

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(tagRepository.findByBusinessIdIn(request.tagIds())).thenReturn(Set.of(tag1));
        when(responseMapper.toResponse(activity)).thenReturn(mock(ActivityResponse.class));

        service.updateActivity(businessId, request);

        assertThat(activity.getTags()).containsExactly(tag1);
    }

    @Test
    void updateActivity_shouldRemoveCustomValues_onUpdateRequest() {
        String businessId = "act-1";

        ActivityAttribute weight = new ActivityAttribute();
        weight.setLabel("weight");
        weight.setValue("80");

        ActivityAttribute height = new ActivityAttribute();
        height.setLabel("height");
        height.setValue("180");

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setTags(new HashSet<>());
        activity.setAttributes(new HashSet<>(Set.of(weight, height)));

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(),
                List.of(new CreateActivityAttributeRequest("weight", "80", true, 0)),
                List.of()
        );

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(responseMapper.toResponse(activity)).thenReturn(mock(ActivityResponse.class));

        service.updateActivity(businessId, request);

        assertThat(activity.getAttributes()).hasSize(1);
        assertThat(activity.getAttributes().iterator().next().getLabel()).isEqualTo("weight");
    }

    @Test
    void updateActivity_shouldRemoveAllocationsThatAreNotPresentInRequest() {
        String businessId = "act-1";

        Category oldCategory = new Category();
        oldCategory.setBusinessId("cat-old");
        SubCategory oldSubCategory = new SubCategory(5L, "sub-cat-id", "name", "desctiption", oldCategory);

        CategoryAllocation oldAllocation = new CategoryAllocation();
        oldAllocation.setCategory(oldCategory);
        oldAllocation.setSubCategory(oldSubCategory);
        oldAllocation.setPercentage(100);

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>(Set.of(oldAllocation)));
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>());

        Category newCategory = new Category();
        newCategory.setBusinessId("cat-new");
        newCategory.setName("New");

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(100, "cat-new", null)),
                List.of(),
                List.of()
        );

        CategoryAllocation newAllocation = new CategoryAllocation();
        newAllocation.setPercentage(100);

        ActivityResponse response = mock(ActivityResponse.class);

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(categoryRepository.findByBusinessId("cat-new")).thenReturn(Optional.of(newCategory));
        when(commandMapper.toEntity(any(CreateCategoryAllocationRequest.class))).thenReturn(newAllocation);
        when(responseMapper.toResponse(activity)).thenReturn(response);

        service.updateActivity(businessId, request);

        assertThat(activity.getCategoryAllocations()).hasSize(1);
        CategoryAllocation remaining = activity.getCategoryAllocations().iterator().next();
        assertThat(remaining.getCategory().getBusinessId()).isEqualTo("cat-new");
    }

    @Test
    void updateActivity_shouldThrowWhenCategoryOfNewAllocationIsNotFound() {
        String businessId = "act-1";

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>());

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(100, "cat-1", null)),
                List.of(),
                List.of()
        );

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateActivity(businessId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("cat-1");
    }

    @Test
    void updateActivity_shouldThrowWhenSubCategoryDoesNotBelongToCategory() {
        String businessId = "act-1";

        Activity activity = new Activity();
        activity.setBusinessId(businessId);
        activity.setCategoryAllocations(new HashSet<>());
        activity.setAttributes(new HashSet<>());
        activity.setTags(new HashSet<>());

        Category category = new Category();
        category.setBusinessId("cat-1");
        category.setName("Sport");

        SubCategory subCategory = new SubCategory();
        subCategory.setBusinessId("sub-1");
        subCategory.setName("Run");

        UpdateActivityRequest request = new UpdateActivityRequest(
                businessId,
                "Updated title",
                "Updated notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(100, "cat-1", "sub-1")),
                List.of(),
                List.of()
        );

        when(repository.findByBusinessId(businessId)).thenReturn(Optional.of(activity));
        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(subCategoryRepository.findByBusinessId("sub-1")).thenReturn(Optional.of(subCategory));
        when(categoryService.isValidCategorySubCategoryRelation(category, subCategory)).thenReturn(false);

        assertThatThrownBy(() -> service.updateActivity(businessId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SubCategory")
                .hasMessageContaining("is not related to Category");
    }
}