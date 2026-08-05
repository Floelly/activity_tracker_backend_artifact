package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities-dashboard")
@RequiredArgsConstructor
public class Controller {

    private final Service service;

    @GetMapping
    public ResponseEntity<Response> getActivitiesDashboard(@Valid @ModelAttribute ActivitiesDashboardFilterDTO filter) {
        Response response = service.getActivitiesDashboardResponse(filter);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}