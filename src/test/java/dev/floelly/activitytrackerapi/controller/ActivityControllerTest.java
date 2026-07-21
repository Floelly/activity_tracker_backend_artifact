package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.ActivityFilterDTO;
import dev.floelly.activitytrackerapi.dto.request.CreateActivityRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateActivityRequest;
import dev.floelly.activitytrackerapi.dto.response.*;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @Test
    void getActivities_shouldReturnActivitiesResponse_whenFilterIsProvided() throws Exception {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T08:00:00Z"),
                Instant.parse("2026-05-25T10:00:00Z"),
                List.of("0000123456789", "0010123456789")
        );
        ActivitiesResponse response = new ActivitiesResponse(true, List.of());

        when(activityService.findAllActivities(filter)).thenReturn(response);

        mockMvc.perform(
                        get("/api/activities")
                                .param("fromStartAt", "2026-05-25T08:00:00Z")
                                .param("toStartAt", "2026-05-25T10:00:00Z")
                                .param("category", "0000123456789")
                                .param("category", "0010123456789")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities.length()").value(0));

        verify(activityService).findAllActivities(filter);
    }

    @Test
    void getActivityById_shouldReturnOk() throws Exception {
        ActivityResponse response = new ActivityResponse(
                "activity-1",
                "morning run",
                "some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                1200,
                List.of(),
                List.of(
                        new ActivityAttributeResponse("key-1", "value-1", false, 0),
                        new ActivityAttributeResponse("key-2", "value-2", false, 1)
                ),
                List.of(),
                Instant.now(),
                null
        );

        when(activityService.findActivityByBusinessId("CBA9876543210")).thenReturn(response);

        mockMvc.perform(get("/api/activities/CBA9876543210"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("activity-1"))
                .andExpect(jsonPath("$.title").value("morning run"))
                .andExpect(jsonPath("$.endAt").value("2024-01-01T11:00:00Z"))
                .andExpect(jsonPath("$.durationInSeconds").value(1200))
                .andExpect(jsonPath("$.customValues").isArray())
                .andExpect(jsonPath("$.customValues.length()").value(2))
                .andExpect(jsonPath("$.customValues[0].key").value("key-1"))
                .andExpect(jsonPath("$.customValues[1].value").value("value-2"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(0))
                .andExpect(jsonPath("$.updatedAt").value(nullValue()));

        verify(activityService).findActivityByBusinessId(any());
    }

    @Test
    void getActivityById_shouldReturnBadRequest_whenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/activities/invalid-id"))
                .andExpect(status().isBadRequest());

        verify(activityService, never()).findActivityByBusinessId(any());
    }

    @Test
    void getActivityById_shouldReturnNotFound_whenServiceThrowsNotFoundException() throws Exception {
        NotFoundException exception = new NotFoundException("Activity not found");

        when(activityService.findActivityByBusinessId("CBA9876543210")).thenThrow(exception);

        mockMvc.perform(get("/api/activities/CBA9876543210"))
                .andExpect(status().isNotFound());

        verify(activityService).findActivityByBusinessId("CBA9876543210");
    }

    @Test
    void postNewActivity_shouldPassValidatedRequestToService() throws Exception {
        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "swimming",
                            "notes": "some notes",
                            "startAt": "2024-01-01T10:00:00Z",
                            "endAt": "2024-01-01T11:00:00Z",
                            "categoryAllocations": [
                                {
                                    "percentage": 100,
                                    "categoryId": "1234567891230",
                                    "subCategoryId": "1234567891230"
                                }
                            ],
                            "customValues": [
                                {
                                    "key": "key-1",
                                    "value": "value-1",
                                    "showInOverview": false,
                                    "sortOrder": 0
                                }
                            ],
                            "tagIds": [
                                "1234567891230",
                                "0123456789123"
                            ]
                        }
                        """));

        ArgumentCaptor<CreateActivityRequest> captor =
                ArgumentCaptor.forClass(CreateActivityRequest.class);

        verify(activityService).registerNewActivity(captor.capture());

        CreateActivityRequest dto = captor.getValue();
        assertThat(dto.title()).isEqualTo("swimming");
        assertThat(dto.notes()).isEqualTo("some notes");
        assertThat(dto.startAt()).hasToString("2024-01-01T10:00:00Z");
        assertThat(dto.endAt()).hasToString("2024-01-01T11:00:00Z");
        assertThat(dto.categoryAllocations()).hasSize(1);
        assertThat(dto.categoryAllocations().getFirst().percentage()).isEqualTo(100);
        assertThat(dto.categoryAllocations().getFirst().categoryId()).isEqualTo("1234567891230");
        assertThat(dto.customValues()).hasSize(1);
        assertThat(dto.customValues().getFirst().key()).isEqualTo("key-1");
        assertThat(dto.tagIds()).hasSize(2);
        assertThat(dto.tagIds().getFirst()).isEqualTo("1234567891230");
        assertThat(dto.tagIds().get(1)).isEqualTo("0123456789123");
    }

    @Test
    void postNewActivity_shouldPassValidatedRequestToService_whenListsEmpty() throws Exception {
        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": null,
                            "notes": null,
                            "startAt": "2024-01-01T10:00:00Z",
                            "endAt": "2024-01-01T11:00:00Z",
                            "categoryAllocations": [],
                            "customValues": [],
                            "tagIds": []
                        }
                        """));

        ArgumentCaptor<CreateActivityRequest> captor =
                ArgumentCaptor.forClass(CreateActivityRequest.class);

        verify(activityService).registerNewActivity(captor.capture());

        CreateActivityRequest dto = captor.getValue();
        assertThat(dto.title()).isNull();
        assertThat(dto.notes()).isNull();
        assertThat(dto.startAt()).hasToString("2024-01-01T10:00:00Z");
        assertThat(dto.endAt()).hasToString("2024-01-01T11:00:00Z");
        assertThat(dto.categoryAllocations()).isEmpty();
        assertThat(dto.customValues()).isEmpty();
        assertThat(dto.tagIds()).isEmpty();
    }

    @Test
    void postNewActivity_shouldReturnCreatedActivity() throws Exception {
        ActivityResponse response = new ActivityResponse(
                "activity-1",
                "swimming",
                "some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                1200,
                List.of(
                        new ActivityCategoryAllocationResponse(
                                100,
                                new ActivityCategoryResponse("cat-1", "sport", "#123456", "sport-icon"),
                                new ActivitySubCategoryResponse("sub-cat-1", "swimming")
                        )
                ),
                List.of(new ActivityAttributeResponse("key-1", "value-1", false, 0)),
                List.of(
                        new ActivityTagResponse("tag-1", "Health", "#22c55e", "First tag", 0),
                        new ActivityTagResponse("tag-2", "What?", "#22c55e", "Second tag", 5)
                ),
                Instant.now(),
                Instant.parse("2024-01-01T12:00:00Z")
        );

        when(activityService.registerNewActivity(any(CreateActivityRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "swimming",
                                    "notes": null,
                                    "startAt": "2024-01-01T10:00:00Z",
                                    "endAt": "2024-01-01T11:00:00Z",
                                    "categoryAllocations": [],
                                    "customValues": [],
                                    "tagIds": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("activity-1"))
                .andExpect(jsonPath("$.title").value("swimming"))
                .andExpect(jsonPath("$.categoryAllocations").isArray())
                .andExpect(jsonPath("$.categoryAllocations.length()").value(1))
                .andExpect(jsonPath("$.categoryAllocations[0].percentage").value(100))
                .andExpect(jsonPath("$.categoryAllocations[0].category.id").value("cat-1"))
                .andExpect(jsonPath("$.categoryAllocations[0].subcategory.name").value("swimming"))
                .andExpect(jsonPath("$.customValues").isArray())
                .andExpect(jsonPath("$.customValues.length()").value(1))
                .andExpect(jsonPath("$.customValues[0].key").value("key-1"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0].id").value("tag-1"))
                .andExpect(jsonPath("$.tags[1].label").value("What?"));

        verify(activityService).registerNewActivity(any(CreateActivityRequest.class));
    }

    @Test
    void postNewActivity_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "running",
                                    "notes": "some notes",
                                    "startAt": null,
                                    "endAt": "2024-01-01T11:00:00Z",
                                    "categoryAllocations": [],
                                    "customValues": [],
                                    "tagIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(activityService, never()).registerNewActivity(any(CreateActivityRequest.class));
    }

    @Test
    void deleteActivity_shouldReturnNoContent_whenActivityWasDeleted() throws Exception {
        mockMvc.perform(delete("/api/activities/0123456789ABC"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(activityService).deleteActivity("0123456789ABC");
    }

    @Test
    void deleteActivity_shouldReturnBadRequest_whenActivityIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/activities/0123456789AB"))
                .andExpect(status().isBadRequest());

        verify(activityService, never()).deleteActivity(any());
    }

    @Test
    void deleteActivity_shouldReturnNotFound_whenServiceThrowsNotFoundException() throws Exception {
        doThrow(new NotFoundException("Activity not found 0123456789ABC"))
                .when(activityService).deleteActivity("0123456789ABC");

        mockMvc.perform(delete("/api/activities/0123456789ABC"))
                .andExpect(status().isNotFound());

        verify(activityService).deleteActivity("0123456789ABC");
    }

    @Test
    void updateActivity_shouldPassValidatedRequestToService() throws Exception {
        String activityId = "0456456456456";
        mockMvc.perform(put("/api/activities/" + activityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id": "%s",
                            "title": "swimming",
                            "notes": "some notes",
                            "startAt": "2024-01-01T10:00:00Z",
                            "endAt": "2024-01-01T11:00:00Z",
                            "categoryAllocations": [
                                {
                                    "percentage": 100,
                                    "categoryId": "1234567891230",
                                    "subCategoryId": "1234567891231"
                                }
                            ]
                        }
                        """, activityId)));

        ArgumentCaptor<UpdateActivityRequest> captor =
                ArgumentCaptor.forClass(UpdateActivityRequest.class);

        verify(activityService).updateActivity(eq(activityId), captor.capture());

        UpdateActivityRequest dto = captor.getValue();
        assertThat(dto.id()).isEqualTo(activityId);
        assertThat(dto.title()).isEqualTo("swimming");
        assertThat(dto.notes()).isEqualTo("some notes");
        assertThat(dto.startAt()).hasToString("2024-01-01T10:00:00Z");
        assertThat(dto.endAt()).hasToString("2024-01-01T11:00:00Z");
        assertThat(dto.categoryAllocations()).hasSize(1);
        assertThat(dto.categoryAllocations().getFirst().percentage()).isEqualTo(100);
        assertThat(dto.categoryAllocations().getFirst().categoryId()).isEqualTo("1234567891230");
        assertThat(dto.categoryAllocations().getFirst().subCategoryId()).isEqualTo("1234567891231");
    }

    @Test
    void updateActivity_shouldPassValidatedRequestToService_whenListsEmpty() throws Exception {
        String activityId = "0456456456456";
        mockMvc.perform(put("/api/activities/" + activityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id": "%s",
                            "title": null,
                            "notes": null,
                            "startAt": "2024-01-01T10:00:00Z",
                            "endAt": "2024-01-01T11:00:00Z",
                            "categoryAllocations": []
                        }
                        """, activityId)));

        ArgumentCaptor<UpdateActivityRequest> captor =
                ArgumentCaptor.forClass(UpdateActivityRequest.class);

        verify(activityService).updateActivity(eq(activityId), captor.capture());

        UpdateActivityRequest dto = captor.getValue();
        assertThat(dto.id()).isEqualTo(activityId);
        assertThat(dto.title()).isNull();
        assertThat(dto.notes()).isNull();
        assertThat(dto.startAt()).hasToString("2024-01-01T10:00:00Z");
        assertThat(dto.endAt()).hasToString("2024-01-01T11:00:00Z");
        assertThat(dto.categoryAllocations()).isEmpty();
    }

    @Test
    void updateActivity_shouldReturnUpdatedActivity() throws Exception {
        String activityId = "0789789789798";
        ActivityResponse response = new ActivityResponse(
                activityId,
                "swimming",
                "some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                1200,
                List.of(
                        new ActivityCategoryAllocationResponse(
                                100,
                                new ActivityCategoryResponse("cat-1", "sport", "#123456", "sport-icon"),
                                new ActivitySubCategoryResponse("sub-cat-1", "swimming")
                        )
                ),
                List.of(new ActivityAttributeResponse("key-1", "value-1", false, 0)),
                List.of(
                        new ActivityTagResponse("tag-1", "Health", "#22c55e", "First tag", 0),
                        new ActivityTagResponse("tag-2", "What?", "#22c55e", "Second tag", 5)
                ),
                Instant.now(),
                Instant.parse("2024-01-01T12:00:00Z")
        );

        when(activityService.updateActivity(eq(activityId), any(UpdateActivityRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/activities/" + activityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "id": "%s",
                                    "title": "swimming",
                                    "notes": null,
                                    "startAt": "2024-01-01T10:00:00Z",
                                    "endAt": "2024-01-01T11:00:00Z",
                                    "categoryAllocations": []
                                }
                                """, activityId)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.title").value("swimming"))
                .andExpect(jsonPath("$.categoryAllocations").isArray())
                .andExpect(jsonPath("$.categoryAllocations.length()").value(1))
                .andExpect(jsonPath("$.categoryAllocations[0].percentage").value(100))
                .andExpect(jsonPath("$.categoryAllocations[0].category.id").value("cat-1"))
                .andExpect(jsonPath("$.categoryAllocations[0].subcategory.name").value("swimming"))
                .andExpect(jsonPath("$.customValues").isArray())
                .andExpect(jsonPath("$.customValues.length()").value(1))
                .andExpect(jsonPath("$.customValues[0].key").value("key-1"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0].id").value("tag-1"))
                .andExpect(jsonPath("$.tags[1].label").value("What?"));

        verify(activityService).updateActivity(anyString(), any(UpdateActivityRequest.class));
    }

    @Test
    void updateActivity_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        mockMvc.perform(put("/api/activities/0123456789123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "0123456789123"
                                    "title": "running",
                                    "notes": "some notes",
                                    "startAt": "2024-01-01T11:00:00Z",
                                    "endAt": "2024-01-01T11:00:00Z",
                                    "categoryAllocations": [
                                        {
                                            "percentage": 100,
                                            "categoryId": "0456456789789",
                                            "subCategoryId": null
                                        },
                                        {
                                            "percentage": 100,
                                            "categoryId": "0456456789789",
                                            "subCategoryId": null
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(activityService, never()).updateActivity(anyString(), any(UpdateActivityRequest.class));
    }
}