package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import dev.floelly.activitytrackerapi.repository.SubCategoryRepository;
import dev.floelly.activitytrackerapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryAllocationMapper {

    private final ActivityCommandMapper commandMapper;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CategoryService categoryService;

    public CategoryAllocation toEntity(CreateCategoryAllocationRequest request, Activity activity) {
        String categoryBusinessId = request.categoryId();
        String subCategoryBusinessId = request.subCategoryId();

        Category category = categoryRepository.findByBusinessId(categoryBusinessId)
                .orElseThrow(() -> new NotFoundException("Category with id '" + categoryBusinessId + "' not found."));

        SubCategory subCategory = null;
        if (subCategoryBusinessId != null) {
            subCategory = subCategoryRepository.findByBusinessId(subCategoryBusinessId)
                    .orElseThrow(() -> new NotFoundException("SubCategory with id '" + subCategoryBusinessId + "' not found."));

            if (!categoryService.isValidCategorySubCategoryRelation(category, subCategory)) {
                throw new BadRequestException("SubCategory '" + subCategory.getName()
                        + "' (id: " + subCategory.getBusinessId() + ") is not related to Category '" + category.getName()
                        + "' (id: " + category.getBusinessId() + ").");
            }
        }

        CategoryAllocation allocation = commandMapper.toEntity(request);
        allocation.setActivity(activity);
        allocation.setCategory(category);
        allocation.setSubCategory(subCategory);
        return allocation;
    }
}