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
    void toEntity_shouldMapRequestWithCategoryOnly() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", null);
        Activity activity = new Activity();
        Category category = new Category();
        category.setBusinessId("cat-1");
        category.setName("Sport");

        CategoryAllocation mappedAllocation = new CategoryAllocation();
        mappedAllocation.setPercentage(100);

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(commandMapper.toEntity(request)).thenReturn(mappedAllocation);

        CategoryAllocation result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAllocation);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getSubCategory()).isNull();
        assertThat(result.getPercentage()).isEqualTo(100);

        verify(categoryRepository).findByBusinessId("cat-1");
        verify(subCategoryRepository, never()).findByBusinessId(any());
        verify(categoryService, never()).isValidCategorySubCategoryRelation(any(), any());
        verify(commandMapper).toEntity(request);
    }

    @Test
    void toEntity_shouldMapRequestWithCategoryAndSubCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(75, "cat-1", "sub-1");
        Activity activity = new Activity();
        Category category = new Category();
        category.setBusinessId("cat-1");
        category.setName("Sport");

        SubCategory subCategory = new SubCategory();
        subCategory.setBusinessId("sub-1");
        subCategory.setName("Running");
        subCategory.setCategory(category);

        CategoryAllocation mappedAllocation = new CategoryAllocation();
        mappedAllocation.setPercentage(75);

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(subCategoryRepository.findByBusinessId("sub-1")).thenReturn(Optional.of(subCategory));
        when(categoryService.isValidCategorySubCategoryRelation(category, subCategory)).thenReturn(true);
        when(commandMapper.toEntity(request)).thenReturn(mappedAllocation);

        CategoryAllocation result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAllocation);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getSubCategory()).isSameAs(subCategory);
        assertThat(result.getPercentage()).isEqualTo(75);

        verify(categoryRepository).findByBusinessId("cat-1");
        verify(subCategoryRepository).findByBusinessId("sub-1");
        verify(categoryService).isValidCategorySubCategoryRelation(category, subCategory);
        verify(commandMapper).toEntity(request);
    }

    @Test
    void toEntity_shouldThrowNotFoundException_whenCategoryNotFound() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "missing-cat", null);
        Activity activity = new Activity();

        when(categoryRepository.findByBusinessId("missing-cat")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapper.toEntity(request, activity))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("missing-cat");

        verify(categoryRepository).findByBusinessId("missing-cat");
        verify(subCategoryRepository, never()).findByBusinessId(any());
        verify(categoryService, never()).isValidCategorySubCategoryRelation(any(), any());
        verify(commandMapper, never()).toEntity(any());
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
                .hasMessageContaining("SubCategory")
                .hasMessageContaining("missing-sub");

        verify(categoryRepository).findByBusinessId("cat-1");
        verify(subCategoryRepository).findByBusinessId("missing-sub");
        verify(categoryService, never()).isValidCategorySubCategoryRelation(any(), any());
        verify(commandMapper, never()).toEntity(any());
    }

    @Test
    void toEntity_shouldThrowBadRequestException_whenSubCategoryNotRelatedToCategory() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(100, "cat-1", "sub-1");
        Activity activity = new Activity();

        Category category = new Category();
        category.setBusinessId("cat-1");
        category.setName("Sport");

        SubCategory subCategory = new SubCategory();
        subCategory.setBusinessId("sub-1");
        subCategory.setName("Running");
        subCategory.setCategory(new Category()); // different category

        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(subCategoryRepository.findByBusinessId("sub-1")).thenReturn(Optional.of(subCategory));
        when(categoryService.isValidCategorySubCategoryRelation(category, subCategory)).thenReturn(false);

        assertThatThrownBy(() -> mapper.toEntity(request, activity))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SubCategory")
                .hasMessageContaining("Running")
                .hasMessageContaining("is not related to Category")
                .hasMessageContaining("Sport");

        verify(categoryRepository).findByBusinessId("cat-1");
        verify(subCategoryRepository).findByBusinessId("sub-1");
        verify(categoryService).isValidCategorySubCategoryRelation(category, subCategory);
        verify(commandMapper, never()).toEntity(any());
    }
}