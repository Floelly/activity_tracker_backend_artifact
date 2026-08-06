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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ActivityControllerBatchDeleteIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldReturn200AndDeleteAll_onBatchDeleteActivities_whenAllIdsExist() throws Exception {
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
    void shouldReturn200AndPartialSuccess_onBatchDeleteActivities_whenSomeIdsDoNotExist() throws Exception {
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
                .andExpect(jsonPath("$.failed[0].id").value(nonExistentId))
                .andExpect(jsonPath("$.failed[0].reason").isString());

        verifyActivitiesDeleted(existingIds);
    }

    @Test
    void shouldReturn200AndNoDeletions_onBatchDeleteActivities_whenAllIdsDoNotExist() throws Exception {
        String nonExistentId1 = "0123456789ABC";
        String nonExistentId2 = "0123456789ABD";

        List<String> nonExistentIds = List.of(nonExistentId1, nonExistentId2);

        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BatchDeleteRequestPayload(nonExistentIds))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.totalDeleted").value(0))
                .andExpect(jsonPath("$.failed").isArray())
                .andExpect(jsonPath("$.failed.length()").value(2))
                .andExpect(jsonPath("$.failed[0].id").value(nonExistentId1))
                .andExpect(jsonPath("$.failed[1].id").value(nonExistentId2));
    }

    @Test
    void shouldReturn400_onBatchDeleteActivities_whenActivityIdsEmpty() throws Exception {
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
    void shouldReturn400_onBatchDeleteActivities_whenAnyIdHasInvalidFormat() throws Exception {
        String validId = createActivityAndGetId();
        String invalidId = "invalid-id";

        mockMvc.perform(delete("/api/activities/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BatchDeleteRequestPayload(List.of(validId, invalidId)))))
                .andExpect(status().isBadRequest());
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
