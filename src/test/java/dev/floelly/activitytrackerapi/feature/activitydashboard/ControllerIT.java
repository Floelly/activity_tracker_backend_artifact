package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class ControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MySQLContainer mysql;

    @Test
    void shouldReturnEmptyResponse_whenNoCategoryMatchesFilters() throws Exception {
        mockMvc.perform(get("/api/activities-dashboard?from=2000-01-01T00:00:00Z&to=2000-01-02T00:00:00Z&granularity=WEEK"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.filters.from").value("2000-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.filters.to").value("2000-01-02T00:00:00Z"))
                .andExpect(jsonPath("$.filters.granularity").value("WEEK"))
                .andExpect(jsonPath("$.filters.category").doesNotExist())
                .andExpect(jsonPath("$.summary.totalMinutes").value(0))
                .andExpect(jsonPath("$.summary.averageMinutesPerPeriod").value(0))
                .andExpect(jsonPath("$.summary.categories").isArray())
                .andExpect(jsonPath("$.summary.categories.length()").value(0))
                .andExpect(jsonPath("$.timeSeries.granularity").value("WEEK"))
                .andExpect(jsonPath("$.timeSeries.periods").isArray())
                .andExpect(jsonPath("$.timeSeries.periods.length()").value(1))
                .andExpect(jsonPath("$.timeSeries.periods[0].from").value("2000-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.timeSeries.periods[0].to").value("2000-01-02T00:00:00Z"))
                .andExpect(jsonPath("$.timeSeries.periods[0].totalMinutes").value(0))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories").isArray())
                .andExpect(jsonPath("$.timeSeries.periods[0].categories.length()").value(0));
    }

    @Test
    void shouldReturnNewlyCreatedActivity_whenMatchingFilters() throws Exception {
        String allowedCategory = createCategoryAndGetId();
        String notAllowedCategory = createCategoryAndGetId();

        // Activity before filterRange (12h)
        createActivity("2001-01-01T00:00:00Z", "2001-01-01T12:00:00Z", allowedCategory);
        // Activity with the wrong category (4h)
        createActivity("2001-02-01T08:00:00Z", "2001-02-01T12:00:00Z", notAllowedCategory);
        // Activity after filterRange (13h)
        createActivity("2001-02-11T00:00:00Z", "2001-02-11T13:00:00Z", allowedCategory);
        // Activity within filterRange and categoryFilter (1h)
        createActivity("2001-02-01T08:00:00Z", "2001-02-01T09:00:00Z", allowedCategory);

        String fromFilter = "2001-02-01T00:00:00Z";
        String toFilter = "2001-02-10T00:00:00Z";

        String path = String.format("/api/activities-dashboard?from=%s&to=%s&granularity=WEEK&category=%s", fromFilter, toFilter, allowedCategory);
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.filters.from").value(fromFilter))
                .andExpect(jsonPath("$.filters.to").value(toFilter))
                .andExpect(jsonPath("$.filters.granularity").value("WEEK"))
                .andExpect(jsonPath("$.filters.category").isArray())
                .andExpect(jsonPath("$.filters.category.length()").value(1))
                .andExpect(jsonPath("$.filters.category[0]").value(allowedCategory))
                .andExpect(jsonPath("$.summary.totalMinutes").value(60))
                .andExpect(jsonPath("$.summary.averageMinutesPerPeriod").value(30))
                .andExpect(jsonPath("$.summary.categories").isArray())
                .andExpect(jsonPath("$.summary.categories.length()").value(1))
                .andExpect(jsonPath("$.summary.categories[0].id").value(allowedCategory))
                .andExpect(jsonPath("$.summary.categories[0].minutes").value(60))
                .andExpect(jsonPath("$.summary.categories[0].percentage").value(1))
                .andExpect(jsonPath("$.timeSeries.granularity").value("WEEK"))
                .andExpect(jsonPath("$.timeSeries.periods").isArray())
                .andExpect(jsonPath("$.timeSeries.periods.length()").value(2))
                .andExpect(jsonPath("$.timeSeries.periods[0].from").value(fromFilter))
                .andExpect(jsonPath("$.timeSeries.periods[0].to").value("2001-02-08T00:00:00Z"))
                .andExpect(jsonPath("$.timeSeries.periods[0].totalMinutes").value(60))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories").isArray())
                .andExpect(jsonPath("$.timeSeries.periods[0].categories.length()").value(1))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].id").value(allowedCategory))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].minutes").value(60))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].percentage").value(1))
                .andExpect(jsonPath("$.timeSeries.periods[1].from").value("2001-02-08T00:00:00Z"))
                .andExpect(jsonPath("$.timeSeries.periods[1].to").value(toFilter))
                .andExpect(jsonPath("$.timeSeries.periods[1].totalMinutes").value(0))
                .andExpect(jsonPath("$.timeSeries.periods[1].categories").isArray())
                .andExpect(jsonPath("$.timeSeries.periods[1].categories.length()").value(1))
                .andExpect(jsonPath("$.timeSeries.periods[1].categories[0].id").value(allowedCategory))
                .andExpect(jsonPath("$.timeSeries.periods[1].categories[0].minutes").value(0))
                .andExpect(jsonPath("$.timeSeries.periods[1].categories[0].percentage").value(0));
    }

    @Test
    void shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/activities-dashboard?from=2000-01-01T00:00:00Z&to=2000-01-02T00:00:00Z&granularity=WEEK&category=0000123456789"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest(name = "queryParams: {0}")
    @ValueSource(strings = {
            "?to=2000-01-02T00:00:00Z&granularity=DAY",
            "?from=2000-01-01T00:00:00Z&granularity=DAY",
            "?from=2000-01-01T00:00:00Z&to=2000-01-02T00:00:00Z"
    })
    void shouldReturnBadRequest_whenInvalidFilters(String queryString) throws Exception {
        mockMvc.perform(get("/api/activities-dashboard" + queryString))
                .andExpect(status().isBadRequest());
    }

    private void createActivity(String startTime, String endTime, String categoryId) throws Exception {
        String categoryAllocations = categoryId != null
                ? String.format("""
                [
                        {
                            "categoryId": "%s",
                            "percentage": 100
                        }
                    ]
                """, categoryId)
                : "[]";
        String payloadAsJson = String.format("""
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "%s",
                    "endAt": "%s",
                    "categoryAllocations": %s,
                    "customValues": [],
                    "tagIds": []
                }
                """, startTime, endTime, categoryAllocations);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAsJson))
                .andExpect(status().isCreated());
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
}