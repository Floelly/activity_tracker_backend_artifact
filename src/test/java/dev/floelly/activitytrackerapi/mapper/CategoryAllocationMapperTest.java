package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryAllocationRequest;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryAllocationMapperTest {

    @Mock
    private ActivityCommandMapper commandMapper;

    @InjectMocks
    private CategoryAllocationMapper mapper;

    @Test
    void toEntity_shouldMapAllFieldsAndSetRelationships() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(75, "cat-1", "sub-1");
        Activity activity = new Activity();
        Category category = new Category();
        SubCategory subCategory = new SubCategory();

        CategoryAllocation basicAllocation = new CategoryAllocation();
        basicAllocation.setPercentage(75);

        when(commandMapper.toEntity(request)).thenReturn(basicAllocation);

        CategoryAllocation result = mapper.toEntity(request, activity, category, subCategory);

        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getSubCategory()).isSameAs(subCategory);
        assertThat(result.getPercentage()).isEqualTo(75);
    }

    @Test
    void toEntity_shouldSetNullSubCategory_whenSubCategoryIsNull() {
        CreateCategoryAllocationRequest request = new CreateCategoryAllocationRequest(50, "cat-1", null);
        Activity activity = new Activity();
        Category category = new Category();

        CategoryAllocation basicAllocation = new CategoryAllocation();
        basicAllocation.setPercentage(50);

        when(commandMapper.toEntity(request)).thenReturn(basicAllocation);

        CategoryAllocation result = mapper.toEntity(request, activity, category, null);

        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getSubCategory()).isNull();
        assertThat(result.getPercentage()).isEqualTo(50);
    }
}