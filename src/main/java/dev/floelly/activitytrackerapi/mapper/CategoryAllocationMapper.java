package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryAllocationMapper {

    private final ActivityCommandMapper commandMapper;

    public CategoryAllocation toEntity(CreateCategoryAllocationRequest request, Activity activity, Category category, SubCategory subCategory) {
        CategoryAllocation allocation = commandMapper.toEntity(request);
        allocation.setActivity(activity);
        allocation.setCategory(category);
        allocation.setSubCategory(subCategory);
        return allocation;
    }
}