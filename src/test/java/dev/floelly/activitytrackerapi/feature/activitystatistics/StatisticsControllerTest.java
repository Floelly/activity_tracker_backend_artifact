package dev.floelly.activitytrackerapi.feature.activitystatistics;

import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.request.StatisticsFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitystatistics.dto.response.StatisticsResponse;
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
    void getStatistics_shouldReturnOk_whenFilterProvided() throws Exception {
        StatisticsResponse response = new StatisticsResponse(2, 10800, 5400);

        when(statisticsService.getStatistics(any(StatisticsFilterDTO.class))).thenReturn(response);

        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "2026-01-01T00:00:00Z")
                        .param("toStartAt", "2026-01-03T00:00:00Z")
                        .param("category", "0000000000001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalActivities").value(2))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(10800))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(5400));

        verify(statisticsService).getStatistics(any(StatisticsFilterDTO.class));
    }

    @Test
    void getStatistics_shouldReturnBadRequest_whenFromStartAtAfterToStartAt() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .param("fromStartAt", "2026-01-02T00:00:00Z")
                        .param("toStartAt", "2026-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());

        verify(statisticsService, never()).getStatistics(any(StatisticsFilterDTO.class));
    }

    @Test
    void getStatistics_shouldReturnBadRequest_whenCategoryIdIsInvalidTSID() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .param("category", "invalid-id-not-tsid"))
                .andExpect(status().isBadRequest());

        verify(statisticsService, never()).getStatistics(any(StatisticsFilterDTO.class));
    }

    @Test
    void getStatistics_shouldReturnNotFound_whenServiceThrowsNotFoundException() throws Exception {
        when(statisticsService.getStatistics(any(StatisticsFilterDTO.class)))
                .thenThrow(new NotFoundException("Category with id '0000000000001' not found."));

        mockMvc.perform(get("/api/statistics")
                        .param("category", "0000000000001"))
                .andExpect(status().isNotFound());

        verify(statisticsService).getStatistics(any(StatisticsFilterDTO.class));
    }

    @Test
    void getStatistics_shouldReturnOk_whenNoFilterParameters() throws Exception {
        StatisticsResponse response = new StatisticsResponse(0, 0, 0);

        when(statisticsService.getStatistics(any(StatisticsFilterDTO.class))).thenReturn(response);

        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalActivities").value(0))
                .andExpect(jsonPath("$.totalDurationInSeconds").value(0))
                .andExpect(jsonPath("$.averageDurationInSeconds").value(0));

        verify(statisticsService).getStatistics(any(StatisticsFilterDTO.class));
    }
}