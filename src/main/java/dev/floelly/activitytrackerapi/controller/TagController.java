package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.dto.response.TagsResponse;
import dev.floelly.activitytrackerapi.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> postNewTag(@RequestBody @Valid CreateTagRequest tagRequest) {
        TagResponse response = tagService.registerNewTag(tagRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TagsResponse> getAllTags() {
        TagsResponse response = tagService.findAllTags();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
