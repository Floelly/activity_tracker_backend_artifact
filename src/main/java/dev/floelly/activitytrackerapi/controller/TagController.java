package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.dto.response.TagsResponse;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<TagsResponse> getAllTags(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer limit
    ) {
        if (limit != null && (limit < 1 || limit > 100)) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }

        int effectiveLimit = limit != null ? limit : 10;

        if (query != null) {
            String trimmedQuery = query.trim();
            if (trimmedQuery.isEmpty()) {
                TagsResponse response = tagService.findAllTags(effectiveLimit);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            if (trimmedQuery.length() > 50) {
                throw new BadRequestException("Query must not exceed 50 characters");
            }
            TagsResponse response = tagService.searchTags(trimmedQuery, effectiveLimit);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        if (limit != null) {
            TagsResponse response = tagService.findAllTags(effectiveLimit);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        TagsResponse response = tagService.findAllTags();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
