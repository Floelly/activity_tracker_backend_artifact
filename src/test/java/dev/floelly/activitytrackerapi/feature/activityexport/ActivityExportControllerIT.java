package dev.floelly.activitytrackerapi.feature.activityexport;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ActivityExportControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MySQLContainer mysql;

    @Test
    void exportActivities_shouldReturnCSV() throws Exception {
        createActivityAndGetId();
        createActivityAndGetId();

        mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv;charset=UTF-8")))
                .andExpect(header().string("Content-Disposition", "attachment; filename=activities.csv"))
                .andDo(result -> {
                    String csv = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(csv).startsWith("businessId,title,startAt,endAt,durationMinutes,categories,tags,notes");
                    String[] lines = csv.split("\r\n|\n");
                    org.assertj.core.api.Assertions.assertThat(lines.length).isGreaterThanOrEqualTo(3);
                });
    }

    @Test
    void exportActivities_shouldFilterByCategory_whenCategoryIdProvided() throws Exception {
        String categoryId1 = createCategoryAndGetId();
        String categoryId2 = createCategoryAndGetId();

        String activityWithCategory1 = createActivityWithCategoryAndGetId(categoryId1);
        createActivityWithCategoryAndGetId(categoryId2);

        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", categoryId1))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String csv = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(csv).contains(activityWithCategory1);
                });
    }

    @Test
    void exportActivities_shouldFilterByDateRange_whenDatesProvided() throws Exception {
        String activityInRange = createActivityWithTimesAndReturnId(
                "Activity In Range",
                "2024-06-15T10:00:00Z",
                "2024-06-15T11:00:00Z"
        );
        createActivityWithTimesAndReturnId(
                "Activity Before Range",
                "2024-05-01T10:00:00Z",
                "2024-05-01T11:00:00Z"
        );
        createActivityWithTimesAndReturnId(
                "Activity After Range",
                "2024-07-20T10:00:00Z",
                "2024-07-20T11:00:00Z"
        );

        mockMvc.perform(get("/api/export-csv")
                        .param("startDate", "2024-06-01T00:00:00Z")
                        .param("endDate", "2024-06-30T23:59:59Z"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String csv = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(csv).contains(activityInRange);
                    org.assertj.core.api.Assertions.assertThat(csv).doesNotContain("Activity Before Range");
                    org.assertj.core.api.Assertions.assertThat(csv).doesNotContain("Activity After Range");
                });
    }

    @Test
    void exportActivities_shouldFilterByCategoryAndDateRange_whenBothProvided() throws Exception {
        String categoryId = createCategoryAndGetId();

        String matchingActivity = createActivityWithTimesAndCategoryAndGetId(
                "2024-06-15T10:00:00Z", "2024-06-15T11:00:00Z", categoryId
        );
        createActivityWithTimesAndCategoryAndGetId(
                "2024-07-20T10:00:00Z", "2024-07-20T11:00:00Z", categoryId
        );

        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", categoryId)
                        .param("startDate", "2024-06-01T00:00:00Z")
                        .param("endDate", "2024-06-30T23:59:59Z"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String csv = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(csv).contains(matchingActivity);
                });
    }

    @Test
    void exportActivities_shouldReturnBadRequest_whenStartDateAfterEndDate() throws Exception {
        mockMvc.perform(get("/api/export-csv")
                        .param("startDate", "2024-06-30T00:00:00Z")
                        .param("endDate", "2024-06-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportActivities_shouldSetCorrectContentHeaders_forDownload() throws Exception {
        mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=activities.csv"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv;charset=UTF-8")));
    }

    @Test
    void exportActivities_shouldReturnBadRequest_whenCategoryDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", "9999999999999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportActivities_shouldEscapeSpecialCharactersInCsv() throws Exception {
        String payload = """
                {
                    "title": "Activity with, comma and \\"quote\\"",
                    "notes": "Notes with\\nnewline",
                    "startAt": "2024-06-15T10:00:00Z",
                    "endAt": "2024-06-15T11:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String activityId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andDo(result2 -> {
                    String csv = result2.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(csv).contains(activityId);
                    org.assertj.core.api.Assertions.assertThat(csv).contains("\"Activity with, comma and \"\"quote\"\"\"");
                });
    }

    @Test
    void exportActivities_shouldIncludeCategoriesAndTagsInCsv() throws Exception {
        String categoryId = createCategoryAndGetId();
        String tagId1 = createTagAndGetId(1);
        String tagId2 = createTagAndGetId(2);

        String payload = String.format("""
                {
                    "title": "Activity with category and tags",
                    "notes": "Some notes",
                    "startAt": "2024-06-15T10:00:00Z",
                    "endAt": "2024-06-15T11:00:00Z",
                    "categoryAllocations": [
                        {
                            "percentage": 100,
                            "categoryId": "%s"
                        }
                    ],
                    "customValues": [],
                    "tagIds": ["%s", "%s"]
                }
                """, categoryId, tagId1, tagId2);

        createActivityAndGetId(payload);

        mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String csv = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(csv).contains(categoryId);
                    org.assertj.core.api.Assertions.assertThat(csv).contains(tagId1);
                    org.assertj.core.api.Assertions.assertThat(csv).contains(tagId2);
                });
    }

    // ----- helper methods -----

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
        return createActivityAndGetId("""
                {
                    "title": "Export Test Activity",
                    "notes": "Some notes",
                    "startAt": "2024-06-15T10:00:00Z",
                    "endAt": "2024-06-15T11:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """);
    }

    private String createActivityWithCategoryAndGetId(String categoryId) throws Exception {
        return createActivityAndGetId(String.format("""
                {
                    "title": "Category Activity",
                    "notes": "Some notes",
                    "startAt": "2024-06-15T10:00:00Z",
                    "endAt": "2024-06-15T11:00:00Z",
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
                    "title": "Timed Category Activity",
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
        MvcResult createActivityResult = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
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
                                    "label": "tag-label-%d",
                                    "color": "#123456",
                                    "description": "tag description",
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
}