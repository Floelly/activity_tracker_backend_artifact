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
    void toEntity_shouldMapAndSetActivity() {
        CreateActivityAttributeRequest request = new CreateActivityAttributeRequest("key", "value", true, 0);
        Activity activity = new Activity();
        ActivityAttribute mappedAttribute = new ActivityAttribute();
        mappedAttribute.setLabel("key");
        mappedAttribute.setValue("value");
        mappedAttribute.setShowInOverview(true);
        mappedAttribute.setSortOrder(0);

        when(commandMapper.toEntity(request)).thenReturn(mappedAttribute);

        ActivityAttribute result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAttribute);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getLabel()).isEqualTo("key");
        assertThat(result.getValue()).isEqualTo("value");
        assertThat(result.isShowInOverview()).isTrue();
        assertThat(result.getSortOrder()).isZero();

        verify(commandMapper).toEntity(request);
    }

    @Test
    void toEntity_shouldMapAndSetActivity_whenOptionalFieldsAreNull() {
        CreateActivityAttributeRequest request = new CreateActivityAttributeRequest("key", null, false, 5);
        Activity activity = new Activity();
        ActivityAttribute mappedAttribute = new ActivityAttribute();
        mappedAttribute.setLabel("key");
        mappedAttribute.setValue(null);
        mappedAttribute.setShowInOverview(false);
        mappedAttribute.setSortOrder(5);

        when(commandMapper.toEntity(request)).thenReturn(mappedAttribute);

        ActivityAttribute result = mapper.toEntity(request, activity);

        assertThat(result).isSameAs(mappedAttribute);
        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getLabel()).isEqualTo("key");
        assertThat(result.getValue()).isNull();
        assertThat(result.isShowInOverview()).isFalse();
        assertThat(result.getSortOrder()).isEqualTo(5);

        verify(commandMapper).toEntity(request);
    }
}