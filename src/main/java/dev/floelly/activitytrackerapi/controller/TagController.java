package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.dto.response.TagsResponse;
import dev.floelly.activitytrackerapi.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> postNewTag(@RequestBody @Valid CreateTagRequest tagRequest) {
        TagResponse response = tagService.registerNewTag(tagRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TagsResponse> getAllTags(
            @RequestParam(required = false) @Size(max = 50) String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        TagsResponse response = tagService.searchTags(query, limit);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
