package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.response.AppReferenceDataResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.mapper.CategoryResponseMapper;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppReferenceDataServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryResponseMapper categoryResponseMapper;

    @InjectMocks
    private AppReferenceDataService service;

    @Test
    void getAppReferenceData_shouldReturnMappedResponse() {
        Category category1 = new Category();
        Category category2 = new Category();
        List<Category> categories = List.of(category1, category2);

        CategoryResponse categoryResponse1 = new CategoryResponse(
                "CAT123456789",
                "Category 1",
                "#123456",
                "icon-1",
                "desc 1",
                List.of()
        );
        CategoryResponse categoryResponse2 = new CategoryResponse(
                "CAT223456789",
                "Category 2",
                "#654321",
                "icon-2",
                "desc 2",
                List.of()
        );
        List<CategoryResponse> mappedResponses = List.of(categoryResponse1, categoryResponse2);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryResponseMapper.toCategoryResponseList(categories)).thenReturn(mappedResponses);

        AppReferenceDataResponse result = service.getAppReferenceData();

        assertThat(result.categories()).isSameAs(mappedResponses);

        verify(categoryRepository).findAll();
        verify(categoryResponseMapper).toCategoryResponseList(categories);
        verifyNoMoreInteractions(categoryRepository, categoryResponseMapper);
    }
}