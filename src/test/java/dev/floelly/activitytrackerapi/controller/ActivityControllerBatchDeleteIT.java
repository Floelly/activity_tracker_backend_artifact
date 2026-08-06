package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ActivityControllerBatchDeleteIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void batchDeleteActivities_shouldDeleteAllActivities_whenValidIds() throws Exception {
        List<String> activityIds = createMultipleActivitiesAndGetIds(3);

        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BatchDeleteRequestPayload(activityIds))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(3))
                .andExpect(jsonPath("$.totalDeleted").value(3))
                .andExpect(jsonPath("$.failed").isArray())
                .andExpect(jsonPath("$.failed.length()").value(0));

        verifyActivitiesDeleted(activityIds);
    }

    @Test
    void batchDeleteActivities_shouldReturn200WithSummary_whenPartialSuccess() throws Exception {
        List<String> existingIds = createMultipleActivitiesAndGetIds(2);
        String nonExistentId = "0123456789ABC";

        List<String> mixedIds = new ArrayList<>(existingIds);
        mixedIds.add(nonExistentId);

        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BatchDeleteRequestPayload(mixedIds))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(3))
                .andExpect(jsonPath("$.totalDeleted").value(2))
                .andExpect(jsonPath("$.failed").isArray())
                .andExpect(jsonPath("$.failed.length()").value(1))
                .andExpect(jsonPath("$.failed[0].activityId").value(nonExistentId))
                .andExpect(jsonPath("$.failed[0].reason").exists());

        verifyActivitiesDeleted(existingIds);
    }

    @Test
    void batchDeleteActivities_shouldReturnBadRequest_whenEmptyIdList() throws Exception {
        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "activityIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void batchDeleteActivities_shouldReturn404_whenNoActivitiesFound() throws Exception {
        List<String> nonExistentIds = List.of("0123456789ABC", "0123456789ABD", "0123456789ABE");

        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BatchDeleteRequestPayload(nonExistentIds))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(3))
                .andExpect(jsonPath("$.totalDeleted").value(0))
                .andExpect(jsonPath("$.failed.length()").value(3));
    }

    @Test
    void batchDeleteActivities_shouldDeleteChildEntities_withNoCascadeOrphans() throws Exception {
        String categoryId = createCategoryAndGetId();
        String tagId = createTagAndGetId();

        String activityId = createActivityWithCategoryAndTagAndGetId(categoryId, tagId);

        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BatchDeleteRequestPayload(List.of(activityId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.totalDeleted").value(1))
                .andExpect(jsonPath("$.failed.length()").value(0));

        // Verify activity is deleted
        mockMvc.perform(get("/api/activities/" + activityId))
                .andExpect(status().isNotFound());

        // Verify category and tag still exist (they are reference data, not orphaned)
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[*].id", hasItem(categoryId)));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[*].id", hasItem(tagId)));
    }

    @Test
    void shouldNotAffectOtherActivities_onBatchDeleteActivities() throws Exception {
        List<String> toDelete = createMultipleActivitiesAndGetIds(2);
        String keptActivityId = createActivityAndGetId();

        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BatchDeleteRequestPayload(toDelete))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeleted").value(2));

        verifyActivitiesDeleted(toDelete);

        // Verify the non-deleted activity still exists
        mockMvc.perform(get("/api/activities/" + keptActivityId))
                .andExpect(status().isOk());
    }

    private List<String> createMultipleActivitiesAndGetIds(int count) throws Exception {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(createActivityAndGetId());
        }
        return ids;
    }

    private String createActivityAndGetId() throws Exception {
        String payloadAsJson = """
                {
                    "title": "Activity for Batch Delete Test",
                    "notes": "Test notes",
                    "startAt": "2024-01-01T00:00:00Z",
                    "endAt": "2024-01-01T01:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAsJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String createActivityWithCategoryAndTagAndGetId(String categoryId, String tagId) throws Exception {
        String payloadAsJson = String.format("""
                {
                    "title": "Activity with Children",
                    "notes": "Test notes",
                    "startAt": "2024-01-01T00:00:00Z",
                    "endAt": "2024-01-01T01:00:00Z",
                    "categoryAllocations": [
                        {
                            "categoryId": "%s",
                            "percentage": 100
                        }
                    ],
                    "customValues": [
                        {
                            "key": "test-key",
                            "value": "test-value",
                            "showInOverview": true,
                            "sortOrder": 1
                        }
                    ],
                    "tagIds": ["%s"]
                }
                """, categoryId, tagId);

        MvcResult result = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAsJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String createCategoryAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Generated Category",
                                    "color": "#123456",
                                    "description": "some other generated category description",
                                    "icon": "icon-name"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String createTagAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "label": "batch-test-tag",
                                    "color": "#123456",
                                    "description": "tag for batch delete test",
                                    "sortOrder": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private void verifyActivitiesDeleted(List<String> activityIds) throws Exception {
        for (String activityId : activityIds) {
            mockMvc.perform(get("/api/activities/" + activityId))
                    .andExpect(status().isNotFound());
        }
    }

    static class BatchDeleteRequestPayload {
        public List<String> activityIds;

        BatchDeleteRequestPayload(List<String> activityIds) {
            this.activityIds = activityIds;
        }
    }
}