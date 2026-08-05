package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.response.AppReferenceDataResponse;
import dev.floelly.activitytrackerapi.service.AppReferenceDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app-reference-data")
@RequiredArgsConstructor
public class AppReferenceDataController {


    private final AppReferenceDataService appReferenceDataService;

    @GetMapping
    public ResponseEntity<AppReferenceDataResponse> getAppReferenceData() {
        AppReferenceDataResponse response = appReferenceDataService.getAppReferenceData();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}