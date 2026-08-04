package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateActivityAttributeRequest;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.ActivityAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityAttributeMapperTest {

    @Mock
    private ActivityCommandMapper commandMapper;

    @InjectMocks
    private ActivityAttributeMapper mapper;

    @Test
    void toEntity_shouldMapRequestAndSetActivity() {
        CreateActivityAttributeRequest request = new CreateActivityAttributeRequest("key-1", "value-1", true, 0);
        Activity activity = new Activity();

        ActivityAttribute mappedAttribute = new ActivityAttribute();
        mappedAttribute.setLabel("key-1");
        mappedAttribute.setValue("value-1");

        when(commandMapper.toEntity(request)).thenReturn(mappedAttribute);

        ActivityAttribute result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAttribute);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getLabel()).isEqualTo("key-1");
        assertThat(result.getValue()).isEqualTo("value-1");

        verify(commandMapper).toEntity(request);
    }

    @Test
    void toEntity_shouldMapRequestWithAllFieldsAndSetActivity() {
        CreateActivityAttributeRequest request = new CreateActivityAttributeRequest("label", "some value", false, 5);
        Activity activity = new Activity();

        ActivityAttribute mappedAttribute = new ActivityAttribute();
        mappedAttribute.setLabel("label");
        mappedAttribute.setValue("some value");
        mappedAttribute.setShowInOverview(false);
        mappedAttribute.setSortOrder(5);

        when(commandMapper.toEntity(request)).thenReturn(mappedAttribute);

        ActivityAttribute result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAttribute);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getLabel()).isEqualTo("label");
        assertThat(result.getValue()).isEqualTo("some value");
        assertThat(result.isShowInOverview()).isFalse();
        assertThat(result.getSortOrder()).isEqualTo(5);

        verify(commandMapper).toEntity(request);
    }

    @Test
    void toEntity_shouldReturnNull_whenCommandMapperReturnsNull() {
        CreateActivityAttributeRequest request = new CreateActivityAttributeRequest("key", "val", true, 0);
        Activity activity = new Activity();

        when(commandMapper.toEntity(request)).thenReturn(null);

        ActivityAttribute result = mapper.toEntity(request, activity);

        assertThat(result).isNull();

        verify(commandMapper).toEntity(request);
    }
}