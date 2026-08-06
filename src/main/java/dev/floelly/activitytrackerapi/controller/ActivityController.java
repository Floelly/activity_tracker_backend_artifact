package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.ActivityFilterDTO;
import dev.floelly.activitytrackerapi.dto.request.CreateActivityRequest;
import dev.floelly.activitytrackerapi.dto.request.DuplicateActivityRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateActivityRequest;
import dev.floelly.activitytrackerapi.dto.response.ActivitiesResponse;
import dev.floelly.activitytrackerapi.dto.response.ActivityResponse;
import dev.floelly.activitytrackerapi.service.ActivityService;
import dev.floelly.activitytrackerapi.validation.ValidTSID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<ActivitiesResponse> getActivities(@Valid @ModelAttribute ActivityFilterDTO filter) {
        ActivitiesResponse response = activityService.findAllActivities(filter);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivityById(@PathVariable @ValidTSID String id) {
        ActivityResponse response = activityService.findActivityByBusinessId(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> postNewActivity(@RequestBody @Valid CreateActivityRequest activityRequest) {
        ActivityResponse response = activityService.registerNewActivity(activityRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable @ValidTSID String activityId) {
        activityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{businessId}/duplicate")
    public ResponseEntity<ActivityResponse> duplicateActivity(
            @PathVariable @ValidTSID String businessId,
            @RequestBody @Valid DuplicateActivityRequest request) {
        ActivityResponse response = activityService.duplicateActivity(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> updateActivity(
            @PathVariable @ValidTSID String activityId,
            @RequestBody @Valid UpdateActivityRequest activityRequest) {
        ActivityResponse response = activityService.updateActivity(activityId, activityRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
