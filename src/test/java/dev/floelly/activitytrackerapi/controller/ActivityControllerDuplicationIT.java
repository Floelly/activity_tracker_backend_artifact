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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ActivityControllerDuplicationIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldReturn201AndDuplicateResponse_onDuplicateActivity_whenEmptyBody() throws Exception {
        String categoryId1 = createCategoryAndGetId();
        String categoryId2 = createCategoryAndGetId();
        String subCategoryId1 = createSubCategoryForCategoryIdAndGetId(categoryId1);
        String tagId = createTagAndGetId(3);
        String sourceActivityId = createActivityAndGetId(String.format("""
                {
                    "title": "Template Activity",
                    "notes": "Template notes",
                    "startAt": "2024-01-01T10:00:00Z",
                    "endAt": "2024-01-01T11:00:00Z",
                    "categoryAllocations": [
                        {
                            "percentage": 60,
                            "categoryId": "%s",
                            "subCategoryId": "%s"
                        },
                        {
                            "percentage": 40,
                            "categoryId": "%s",
                            "subCategoryId": null
                        }
                    ],
                    "customValues": [
                        {
                            "key": "effort",
                            "value": "medium",
                            "showInOverview": true,
                            "sortOrder": 0
                        }
                    ],
                    "tagIds": ["%s"]
                }
                """, categoryId1, subCategoryId1, categoryId2, tagId));

        JsonNode duplicated = duplicateActivityAndGetResponse(sourceActivityId, "{}");

        assertThat(duplicated.get("id").asString()).isNotEqualTo(sourceActivityId);
        assertThat(duplicated.get("title").asString()).isEqualTo("Template Activity");
        assertThat(duplicated.get("notes").asString()).isEqualTo("Template notes");
        assertThat(duplicated.get("startAt").asString()).isEqualTo("2024-01-01T10:00:00Z");
        assertThat(duplicated.get("endAt").asString()).isEqualTo("2024-01-01T11:00:00Z");
        assertThat(duplicated.get("categoryAllocations")).hasSize(2);
        assertThat(duplicated.get("customValues")).hasSize(1);
        assertThat(duplicated.get("tags")).hasSize(1);
        assertThat(duplicated.get("createdAt").asString()).isNotBlank();
        assertThat(duplicated.get("updatedAt").isNull()).isTrue();
    }

    @Test
    void shouldPreserveDuration_onDuplicateActivity_whenOnlyStartAtProvided() throws Exception {
        String sourceActivityId = createActivityAndGetId("""
                {
                    "title": "Duration Source",
                    "notes": "Duration Notes",
                    "startAt": "2024-01-01T10:00:00Z",
                    "endAt": "2024-01-01T11:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """);

        JsonNode duplicated = duplicateActivityAndGetResponse(sourceActivityId, """
                {
                    "startAt": "2024-02-01T14:00:00Z"
                }
                """);

        assertThat(duplicated.get("startAt").asString()).isEqualTo("2024-02-01T14:00:00Z");
        assertThat(duplicated.get("endAt").asString()).isEqualTo("2024-02-01T15:00:00Z");
        assertThat(duplicated.get("durationInSeconds").asInt()).isEqualTo(3600);
    }

    @Test
    void shouldUseExactTimestamps_onDuplicateActivity_whenBothProvided() throws Exception {
        String sourceActivityId = createActivityAndGetId("""
                {
                    "title": "Explicit End Source",
                    "notes": "Explicit End Notes",
                    "startAt": "2024-01-01T10:00:00Z",
                    "endAt": "2024-01-01T11:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """);

        JsonNode duplicated = duplicateActivityAndGetResponse(sourceActivityId, """
                {
                    "startAt": "2024-02-01T14:00:00Z",
                    "endAt": "2024-02-01T16:00:00Z"
                }
                """);

        assertThat(duplicated.get("startAt").asString()).isEqualTo("2024-02-01T14:00:00Z");
        assertThat(duplicated.get("endAt").asString()).isEqualTo("2024-02-01T16:00:00Z");
        assertThat(duplicated.get("durationInSeconds").asInt()).isEqualTo(7200);
    }

    @Test
    void shouldNotModifyOriginalActivity_onDuplicateActivity() throws Exception {
        String sourceActivityId = createActivityAndGetId("""
                {
                    "title": "Original Activity",
                    "notes": "Original Notes",
                    "startAt": "2024-03-01T10:00:00Z",
                    "endAt": "2024-03-01T11:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """);

        JsonNode originalBefore = getActivityById(sourceActivityId);

        duplicateActivityAndGetResponse(sourceActivityId, """
                {
                    "startAt": "2024-03-02T10:00:00Z",
                    "endAt": "2024-03-02T12:00:00Z"
                }
                """);

        JsonNode originalAfter = getActivityById(sourceActivityId);

        assertThat(originalAfter.get("id").asString()).isEqualTo(originalBefore.get("id").asString());
        assertThat(originalAfter.get("title").asString()).isEqualTo(originalBefore.get("title").asString());
        assertThat(originalAfter.get("notes").asString()).isEqualTo(originalBefore.get("notes").asString());
        assertThat(originalAfter.get("startAt").asString()).isEqualTo("2024-03-01T10:00:00Z");
        assertThat(originalAfter.get("endAt").asString()).isEqualTo("2024-03-01T11:00:00Z");
        assertThat(originalAfter.get("durationInSeconds").asInt()).isEqualTo(originalBefore.get("durationInSeconds").asInt());
    }

    @Test
    void shouldCopyAllChildEntities_onDuplicateActivity() throws Exception {
        String categoryId = createCategoryAndGetId();
        String subCategoryId = createSubCategoryForCategoryIdAndGetId(categoryId);
        String tagId = createTagAndGetId(7);

        String sourceActivityId = createActivityAndGetId(String.format("""
                {
                    "title": "Child Copy Source",
                    "notes": "Some note",
                    "startAt": "2024-01-01T08:00:00Z",
                    "endAt": "2024-01-01T09:00:00Z",
                    "categoryAllocations": [
                        {
                            "percentage": 100,
                            "categoryId": "%s",
                            "subCategoryId": "%s"
                        }
                    ],
                    "customValues": [
                        {
                            "key": "location",
                            "value": "gym",
                            "showInOverview": true,
                            "sortOrder": 1
                        }
                    ],
                    "tagIds": ["%s"]
                }
                """, categoryId, subCategoryId, tagId));

        JsonNode duplicated = duplicateActivityAndGetResponse(sourceActivityId, "{}");
        String duplicatedId = duplicated.get("id").asString();

        mockMvc.perform(get("/api/activities/" + duplicatedId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(duplicatedId))
                .andExpect(jsonPath("$.categoryAllocations.length()").value(1))
                .andExpect(jsonPath("$.categoryAllocations[0].percentage").value(100))
                .andExpect(jsonPath("$.categoryAllocations[0].category.id").value(categoryId))
                .andExpect(jsonPath("$.categoryAllocations[0].subcategory.id").value(subCategoryId))
                .andExpect(jsonPath("$.customValues.length()").value(1))
                .andExpect(jsonPath("$.customValues[0].key").value("location"))
                .andExpect(jsonPath("$.customValues[0].value").value("gym"))
                .andExpect(jsonPath("$.tags.length()").value(1))
                .andExpect(jsonPath("$.tags[0].id").value(tagId));

        assertThat(duplicatedId).isNotEqualTo(sourceActivityId);
    }

    @Test
    void shouldReturn404_onDuplicateActivity_whenActivityNotFound() throws Exception {
        mockMvc.perform(post("/api/activities/0123456789ABC/duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400_onDuplicateActivity_whenActivityIdIsInvalid() throws Exception {
        mockMvc.perform(post("/api/activities/invalid-id/duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_onDuplicateActivity_whenEndAtIsBeforeStartAt() throws Exception {
        String sourceActivityId = createActivityAndGetId();

        mockMvc.perform(post("/api/activities/" + sourceActivityId + "/duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startAt": "2024-01-01T10:00:00Z",
                                    "endAt": "2024-01-01T09:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_onDuplicateActivity_whenEndAtProvidedWithoutStartAt() throws Exception {
        String sourceActivityId = createActivityAndGetId();

        mockMvc.perform(post("/api/activities/" + sourceActivityId + "/duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "endAt": "2024-01-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private JsonNode duplicateActivityAndGetResponse(String activityId, String payloadAsJson) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/activities/" + activityId + "/duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAsJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getActivityById(String activityId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/activities/" + activityId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String createActivityAndGetId() throws Exception {
        return createActivityAndGetId("""
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """);
    }

    private String createActivityAndGetId(String payloadAsJson) throws Exception {
        MvcResult createActivityResult = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(createActivityResult.getResponse().getContentAsString())
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
                                    "description": "generated category description",
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

    private String createTagAndGetId(int sortOrder) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "label": "tag-%d",
                                    "color": "#123456",
                                    "description": "generated tag",
                                    "sortOrder": %d
                                }
                                """, sortOrder, sortOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String createSubCategoryForCategoryIdAndGetId(String categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories/" + categoryId + "/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "generated subcategory",
                                    "description": "generated subcategory description"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }
}


