package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.exception.NotFoundException;
import org.junit.jupiter.api.Test;
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

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    void getStatistics_shouldReturnOk_whenNoFilter() throws Exception {
        StatisticsResponse response = new StatisticsResponse(5, 36000L, 7200L);
        when(statisticsService.getStatistics(any())).thenReturn(response);

        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalActivities").value(5))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(36000))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(7200));

        verify(statisticsService).getStatistics(any());
    }

    @Test
    void getStatistics_shouldReturnOk_whenDateRangeProvided() throws Exception {
        StatisticsResponse response = new StatisticsResponse(2, 10800L, 5400L);
        when(statisticsService.getStatistics(any())).thenReturn(response);

        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "2041-01-01T00:00:00Z")
                        .param("toStartAt", "2041-01-03T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalActivities").value(2))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(10800))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(5400));

        verify(statisticsService).getStatistics(any());
    }

    @Test
    void getStatistics_shouldReturnOk_whenCategoryProvided() throws Exception {
        StatisticsResponse response = new StatisticsResponse(1, 3600L, 3600L);
        when(statisticsService.getStatistics(any())).thenReturn(response);

        mockMvc.perform(get("/api/statistics")
                        .param("category", "0000123456789"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalActivities").value(1))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(3600))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(3600));

        verify(statisticsService).getStatistics(any());
    }

    @Test
    void getStatistics_shouldReturnBadRequest_whenInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "2044-01-02T00:00:00Z")
                        .param("toStartAt", "2044-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());

        verify(statisticsService, never()).getStatistics(any());
    }

    @Test
    void getStatistics_shouldReturnBadRequest_whenInvalidCategoryId() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .param("category", "invalid-id-not-tsid"))
                .andExpect(status().isBadRequest());

        verify(statisticsService, never()).getStatistics(any());
    }

    @Test
    void getStatistics_shouldReturnNotFound_whenServiceThrowsNotFoundException() throws Exception {
        when(statisticsService.getStatistics(any())).thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(get("/api/statistics")
                        .param("category", "0000123456789"))
                .andExpect(status().isNotFound());

        verify(statisticsService).getStatistics(any());
    }

    @Test
    void getStatistics_shouldReturnZeroMetrics_whenNoActivitiesMatch() throws Exception {
        StatisticsResponse response = new StatisticsResponse(0, 0L, 0L);
        when(statisticsService.getStatistics(any())).thenReturn(response);

        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "1900-01-01T00:00:00Z")
                        .param("toStartAt", "1900-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(0))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(0))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(0));

        verify(statisticsService).getStatistics(any());
    }
}