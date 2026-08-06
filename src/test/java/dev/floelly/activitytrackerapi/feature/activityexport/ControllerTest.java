package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExportController.class)
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportService exportService;

    @Test
    void exportCsv_shouldReturnCsvWithHeaders_whenNoParams() throws Exception {
        String csvContent = "businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\n";
        when(exportService.exportCsv(null, null, null)).thenReturn(csvContent);

        mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv;charset=UTF-8")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activities.csv"))
                .andExpect(content().string(csvContent));
    }

    @Test
    void exportCsv_shouldPassParamsToService() throws Exception {
        String categoryId = "cat-123";
        String startDate = "2024-01-01T00:00:00Z";
        String endDate = "2024-01-31T23:59:59Z";
        String csvContent = "businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\n";
        when(exportService.exportCsv(categoryId, startDate, endDate)).thenReturn(csvContent);

        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", categoryId)
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(content().string(csvContent));

        verify(exportService).exportCsv(categoryId, startDate, endDate);
    }

    @Test
    void exportCsv_shouldReturnBadRequest_whenServiceThrowsBadRequestException() throws Exception {
        when(exportService.exportCsv(anyString(), anyString(), anyString()))
                .thenThrow(new BadRequestException("Invalid request"));

        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", "nonexistent")
                        .param("startDate", "2024-01-01T00:00:00Z")
                        .param("endDate", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }
}