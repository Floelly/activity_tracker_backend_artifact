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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryAllocationMapperTest {

    @Mock
    private ActivityCommandMapper commandMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SubCategoryRepository subCategoryRepository;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryAllocationMapper mapper;

    @Test
    void toEntity_shouldMapSuccessfully_withSubCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(75, "cat-1", "sub-1");
        Activity activity = new Activity();
        Category category = new Category();
        category.setBusinessId("cat-1");
        SubCategory subCategory = new SubCategory();
        subCategory.setBusinessId("sub-1");
        subCategory.setCategory(category);
        CategoryAllocation mappedAllocation = new CategoryAllocation();

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(subCategoryRepository.findByBusinessId("sub-1")).thenReturn(Optional.of(subCategory));
        when(categoryService.isValidCategorySubCategoryRelation(category, subCategory)).thenReturn(true);
        when(commandMapper.toEntity(request)).thenReturn(mappedAllocation);

        CategoryAllocation result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAllocation);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getSubCategory()).isSameAs(subCategory);

        verify(commandMapper).toEntity(request);
        verify(categoryRepository).findByBusinessId("cat-1");
        verify(subCategoryRepository).findByBusinessId("sub-1");
        verify(categoryService).isValidCategorySubCategoryRelation(category, subCategory);
    }

    @Test
    void toEntity_shouldMapSuccessfully_withoutSubCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", null);
        Activity activity = new Activity();
        Category category = new Category();
        category.setBusinessId("cat-1");
        CategoryAllocation mappedAllocation = new CategoryAllocation();

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(commandMapper.toEntity(request)).thenReturn(mappedAllocation);

        CategoryAllocation result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAllocation);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getSubCategory()).isNull();

        verify(commandMapper).toEntity(request);
        verify(categoryRepository).findByBusinessId("cat-1");
        verifyNoInteractions(subCategoryRepository);
        verifyNoInteractions(categoryService);
    }

    @Test
    void toEntity_shouldThrowNotFoundException_whenCategoryNotFound() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "missing-cat", null);
        Activity activity = new Activity();

        when(categoryRepository.findByBusinessId("missing-cat")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapper.toEntity(request, activity))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id 'missing-cat' not found.");

        verify(categoryRepository).findByBusinessId("missing-cat");
        verifyNoInteractions(subCategoryRepository);
        verifyNoInteractions(categoryService);
        verifyNoInteractions(commandMapper);
    }

    @Test
    void toEntity_shouldThrowNotFoundException_whenSubCategoryNotFound() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", "missing-sub");
        Activity activity = new Activity();
        Category category = new Category();
        category.setBusinessId("cat-1");

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(subCategoryRepository.findByBusinessId("missing-sub")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapper.toEntity(request, activity))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("SubCategory with id 'missing-sub' not found.");

        verify(categoryRepository).findByBusinessId("cat-1");
        verify(subCategoryRepository).findByBusinessId("missing-sub");
        verifyNoInteractions(categoryService);
        verifyNoInteractions(commandMapper);
    }

    @Test
    void toEntity_shouldThrowBadRequestException_whenSubCategoryNotRelated() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", "sub-1");
        Activity activity = new Activity();
        Category category = new Category();
        category.setBusinessId("cat-1");
        category.setName("Category A");
        SubCategory subCategory = new SubCategory();
        subCategory.setBusinessId("sub-1");
        subCategory.setName("Sub A");
        subCategory.setCategory(new Category()); // different category

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(subCategoryRepository.findByBusinessId("sub-1")).thenReturn(Optional.of(subCategory));
        when(categoryService.isValidCategorySubCategoryRelation(category, subCategory)).thenReturn(false);

        assertThatThrownBy(() -> mapper.toEntity(request, activity))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("SubCategory 'Sub A' (id: sub-1) is not related to Category 'Category A' (id: cat-1).");

        verify(categoryRepository).findByBusinessId("cat-1");
        verify(subCategoryRepository).findByBusinessId("sub-1");
        verify(categoryService).isValidCategorySubCategoryRelation(category, subCategory);
        verifyNoInteractions(commandMapper);
    }
}