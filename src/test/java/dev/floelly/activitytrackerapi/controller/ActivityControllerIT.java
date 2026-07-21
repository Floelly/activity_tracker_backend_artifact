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
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ActivityControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MySQLContainer mysql;

    @Test
    void shouldReturn201_onPostActivity_whenValidRequest() throws Exception {
        createActivityAndGetId();
    }

    @Test
    void shouldReturnActivityResponse_onPostActivity_whenValidRequestWithLists() throws Exception {
        String categoryId1 = createCategoryAndGetId();
        String categoryId2 = createCategoryAndGetId();
        String tagId1 = createTagAndGetId(3);
        String tagId2 = createTagAndGetId(2);
        String tagId3 = createTagAndGetId(1);
        String subCategoryId1 = createSubCategoryForCategoryIdAndGetId(categoryId1);


        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "title": "Some Activity",
                                    "notes": "Some notes",
                                    "startAt": "2023-01-01T00:00:00Z",
                                    "endAt": "2023-01-01T01:00:00Z",
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
                                            "key": "key1",
                                            "value": "value1",
                                            "showInOverview": true,
                                            "sortOrder": 2
                                        },
                                        {
                                            "key": "key2",
                                            "value": "value2",
                                            "showInOverview": false,
                                            "sortOrder": 1
                                        }
                                    ],
                                    "tagIds": ["%s", "%s", "%s"]
                                }
                                """, categoryId1, subCategoryId1, categoryId2, tagId1, tagId2, tagId3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Some Activity"))
                .andExpect(jsonPath("$.notes").value("Some notes"))
                .andExpect(jsonPath("$.startAt").value("2023-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.endAt").value("2023-01-01T01:00:00Z"))
                .andExpect(jsonPath("$.categoryAllocations").isArray())
                .andExpect(jsonPath("$.categoryAllocations.length()").value(2))
                .andExpect(jsonPath("$.categoryAllocations[0].percentage").value(60))
                .andExpect(jsonPath("$.categoryAllocations[0].category.id").value(categoryId1))
                .andExpect(jsonPath("$.categoryAllocations[0].subcategory.id").value(subCategoryId1))
                .andExpect(jsonPath("$.categoryAllocations[1].percentage").value(40))
                .andExpect(jsonPath("$.categoryAllocations[1].category.id").value(categoryId2))
                .andExpect(jsonPath("$.categoryAllocations[1].subcategory.id").doesNotExist())
                .andExpect(jsonPath("$.customValues").isArray())
                .andExpect(jsonPath("$.customValues.length()").value(2))
                .andExpect(jsonPath("$.customValues[0].key").value("key2"))
                .andExpect(jsonPath("$.customValues[1].value").value("value1"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(3))
                .andExpect(jsonPath("$.tags[0].id").value(tagId3))
                .andExpect(jsonPath("$.tags[1].id").value(tagId2))
                .andExpect(jsonPath("$.tags[2].id").value(tagId1))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void shouldReturn400_onPostActivity_whenNoStartTimeProvided() throws Exception {
        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Some Activity",
                                    "notes": "Some notes",
                                    "startAt": null,
                                    "endAt": "2023-01-01T01:00:00Z",
                                    "categoryAllocations": [],
                                    "customValues": [],
                                    "tagIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnActivityResponseDTO_onGetActivityById() throws Exception {
        String activityId = createActivityAndGetId(
                """
                        {
                            "title": "Some Activity",
                            "notes": "Some notes",
                            "startAt": "2023-01-01T00:00:00Z",
                            "endAt": "2023-01-01T01:00:00Z",
                            "categoryAllocations": [],
                            "customValues": [],
                            "tagIds": []
                        }
                        """
        );

        mockMvc.perform(get("/api/activities/" + activityId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.title").value("Some Activity"))
                .andExpect(jsonPath("$.notes").value("Some notes"))
                .andExpect(jsonPath("$.startAt").value("2023-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.endAt").value("2023-01-01T01:00:00Z"))
                .andExpect(jsonPath("$.durationInSeconds").value(3600))
                .andExpect(jsonPath("$.categoryAllocations").isArray())
                .andExpect(jsonPath("$.categoryAllocations").isEmpty())
                .andExpect(jsonPath("$.customValues").isArray())
                .andExpect(jsonPath("$.customValues").isEmpty())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void shouldReturn400_onGetActivityById_whenInvalidActivityId() throws Exception {
        mockMvc.perform(get("/api/activities/" + "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404_onGetActivityById_whenActivityIdNotFound() throws Exception {
        mockMvc.perform(get("/api/activities/" + "9999999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnActivitiesResponseContract_onGetAllActivities() throws Exception {
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasMore").exists())
                .andExpect(jsonPath("$.activities").isArray());
    }

    @Test
    void shouldFilterActivities_byFromStartAt_onGetAllActivities() throws Exception {
        createActivityWithTimesAndReturnId(
                "Activity Before Beginning",
                "2023-01-01T08:00:00Z",
                "2023-01-01T09:00:00Z"
        );
        String activityId1 = createActivityWithTimesAndReturnId(
                "Activity After Beginning 1",
                "2023-01-02T10:00:00Z",
                "2023-01-02T11:00:00Z"
        );
        String activityId2 = createActivityWithTimesAndReturnId(
                "Activity After Beginning 2",
                "2023-01-03T12:00:00Z",
                "2023-01-03T13:00:00Z"
        );

        mockMvc.perform(get("/api/activities")
                        .param("fromStartAt", "2023-01-02T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId1)))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId2)))
                .andExpect(jsonPath("$.activities[*].title", not(hasItem("Activity Before Beginning"))));
    }

    @Test
    void shouldFilterActivities_byToStartAt_onGetAllActivities() throws Exception {
        String activityId1 = createActivityWithTimesAndReturnId(
                "Activity Before End 1",
                "2023-01-01T08:00:00Z",
                "2023-01-01T09:00:00Z"
        );
        String activityId2 = createActivityWithTimesAndReturnId(
                "Activity Before End 2",
                "2023-01-01T10:00:00Z",
                "2023-01-01T11:00:00Z"
        );
        createActivityWithTimesAndReturnId(
                "Activity After End",
                "2023-01-03T12:00:00Z",
                "2023-01-03T13:00:00Z"
        );

        mockMvc.perform(get("/api/activities")
                        .param("toStartAt", "2023-01-02T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId1)))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId2)))
                .andExpect(jsonPath("$.activities[*].title", not(hasItem("Activity After End"))));
    }

    @Test
    void shouldFilterActivities_byBothTimeRange_onGetAllActivities() throws Exception {
        createActivityWithTimesAndReturnId(
                "Activity Before Range",
                "2023-01-01T08:00:00Z",
                "2023-01-01T09:00:00Z"
        );
        String activityId1 = createActivityWithTimesAndReturnId(
                "Activity In Range 1",
                "2023-01-02T10:00:00Z",
                "2023-01-02T11:00:00Z"
        );
        String activityId2 = createActivityWithTimesAndReturnId(
                "Activity In Range 2",
                "2023-01-02T14:00:00Z",
                "2023-01-02T15:00:00Z"
        );
        createActivityWithTimesAndReturnId(
                "Activity After Range",
                "2023-01-03T12:00:00Z",
                "2023-01-03T13:00:00Z"
        );

        mockMvc.perform(get("/api/activities")
                        .param("fromStartAt", "2023-01-02T00:00:00Z")
                        .param("toStartAt", "2023-01-02T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId1)))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId2)))
                .andExpect(jsonPath("$.activities[*].title", not(hasItem("Activity Before Range"))))
                .andExpect(jsonPath("$.activities[*].title", not(hasItem("Activity After Range"))));
    }

    @Test
    void shouldFilterActivities_bySingleCategory_onGetAllActivities() throws Exception {
        String sportsCategoryId = createCategoryAndGetId();
        String workCategoryId = createCategoryAndGetId();

        String sportsActivityId = createActivityWithCategoryAndGetId(sportsCategoryId);

        String workActivityId = createActivityWithCategoryAndGetId(workCategoryId);

        String noCategoryActivityId = createActivityAndGetId();

        mockMvc.perform(get("/api/activities")
                        .param("category", sportsCategoryId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activities[*].id", hasItem(sportsActivityId)))
                .andExpect(jsonPath("$.activities[*].id", not(hasItem(workActivityId))))
                .andExpect(jsonPath("$.activities[*].id", not(hasItem(noCategoryActivityId))));
    }

    @Test
    void shouldFilterActivities_byMultipleCategories_onGetAllActivities() throws Exception {
        String sportsCategoryId = createCategoryAndGetId();
        String workCategoryId = createCategoryAndGetId();
        String studyCategoryId = createCategoryAndGetId();

        String sportsActivityId = createActivityWithCategoryAndGetId(sportsCategoryId);

        String workActivityId = createActivityWithCategoryAndGetId(workCategoryId);

        String studyActivityId = createActivityWithCategoryAndGetId(studyCategoryId);

        mockMvc.perform(get("/api/activities")
                        .param("category", sportsCategoryId, workCategoryId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activities[*].id", hasItem(sportsActivityId)))
                .andExpect(jsonPath("$.activities[*].id", hasItem(workActivityId)))
                .andExpect(jsonPath("$.activities[*].id", not(hasItem(studyActivityId))));
    }

    @Test
    void shouldFilterActivities_byTimeRangeAndCategory_onGetAllActivities() throws Exception {
        String matchingCategoryId = createCategoryAndGetId();
        String wrongCategoryId = createCategoryAndGetId();

        String matchingActivityId = createActivityWithTimesAndCategoryAndGetId(
                "2023-01-02T10:00:00Z",
                "2023-01-02T11:00:00Z",
                matchingCategoryId
        );
        String wrongCategoryActivityId = createActivityWithTimesAndCategoryAndGetId(
                "2023-01-02T12:00:00Z",
                "2023-01-02T13:00:00Z",
                wrongCategoryId
        );
        String wrongTimeActivityId = createActivityWithTimesAndCategoryAndGetId(
                "2023-01-03T10:00:00Z",
                "2023-01-03T11:00:00Z",
                matchingCategoryId
        );

        mockMvc.perform(get("/api/activities")
                        .param("fromStartAt", "2023-01-02T00:00:00Z")
                        .param("toStartAt", "2023-01-02T23:59:59Z")
                        .param("category", matchingCategoryId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activities[*].id", hasItem(matchingActivityId)))
                .andExpect(jsonPath("$.activities[*].id", not(hasItem(wrongCategoryActivityId))))
                .andExpect(jsonPath("$.activities[*].id", not(hasItem(wrongTimeActivityId))));
    }

    @Test
    void shouldReturnAllActivities_whenNoFilterProvided_onGetAllActivities() throws Exception {
        String activityId1 = createActivityWithTimesAndReturnId(
                "Activity 1",
                "2023-01-01T08:00:00Z",
                "2023-01-01T09:00:00Z"
        );
        String activityId2 = createActivityWithTimesAndReturnId(
                "Activity 2",
                "2023-01-02T10:00:00Z",
                "2023-01-02T11:00:00Z"
        );
        String activityId3 = createActivityWithTimesAndReturnId(
                "Activity 3",
                "2023-01-03T12:00:00Z",
                "2023-01-03T13:00:00Z"
        );

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId1)))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId2)))
                .andExpect(jsonPath("$.activities[*].id", hasItem(activityId3)));
    }

    @Test
    void shouldReturn204_onDeleteActivity_whenValidRequest() throws Exception {
        String activityId = createActivityAndGetId();

        mockMvc.perform(delete("/api/activities/" + activityId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void shouldNotShowDeletedActivity_afterDeleteActivity_whenValidRequest() throws Exception {
        String activityId = createActivityAndGetId();

        mockMvc.perform(delete("/api/activities/" + activityId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities[*].id", not(hasItem(activityId))));
    }

    @Test
    void shouldReturn404_onDeleteActivity_whenActivityDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/activities/0123456789ABC"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400_onDeleteActivity_whenActivityIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/activities/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200AndDTO_onUpdateActivity_whenValidRequestWithoutLists() throws Exception {
        String initialActivityPayloadAsJson = """
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """;
        String activityId = createActivityAndGetId(initialActivityPayloadAsJson);
        String updateActivityPayloadAsJson = String.format("""
                {
                    "id": "%s",
                    "title": "Some Activity UPDATE",
                    "notes": "Some notes UPDATE",
                    "startAt": "2023-01-01T03:00:00Z",
                    "endAt": "2023-01-01T05:00:00Z",
                    "categoryAllocations": []
                }
                """, activityId);

        mockMvc.perform(put("/api/activities/" + activityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateActivityPayloadAsJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.title").value("Some Activity UPDATE"))
                .andExpect(jsonPath("$.notes").value("Some notes UPDATE"))
                .andExpect(jsonPath("$.startAt").value("2023-01-01T03:00:00Z"))
                .andExpect(jsonPath("$.endAt").value("2023-01-01T05:00:00Z"))
                .andExpect(jsonPath("$.durationInSeconds").value(7200));
    }

    @Test
    void shouldReturn200AndDTO_onUpdateActivity_whenValidRequestWithLists() throws Exception {
        String categoryId1 = createCategoryAndGetId();
        String categoryId2 = createCategoryAndGetId();
        String categoryId3 = createCategoryAndGetId();
        String initialActivityPayloadAsJson = String.format("""
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [
                        {
                            "percentage": 60,
                            "categoryId": "%s",
                            "subCategoryId": null
                        },
                        {
                            "percentage": 20,
                            "categoryId": "%s",
                            "subCategoryId": null
                        },
                        {
                            "percentage": 20,
                            "categoryId": "%s",
                            "subCategoryId": null
                        }
                    ],
                    "customValues": [],
                    "tagIds": []
                }
                """, categoryId1, categoryId2, categoryId3);
        String activityId = createActivityAndGetId(initialActivityPayloadAsJson);

        String categoryId4 = createCategoryAndGetId();
        String updateActivityPayloadAsJson = String.format("""
                {
                    "id": "%s",
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [
                        {
                            "percentage": 10,
                            "categoryId": "%s",
                            "subCategoryId": null
                        },
                        {
                            "percentage": 30,
                            "categoryId": "%s",
                            "subCategoryId": null
                        },
                        {
                            "percentage": 60,
                            "categoryId": "%s",
                            "subCategoryId": null
                        }
                    ]
                }
                """, activityId, categoryId4, categoryId2, categoryId1);

        mockMvc.perform(put("/api/activities/" + activityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateActivityPayloadAsJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.title").value("Some Activity"))
                .andExpect(jsonPath("$.notes").value("Some notes"))
                .andExpect(jsonPath("$.startAt").value("2023-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.endAt").value("2023-01-01T01:00:00Z"))
                .andExpect(jsonPath("$.durationInSeconds").value(3600))
                .andExpect(jsonPath("$.categoryAllocations").isArray())
                .andExpect(jsonPath("$.categoryAllocations.length()").value(3))
                .andExpect(jsonPath("$.categoryAllocations[0].percentage").value(60))
                .andExpect(jsonPath("$.categoryAllocations[0].category.id").value(categoryId1))
                .andExpect(jsonPath("$.categoryAllocations[1].percentage").value(30))
                .andExpect(jsonPath("$.categoryAllocations[1].category.id").value(categoryId2))
                .andExpect(jsonPath("$.categoryAllocations[2].percentage").value(10))
                .andExpect(jsonPath("$.categoryAllocations[2].category.id").value(categoryId4));
    }

    @Test
    void shouldReturn404_onUpdateActivity_whenActivityDoesNotExist() throws Exception {
        String updateActivityPayloadAsJson = """
                {
                    "id": "0123456789ABC",
                    "title": "Some Activity UPDATE",
                    "notes": "Some notes UPDATE",
                    "startAt": "2023-01-01T03:00:00Z",
                    "endAt": "2023-01-01T05:00:00Z",
                    "categoryAllocations": []
                }
                """;

        mockMvc.perform(put("/api/activities/0123456789ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateActivityPayloadAsJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400_onUpdateActivity_whenActivityIdIsInvalid() throws Exception {
        String updateActivityPayloadAsJson = """
                {
                    "id": "invalid-id",
                    "title": "Some Activity UPDATE",
                    "notes": "Some notes UPDATE",
                    "startAt": "2023-01-01T03:00:00Z",
                    "endAt": "2023-01-01T05:00:00Z",
                    "categoryAllocations": [],
                }
                """;
        mockMvc.perform(put("/api/activities/invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateActivityPayloadAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_onUpdateActivity_whenActivityIdDoesNotMatchPayloadActivityId() throws Exception {
        String activityId1 = createActivityAndGetId();
        String activityId2 = createActivityAndGetId();

        String updateActivityPayloadAsJson = String.format("""
                {
                    "id": "%s",
                    "title": "Some Activity UPDATE",
                    "notes": "Some notes UPDATE",
                    "startAt": "2023-01-01T03:00:00Z",
                    "endAt": "2023-01-01T05:00:00Z",
                    "categoryAllocations": [],
                }
                """, activityId1);
        mockMvc.perform(put("/api/activities/" + activityId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateActivityPayloadAsJson))
                .andExpect(status().isBadRequest());
    }

    private String createActivityWithTimesAndReturnId(String title, String startAt, String endAt) throws Exception {
        String payloadAsJson = String.format("""
                {
                    "title": "%s",
                    "notes": "Some notes",
                    "startAt": "%s",
                    "endAt": "%s",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """, title, startAt, endAt);

        return createActivityAndGetId(payloadAsJson);
    }

    private String createActivityAndGetId() throws Exception {
        return createActivityAndGetId("irrelevant-activity-json-payload");
    }

    private String createActivityWithCategoryAndGetId(String categoryId) throws Exception {
        return createActivityAndGetId(String.format("""
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [
                        {
                            "categoryId": "%s",
                            "percentage": 100
                        }
                    ],
                    "customValues": [],
                    "tagIds": []
                }
                """, categoryId));
    }

    private String createActivityWithTimesAndCategoryAndGetId(String startTime, String endTime, String categoryId) throws Exception {
        return createActivityAndGetId(String.format("""
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "%s",
                    "endAt": "%s",
                    "categoryAllocations": [
                        {
                            "categoryId": "%s",
                            "percentage": 100
                        }
                    ],
                    "customValues": [],
                    "tagIds": []
                }
                """, startTime, endTime, categoryId));
    }

    private String createActivityAndGetId(String content) throws Exception {
        String payloadAsJson = content.equals("irrelevant-activity-json-payload") ?
                """
                        {
                            "title": "Some Activity",
                            "notes": "Some notes",
                            "startAt": "2023-01-01T00:00:00Z",
                            "endAt": "2023-01-01T01:00:00Z",
                            "categoryAllocations": [],
                            "customValues": [],
                            "tagIds": []
                        }
                        """
                : content;

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

    private String createTagAndGetId(int sortOrder) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "label": "easy",
                                    "color": "#123456",
                                    "description": "easy to do",
                                    "sortOrder": %d
                                }
                                """, sortOrder)))
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
                                    "name": "some subcategory",
                                    "description": "some description"
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