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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityAttributeMapperTest {

    @Mock
    private ActivityCommandMapper commandMapper;

    @InjectMocks
    private ActivityAttributeMapper mapper;

    @Test
    void toEntity_shouldMapAllFieldsAndSetActivity() {
        CreateActivityAttributeRequest request = new CreateActivityAttributeRequest("key-1", "value-1", true, 3);
        Activity activity = new Activity();

        ActivityAttribute basicAttribute = new ActivityAttribute();
        basicAttribute.setLabel("key-1");
        basicAttribute.setValue("value-1");
        basicAttribute.setShowInOverview(true);
        basicAttribute.setSortOrder(3);

        when(commandMapper.toEntity(request)).thenReturn(basicAttribute);

        ActivityAttribute result = mapper.toEntity(request, activity);

        assertThat(result.getActivity()).isSameAs(activity);
        assertThat(result.getLabel()).isEqualTo("key-1");
        assertThat(result.getValue()).isEqualTo("value-1");
        assertThat(result.isShowInOverview()).isTrue();
        assertThat(result.getSortOrder()).isEqualTo(3);
    }
}