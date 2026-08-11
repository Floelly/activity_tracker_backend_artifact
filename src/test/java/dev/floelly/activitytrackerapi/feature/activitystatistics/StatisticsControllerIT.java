package dev.floelly.activitytrackerapi.feature.activitystatistics;

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
class StatisticsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MySQLContainer mysql;

    @Test
    void getStatistics_shouldReturnAggregatedMetrics_whenFilteredByDateRange() throws Exception {
        // Arrange: Create 2 activities in 2041 (3600s + 7200s = 10800s total, average = 5400s)
        createActivityWithTimes("Activity 1", "2041-01-01T10:00:00Z", "2041-01-01T11:00:00Z");
        createActivityWithTimes("Activity 2", "2041-01-02T10:00:00Z", "2041-01-02T12:00:00Z");

        // Act & Assert
        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "2041-01-01T00:00:00Z")
                        .param("toStartAt", "2041-01-03T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalActivities").value(2))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(10800))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(5400));
    }

    @Test
    void getStatistics_shouldFilterByDateRange_whenFromAndToProvided() throws Exception {
        // Arrange: Create 3 activities, 2 in range, 1 before
        createActivityWithTimes("Before Range", "2042-01-01T10:00:00Z", "2042-01-01T11:00:00Z"); // outside
        createActivityWithTimes("In Range 1", "2042-02-01T10:00:00Z", "2042-02-01T11:00:00Z"); // 3600s
        createActivityWithTimes("In Range 2", "2042-02-02T10:00:00Z", "2042-02-02T13:00:00Z"); // 10800s

        // Act & Assert: Only the 2 in-range activities should be counted (14400s total, 7200s avg)
        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "2042-02-01T00:00:00Z")
                        .param("toStartAt", "2042-02-03T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(2))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(14400))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(7200));
    }

    @Test
    void getStatistics_shouldFilterByCategoryIds_whenCategoryIdsProvided() throws Exception {
        // Arrange: Create 3 categories and 3 activities
        String categoryA = createCategoryAndGetId();
        String categoryB = createCategoryAndGetId();
        String categoryC = createCategoryAndGetId();

        createActivityWithTimesAndCategory("Activity A", "2043-01-01T10:00:00Z", "2043-01-01T11:00:00Z", categoryA); // 3600s
        createActivityWithTimesAndCategory("Activity B", "2043-01-02T10:00:00Z", "2043-01-02T12:00:00Z", categoryB); // 7200s
        createActivityWithTimesAndCategory("Activity C", "2043-01-03T10:00:00Z", "2043-01-03T13:00:00Z", categoryC); // 10800s

        // Act & Assert: Only A and B should match (10800s total, 5400s avg)
        mockMvc.perform(get("/api/statistics")
                        .param("category", categoryA)
                        .param("category", categoryB)
                        .param("fromStartAt", "2043-01-01T00:00:00Z")
                        .param("toStartAt", "2043-01-04T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(2))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(10800))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(5400));
    }

    @Test
    void getStatistics_shouldReturnBadRequest_whenFromStartAtAfterToStartAt() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "2044-01-02T00:00:00Z")
                        .param("toStartAt", "2044-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatistics_shouldReturnBadRequest_whenCategoryIdIsInvalidTSID() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .param("category", "invalid-id-not-tsid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatistics_shouldReturnNotFound_whenCategoryIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .param("category", "0000000000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStatistics_shouldReturnZeroMetrics_whenNoActivitiesMatch() throws Exception {
        // Act & Assert: Request statistics for a date range far in the past with no activities
        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "1900-01-01T00:00:00Z")
                        .param("toStartAt", "1900-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(0))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(0))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(0));
    }

    // ============ Helper methods ============

    private String createActivityWithTimes(String title, String startAt, String endAt) throws Exception {
        String payloadAsJson = String.format("""
                {
                    "title": "%s",
                    "notes": "Test activity",
                    "startAt": "%s",
                    "endAt": "%s",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """, title, startAt, endAt);

        return createActivityAndGetId(payloadAsJson);
    }

    private String createActivityWithTimesAndCategory(String title, String startAt, String endAt, String categoryId) throws Exception {
        String payloadAsJson = String.format("""
                {
                    "title": "%s",
                    "notes": "Test activity",
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
                """, title, startAt, endAt, categoryId);

        return createActivityAndGetId(payloadAsJson);
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
                                    "name": "Test Category",
                                    "color": "#123456",
                                    "description": "auto-generated test category",
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
}

