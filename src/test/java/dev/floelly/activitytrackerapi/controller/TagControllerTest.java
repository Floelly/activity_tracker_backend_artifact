package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.dto.response.TagsResponse;
import dev.floelly.activitytrackerapi.service.TagService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @Test
    void postNewTag_shouldReturnCreated() throws Exception {
        TagResponse response = new TagResponse(
                "tag-1",
                "Health",
                "#22c55e",
                "Health related tag",
                0
        );

        when(tagService.registerNewTag(any(CreateTagRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "label": "Health",
                                  "color": "#22c55e",
                                  "description": "Health related tag",
                                  "sortOrder": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("tag-1"))
                .andExpect(jsonPath("$.label").value("Health"))
                .andExpect(jsonPath("$.color").value("#22c55e"))
                .andExpect(jsonPath("$.description").value("Health related tag"))
                .andExpect(jsonPath("$.sortOrder").value(0));

        verify(tagService).registerNewTag(any(CreateTagRequest.class));
    }

    @Test
    void postNewTag_shouldPassValidatedRequestToService() throws Exception {
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "label": "Health",
                          "color": "#22c55e",
                          "description": "Health related tag",
                          "sortOrder": 0
                        }
                        """));

        verify(tagService).registerNewTag(any(CreateTagRequest.class));

        ArgumentCaptor<CreateTagRequest> captor =
                ArgumentCaptor.forClass(CreateTagRequest.class);

        verify(tagService).registerNewTag(captor.capture());

        CreateTagRequest dto = captor.getValue();
        assertThat(dto.label()).isEqualTo("Health");
        assertThat(dto.color()).isEqualTo("#22c55e");
        assertThat(dto.sortOrder()).isZero();
        assertThat(dto.description()).isEqualTo("Health related tag");
    }

    @Test
    void postNewTag_shouldReturnBadRequest_whenLabelIsBlank() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "label": "",
                                  "color": "#22c55e",
                                  "description": "Health related tag",
                                  "sortOrder": 0
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).registerNewTag(any(CreateTagRequest.class));
    }

    @Test
    void getAllTags_shouldReturnOk() throws Exception {
        TagsResponse response = new TagsResponse(List.of(
                new TagResponse("tag-1", "Health", "#22c55e", "Health related tag", 0),
                new TagResponse("tag-2", "Work", "#3b82f6", "Work related tag", 1)
        ));

        when(tagService.searchTags(nullable(String.class), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0].id").value("tag-1"))
                .andExpect(jsonPath("$.tags[0].label").value("Health"))
                .andExpect(jsonPath("$.tags[1].color").value("#3b82f6"))
                .andExpect(jsonPath("$.tags[1].description").value("Work related tag"))
                .andExpect(jsonPath("$.tags[1].sortOrder").value(1));

        verify(tagService).searchTags(null, 10);
    }

    @Test
    void getAllTags_shouldReturnOk_OnEmptyList() throws Exception {
        TagsResponse response = new TagsResponse(List.of());

        when(tagService.searchTags(nullable(String.class), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(0));

        verify(tagService).searchTags(null, 10);
    }

    @Test
    void getAllTags_shouldPassQueryAndLimitToService() throws Exception {
        TagsResponse response = new TagsResponse(List.of(
                new TagResponse("tag-1", "Health", "#22c55e", "Health related tag", 0)
        ));

        when(tagService.searchTags("proj", 5)).thenReturn(response);

        mockMvc.perform(get("/api/tags")
                        .param("query", "proj")
                        .param("limit", "5"))
                .andExpect(status().isOk());

        verify(tagService).searchTags("proj", 5);
    }

    @Test
    void getAllTags_shouldReturnBadRequest_whenQueryExceedsMaxLength() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("query", "x".repeat(51)))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).searchTags(any(), anyInt());
    }

    @Test
    void getAllTags_shouldReturnBadRequest_whenLimitBelowMinimum() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).searchTags(any(), anyInt());
    }

    @Test
    void getAllTags_shouldReturnBadRequest_whenLimitAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).searchTags(any(), anyInt());
    }
}