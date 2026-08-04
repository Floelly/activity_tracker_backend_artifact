package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.request.ActivityFilterDTO;
import dev.floelly.activitytrackerapi.dto.request.CreateActivityRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateActivityRequest;
import dev.floelly.activitytrackerapi.dto.response.ActivitiesResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityResponse;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.ActivityAttribute;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.mapper.ActivityAttributeMapper;
import dev.floelly.activitytrackerapi.mapper.ActivityCommandMapper;
import dev.floelly.activitytrackerapi.mapper.ActivityResponseMapper;
import dev.floelly.activitytrackerapi.mapper.CategoryAllocationMapper;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.ActivitySpecifications;
import dev.floelly.activitytrackerapi.repository.TagRepository;
import io.hypersistence.tsid.TSID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final TSID.Factory tsidFactory;

    private final ActivityRepository repository;
    private final ActivityCommandMapper commandMapper;
    private final ActivityResponseMapper responseMapper;

    private final CategoryAllocationMapper categoryAllocationMapper;
    private final ActivityAttributeMapper activityAttributeMapper;

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public ActivityResponse findActivityByBusinessId(String businessId) {
        Activity activity = findByBusinessId(businessId);
        return responseMapper.toResponse(activity);
    }

    @Transactional(readOnly = true)
    public ActivitiesResponse findAllActivities(@NonNull ActivityFilterDTO filter) {
        List<Activity> activities = repository.findAll(ActivitySpecifications.withFilter(filter));
        return responseMapper.toActivitiesResponse(activities);
    }

    @Transactional
    public ActivityResponse registerNewActivity(CreateActivityRequest activityRequest) {
        if (!activityRequest.categoryAllocations().isEmpty()) {
            validateAllocations(activityRequest.categoryAllocations());
            validateAllocationSum(activityRequest.categoryAllocations());
        }
        Activity activity = commandMapper.toEntity(activityRequest);

        activity.setBusinessId(tsidFactory.generate().toString());
        activity.setCreatedAt(Instant.now());

        Set<CategoryAllocation> allocations = activityRequest.categoryAllocations().stream()
                .map(allocationRequest -> categoryAllocationMapper.toEntity(allocationRequest, activity))
                .collect(Collectors.toSet());
        activity.setCategoryAllocations(allocations);

        Set<ActivityAttribute> attributes = activityRequest.customValues().stream()
                .map(attributeRequest -> activityAttributeMapper.toEntity(attributeRequest, activity))
                .collect(Collectors.toSet());
        activity.setAttributes(attributes);

        Set<Tag> tags = tagRepository.findByBusinessIdIn(activityRequest.tagIds());
        activity.setTags(tags);

        repository.save(activity);

        return responseMapper.toResponse(activity);
    }

    @Transactional
    public void deleteActivity(String businessId) {
        Activity activity = findByBusinessId(businessId);
        repository.delete(activity);
    }

    @Transactional
    public ActivityResponse updateActivity(String businessId, UpdateActivityRequest activityRequest) {
        if (!activityRequest.id().equals(businessId)) {
            throw new BadRequestException("Activity id in request does not match the path variable");
        }
        List<CreateCategoryAllocationRequest> allocationRequests = activityRequest.categoryAllocations();
        if (!allocationRequests.isEmpty()) {
            validateAllocations(allocationRequests);
            validateAllocationSum(allocationRequests);
        }
        Activity activity = findByBusinessId(businessId);
        commandMapper.updateEntity(activityRequest, activity);
        mergeCategoryAllocations(activity, allocationRequests);
        // TODO: update custom values       // NOSONAR - a future implementation option
        // TODO: update tags                // NOSONAR - a future implementation option
        activity.setUpdatedAt(Instant.now());
        return responseMapper.toResponse(activity);
    }

    private Activity findByBusinessId(String businessId) {
        return repository.findByBusinessId(businessId)
                .orElseThrow(() -> generateNotFoundException("Activity", businessId));
    }

    @SuppressWarnings("PMD.LooseCoupling")
    private void mergeCategoryAllocations(Activity activity, List<CreateCategoryAllocationRequest> allocationRequests) {
        Map<String, CategoryAllocation> existingByKey = activity.getCategoryAllocations().stream()
                .collect(Collectors.toMap(
                        this::allocationKey,
                        Function.identity()
                ));

        List<String> requestedByKey = allocationRequests.stream()
                .map(this::allocationKey)
                .toList();

        activity.getCategoryAllocations().removeIf(existing ->
                !requestedByKey.contains(allocationKey(existing)));

        for (CreateCategoryAllocationRequest allocationRequest : allocationRequests) {
            String key = allocationKey(allocationRequest);
            CategoryAllocation existing = existingByKey.get(key);

            if (existing != null) {
                existing.setPercentage(allocationRequest.percentage());
            } else {
                activity.getCategoryAllocations().add(categoryAllocationMapper.toEntity(allocationRequest, activity));
            }
        }
    }

    private void validateAllocationSum(List<CreateCategoryAllocationRequest> allocations) {
        int percentageSum = allocations.stream()
                .mapToInt(CreateCategoryAllocationRequest::percentage).sum();
        if (percentageSum != 100) {
            throw new BadRequestException("Sum of category allocations must be 100%");
        }
    }

    private void validateAllocations(List<CreateCategoryAllocationRequest> allocationRequests) {
        long distinctCount = allocationRequests.stream()
                .map(this::allocationKey)
                .distinct()
                .count();
        if (distinctCount != allocationRequests.size()) {
            throw new BadRequestException("Duplicate category allocation keys found.");
        }
    }

    private String allocationKey(CreateCategoryAllocationRequest request) {
        return request.categoryId() + "::" + request.subCategoryId();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private String allocationKey(CategoryAllocation allocation) {
        String subCategoryId = allocation.getSubCategory() != null
                ? allocation.getSubCategory().getBusinessId()
                : null;
        return allocation.getCategory().getBusinessId() + "::" + subCategoryId;
    }

    private NotFoundException generateNotFoundException(String resource, String id) {
        return new NotFoundException(resource + " with id '" + id + "' not found.");
    }
}
