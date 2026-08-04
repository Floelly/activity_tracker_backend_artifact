package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateActivityAttributeRequest;
import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.ActivityAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityAttributeMapper {

    private final ActivityCommandMapper commandMapper;

    public ActivityAttribute toEntity(CreateActivityAttributeRequest request, Activity activity) {
        ActivityAttribute attribute = commandMapper.toEntity(request);
        attribute.setActivity(activity);
        return attribute;
    }
}