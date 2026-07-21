package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateSubCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.response.CategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoryResponse;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.exception.EntityDeletionConflictException;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.mapper.CategoryCommandMapper;
import dev.floelly.activitytrackerapi.mapper.CategoryResponseMapper;
import dev.floelly.activitytrackerapi.mapper.SubCategoryCommandMapper;
import dev.floelly.activitytrackerapi.mapper.SubCategoryResponseMapper;
import dev.floelly.activitytrackerapi.repository.CategoryAllocationRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import dev.floelly.activitytrackerapi.repository.SubCategoryRepository;
import dev.floelly.activitytrackerapi.validation.ValidTSID;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final TSID.Factory tsidFactory;

    private final CategoryRepository categoryRepository;
    private final CategoryCommandMapper categoryCommandMapper;
    private final CategoryResponseMapper categoryResponseMapper;

    private final SubCategoryRepository subCategoryRepository;
    private final SubCategoryCommandMapper subCategoryCommandMapper;
    private final SubCategoryResponseMapper subCategoryResponseMapper;
    private final CategoryAllocationRepository categoryAllocationRepository;

    @Transactional(readOnly = true)
    public CategoriesResponse findAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryResponseMapper.toCategoriesResponse(categories);
    }

    @Transactional
    public CategoryResponse registerNewCategory(CreateCategoryRequest categoryRequest) {
        Category category = categoryCommandMapper.toEntity(categoryRequest);
        category.setBusinessId(tsidFactory.generate().toString());
        categoryRepository.save(category);
        return categoryResponseMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(String categoryId, UpdateCategoryRequest categoryRequest) {
        if (!categoryRequest.id().equals(categoryId)) {
            throw new BadRequestException("Category id in request does not match the path variable");
        }
        Category category = findCategoryByBusinessId(categoryId);
        categoryCommandMapper.updateEntity(categoryRequest, category);
        return categoryResponseMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(@ValidTSID String categoryBusinessId) {
        Category category = findCategoryByBusinessId(categoryBusinessId);
        assertNoActivitiesAssignedToCategory(category);
        assertNoSubCategoriesAssignedToCategory(category);
        categoryRepository.delete(category);
    }

    @Transactional
    public SubCategoryResponse registerNewSubCategory(CreateSubCategoryRequest subCategoryRequest, String categoryBusinessId) {
        SubCategory subCategory = subCategoryCommandMapper.toEntity(subCategoryRequest);
        subCategory.setBusinessId(tsidFactory.generate().toString());
        subCategory.setCategory(findCategoryByBusinessId(categoryBusinessId));
        subCategoryRepository.save(subCategory);
        return subCategoryResponseMapper.toResponse(subCategory);
    }

    @Transactional(readOnly = true)
    public SubCategoriesResponse findAllSubCategoriesByCategoryBusinessId(String categoryBusinessId) {
        List<SubCategory> subCategories = subCategoryRepository.findAllByCategory_BusinessId(categoryBusinessId);
        return subCategoryResponseMapper.toSubCategoriesResponse(subCategories);
    }


    public boolean isValidCategorySubCategoryRelation(Category category, SubCategory subCategory) {
        return subCategory.getCategory().getBusinessId().equals(category.getBusinessId());
    }

    private Category findCategoryByBusinessId(String businessId) {
        return categoryRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new NotFoundException("Category not found " + businessId));
    }

    private void assertNoActivitiesAssignedToCategory(Category category) {
        if (categoryAllocationRepository.existsByCategory(category)) {
            throw new EntityDeletionConflictException("Category '" + category.getName() + "' (id: " + category.getBusinessId()
                    + ") has activities assigned to it.");
        }
    }

    private void assertNoSubCategoriesAssignedToCategory(Category category) {
        if (subCategoryRepository.existsByCategory(category)) {
            throw new EntityDeletionConflictException("Category '" + category.getName() + "' (id: " + category.getBusinessId()
                    + ") has sub categories assigned to it.");
        }
    }
}
