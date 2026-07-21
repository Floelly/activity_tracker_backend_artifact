package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
class ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private Service service;

    @Test
    void getActivitiesDashboard_shouldReturnOk_whenValidFilter() throws Exception {
        Response response = new Response(null, null, null);
        ActivitiesDashboardFilterDTO expectedFilter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2000-01-01T00:00:00Z"),
                Instant.parse("2000-01-02T00:00:00Z"),
                TimeGranularity.DAY,
                List.of("0000123456789"));
        when(service.getActivitiesDashboardResponse(any())).thenReturn(response);

        mockMvc.perform(get("/api/activities-dashboard?from=2000-01-01T00:00:00Z&to=2000-01-02T00:00:00Z&granularity=DAY&category=0000123456789"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(service).getActivitiesDashboardResponse(expectedFilter);
    }

    @Test
    void getActivitiesDashboard_shouldReturnResponseDtoFromService_whenValidFilter() throws Exception {
        Response response = new Response(
                new ActivitiesDashboardFilterDTO(
                        Instant.parse("2000-01-01T00:00:00Z"),
                        Instant.parse("2000-01-02T00:00:00Z"),
                        TimeGranularity.DAY,
                        List.of()),
                new SummaryResponse(101L, 102L, List.of(
                        new CategoryResponse("cat-id", "cat-name", 103L, 1f, "#123456", "default")
                )),
                new TimeSeriesResponse(TimeGranularity.DAY, List.of(new PeriodResponse(
                        Instant.parse("2000-01-01T01:00:00Z"),
                        Instant.parse("2000-01-02T01:00:00Z"),
                        104L,
                        List.of(new CategoryResponse("cat-id2", "cat-name2", 105L, .9f, "#654321", "some-icon")))
                )));
        when(service.getActivitiesDashboardResponse(any())).thenReturn(response);

        mockMvc.perform(get("/api/activities-dashboard?from=2000-01-01T00:00:00Z&to=2000-01-02T00:00:00Z&granularity=DAY"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.filters.from").value("2000-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.filters.to").value("2000-01-02T00:00:00Z"))
                .andExpect(jsonPath("$.filters.granularity").value("DAY"))
                .andExpect(jsonPath("$.filters.category").isArray())
                .andExpect(jsonPath("$.summary.totalMinutes").value(101))
                .andExpect(jsonPath("$.summary.averageMinutesPerPeriod").value(102))
                .andExpect(jsonPath("$.summary.categories").isArray())
                .andExpect(jsonPath("$.summary.categories[0].id").value("cat-id"))
                .andExpect(jsonPath("$.summary.categories[0].name").value("cat-name"))
                .andExpect(jsonPath("$.summary.categories[0].minutes").value(103))
                .andExpect(jsonPath("$.summary.categories[0].percentage").value(1))
                .andExpect(jsonPath("$.summary.categories[0].color").value("#123456"))
                .andExpect(jsonPath("$.summary.categories[0].icon").value("default"))
                .andExpect(jsonPath("$.timeSeries.granularity").value("DAY"))
                .andExpect(jsonPath("$.timeSeries.periods").isArray())
                .andExpect(jsonPath("$.timeSeries.periods[0].from").value("2000-01-01T01:00:00Z"))
                .andExpect(jsonPath("$.timeSeries.periods[0].to").value("2000-01-02T01:00:00Z"))
                .andExpect(jsonPath("$.timeSeries.periods[0].totalMinutes").value(104))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories").isArray())
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].id").value("cat-id2"))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].name").value("cat-name2"))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].minutes").value(105))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].percentage").value(0.9))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].color").value("#654321"))
                .andExpect(jsonPath("$.timeSeries.periods[0].categories[0].icon").value("some-icon"));

        verify(service).getActivitiesDashboardResponse(any());
    }

    @ParameterizedTest(name = "queryParams: {0}")
    @ValueSource(strings = {
            "?to=2000-01-02T00:00:00Z&granularity=DAY",
            "?from=2000-01-01T00:00:00Z&granularity=DAY",
            "?from=2000-01-01T00:00:00Z&to=2000-01-02T00:00:00Z"
    })
    void getActivitiesDashboard_shouldReturn4xxError_whenMissingFilter(String queryString) throws Exception {
        mockMvc.perform(get("/api/activities-dashboard" + queryString))
                .andExpect(status().isBadRequest());

        verify(service, never()).getActivitiesDashboardResponse(any());
    }

    @Test
    void getActivitiesDashboard_shouldReturnError_whenServiceThrowsError() throws Exception {
        when(service.getActivitiesDashboardResponse(any())).thenThrow(new NotFoundException("Not Found"));

        mockMvc.perform(get("/api/activities-dashboard?from=2000-01-01T00:00:00Z&to=2000-01-02T00:00:00Z&granularity=DAY"))
                .andExpect(status().isNotFound());

        verify(service).getActivitiesDashboardResponse(any());
    }
}