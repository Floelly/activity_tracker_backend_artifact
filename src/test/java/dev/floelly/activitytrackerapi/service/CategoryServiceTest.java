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
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.mapper.CategoryCommandMapper;
import dev.floelly.activitytrackerapi.mapper.CategoryResponseMapper;
import dev.floelly.activitytrackerapi.mapper.SubCategoryCommandMapper;
import dev.floelly.activitytrackerapi.mapper.SubCategoryResponseMapper;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import dev.floelly.activitytrackerapi.repository.SubCategoryRepository;
import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private TSID.Factory tsidFactory;

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryCommandMapper categoryCommandMapper;
    @Mock
    private CategoryResponseMapper categoryResponseMapper;

    @Mock
    private SubCategoryRepository subCategoryRepository;
    @Mock
    private SubCategoryCommandMapper subCategoryCommandMapper;
    @Mock
    private SubCategoryResponseMapper subCategoryResponseMapper;

    @InjectMocks
    private CategoryService service;

    @Test
    void findAllCategories_shouldReturnMappedResponse() {
        List<Category> categories = List.of(new Category(), new Category());
        CategoriesResponse expected = mock(CategoriesResponse.class);

        when(categoryRepository.findAllByDeletedAtIsNull()).thenReturn(categories);
        when(categoryResponseMapper.toCategoriesResponse(categories)).thenReturn(expected);

        CategoriesResponse result = service.findAllCategories();

        assertThat(result).isSameAs(expected);
        verify(categoryRepository).findAllByDeletedAtIsNull();
        verify(categoryResponseMapper).toCategoriesResponse(categories);
    }

    @Test
    void findAllSubCategoriesByCategoryBusinessId_shouldReturnMappedResponse() {
        String categoryBusinessId = "cat-1";
        List<SubCategory> subCategories = List.of(new SubCategory(), new SubCategory());
        SubCategoriesResponse expected = mock(SubCategoriesResponse.class);

        when(subCategoryRepository.findAllByCategory_BusinessId(categoryBusinessId)).thenReturn(subCategories);
        when(subCategoryResponseMapper.toSubCategoriesResponse(subCategories)).thenReturn(expected);

        SubCategoriesResponse result = service.findAllSubCategoriesByCategoryBusinessId(categoryBusinessId);

        assertThat(result).isSameAs(expected);
        verify(subCategoryRepository).findAllByCategory_BusinessId(categoryBusinessId);
        verify(subCategoryResponseMapper).toSubCategoriesResponse(subCategories);
    }

    @Test
    void registerNewCategory_shouldMapAssignBusinessIdSaveAndReturnResponse() {
        CreateCategoryRequest request = mock(CreateCategoryRequest.class);
        Category category = new Category();
        CategoryResponse expected = mock(CategoryResponse.class);

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("cat-tsid");

        when(categoryCommandMapper.toEntity(request)).thenReturn(category);
        when(categoryResponseMapper.toResponse(category)).thenReturn(expected);

        CategoryResponse result = service.registerNewCategory(request);

        assertThat(result).isSameAs(expected);
        assertThat(category.getBusinessId()).isEqualTo("cat-tsid");
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_shouldUpdateCategory_whenRequestIsValid() {
        String categoryId = "01HZX3K8MTSY9E8L4KQK123456";
        UpdateCategoryRequest request = new UpdateCategoryRequest(
                categoryId,
                "Sport",
                "#FF0000",
                null,
                "Alle Sportaktivitäten"
        );

        Category category = new Category();
        category.setBusinessId(categoryId);

        CategoryResponse response = new CategoryResponse(
                categoryId,
                "Sport",
                "#FF0000",
                null,
                "Alle Sportaktivitäten",
                List.of()
        );

        when(categoryRepository.findByBusinessId(categoryId)).thenReturn(Optional.of(category));
        when(categoryResponseMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = service.updateCategory(categoryId, request);

        assertThat(result).isSameAs(response);
        verify(categoryRepository).findByBusinessId(categoryId);
        verify(categoryCommandMapper).updateEntity(request, category);
        verify(categoryResponseMapper).toResponse(category);
        verifyNoMoreInteractions(categoryRepository, categoryCommandMapper, categoryResponseMapper);
    }

    @Test
    void updateCategory_shouldThrowBadRequest_whenPathIdDoesNotMatchRequestId() {
        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "01HZX3K8MTSY9E8L4KQK999999",
                "Sport",
                "#FF0000",
                "bike",
                "Alle Sportaktivitäten"
        );

        assertThatThrownBy(() -> service.updateCategory("01HZX3K8MTSY9E8L4KQK123456", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category id in request does not match the path variable");

        verifyNoInteractions(categoryRepository, categoryCommandMapper, categoryResponseMapper);
    }

    @Test
    void updateCategory_shouldThrowNotFound_whenCategoryDoesNotExist() {
        String categoryId = "01HZX3K8MTSY9E8L4KQK123456";
        UpdateCategoryRequest request = new UpdateCategoryRequest(
                categoryId,
                "Sport",
                "#FF0000",
                null,
                "Alle Sportaktivitäten"
        );

        when(categoryRepository.findByBusinessId(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(categoryId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found " + categoryId);

        verify(categoryRepository).findByBusinessId(categoryId);
        verifyNoInteractions(categoryCommandMapper, categoryResponseMapper);
    }

    @Test
    void deleteCategory_shouldSoftDeleteCategory_whenValidId() {
        String categoryBusinessId = "0123456789ABC";

        Category category = new Category();
        category.setBusinessId(categoryBusinessId);
        category.setName("Sport");

        when(categoryRepository.findByBusinessId(categoryBusinessId)).thenReturn(Optional.of(category));

        service.deleteCategory(categoryBusinessId);

        ArgumentCaptor<Category> savedCategoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).findByBusinessId(categoryBusinessId);
        verify(categoryRepository).save(savedCategoryCaptor.capture());

        Category savedCategory = savedCategoryCaptor.getValue();
        assertThat(savedCategory.getDeletedAt()).isNotNull();
        assertThat(savedCategory.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void deleteCategory_shouldThrowNotFoundException_whenCategoryDoesNotExist() {
        String categoryBusinessId = "0123456789ABC";

        when(categoryRepository.findByBusinessId(categoryBusinessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCategory(categoryBusinessId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found " + categoryBusinessId);

        verify(categoryRepository).findByBusinessId(categoryBusinessId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void registerNewSubCategory_shouldMapAssignBusinessIdSetCategorySaveAndReturnResponse() {
        CreateSubCategoryRequest request = mock(CreateSubCategoryRequest.class);
        String categoryBusinessId = "cat-1";

        Category category = new Category();
        category.setBusinessId(categoryBusinessId);

        SubCategory subCategory = new SubCategory();
        SubCategoryResponse expected = mock(SubCategoryResponse.class);

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("subcat-tsid");

        when(subCategoryCommandMapper.toEntity(request)).thenReturn(subCategory);
        when(categoryRepository.findByBusinessId(categoryBusinessId)).thenReturn(Optional.of(category));
        when(subCategoryResponseMapper.toResponse(subCategory)).thenReturn(expected);

        SubCategoryResponse result = service.registerNewSubCategory(request, categoryBusinessId);

        assertThat(result).isSameAs(expected);
        assertThat(subCategory.getBusinessId()).isEqualTo("subcat-tsid");
        assertThat(subCategory.getCategory()).isSameAs(category);
        verify(subCategoryRepository).save(subCategory);
    }

    @Test
    void registerNewSubCategory_shouldThrowWhenCategoryDoesNotExist() {
        CreateSubCategoryRequest request = mock(CreateSubCategoryRequest.class);
        String categoryBusinessId = "missing-cat";

        SubCategory subCategory = new SubCategory();
        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("subcat-tsid");
        when(subCategoryCommandMapper.toEntity(request)).thenReturn(subCategory);
        when(categoryRepository.findByBusinessId(categoryBusinessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerNewSubCategory(request, categoryBusinessId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(categoryBusinessId);

        verify(subCategoryRepository, never()).save(any());
    }

    @Test
    void isValidCategorySubCategoryRelation_shouldReturnTrueWhenRelated() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        Category parentCategory = new Category();
        parentCategory.setBusinessId("cat-1");

        SubCategory subCategory = new SubCategory();
        subCategory.setCategory(parentCategory);

        boolean result = service.isValidCategorySubCategoryRelation(category, subCategory);

        assertThat(result).isTrue();
    }

    @Test
    void isValidCategorySubCategoryRelation_shouldReturnFalseWhenNotRelated() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        Category parentCategory = new Category();
        parentCategory.setBusinessId("cat-2");

        SubCategory subCategory = new SubCategory();
        subCategory.setCategory(parentCategory);

        boolean result = service.isValidCategorySubCategoryRelation(category, subCategory);

        assertThat(result).isFalse();
    }
}