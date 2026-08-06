package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.feature.activityexport.dto.ActivityExportFilterDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityExportController.class)
class ActivityExportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ActivityExportService activityExportService;

    @Test
    void exportCsv_shouldReturnCsvContent_whenNoFiltersProvided() throws Exception {
        String csvContent = "businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\r\n" +
                "ABC123,Test Activity,2024-01-01T10:00:00Z,2024-01-01T11:00:00Z,60,,,\r\n";

        when(activityExportService.exportCsv(any(ActivityExportFilterDTO.class))).thenReturn(csvContent);

        mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv;charset=UTF-8")))
                .andExpect(header().string("Content-Disposition", "attachment; filename=activities.csv"))
                .andExpect(content().string(csvContent));

        verify(activityExportService).exportCsv(any(ActivityExportFilterDTO.class));
    }

    @Test
    void exportCsv_shouldPassCategoryIdToService() throws Exception {
        String categoryId = "0123456789ABC";
        String csvContent = "businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\r\n";

        when(activityExportService.exportCsv(any(ActivityExportFilterDTO.class))).thenReturn(csvContent);

        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", categoryId))
                .andExpect(status().isOk());

        verify(activityExportService).exportCsv(any(ActivityExportFilterDTO.class));
    }

    @Test
    void exportCsv_shouldPassDateRangeToService() throws Exception {
        String csvContent = "businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\r\n";

        when(activityExportService.exportCsv(any(ActivityExportFilterDTO.class))).thenReturn(csvContent);

        mockMvc.perform(get("/api/export-csv")
                        .param("startDate", "2024-01-01T00:00:00Z")
                        .param("endDate", "2024-01-31T23:59:59Z"))
                .andExpect(status().isOk());

        verify(activityExportService).exportCsv(any(ActivityExportFilterDTO.class));
    }

    @Test
    void exportCsv_shouldReturnBadRequest_whenStartDateAfterEndDate() throws Exception {
        when(activityExportService.exportCsv(any(ActivityExportFilterDTO.class)))
                .thenThrow(new BadRequestException("startDate must be before or equal to endDate"));

        mockMvc.perform(get("/api/export-csv")
                        .param("startDate", "2024-02-01T00:00:00Z")
                        .param("endDate", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportCsv_shouldReturnBadRequest_whenCategoryDoesNotExist() throws Exception {
        when(activityExportService.exportCsv(any(ActivityExportFilterDTO.class)))
                .thenThrow(new BadRequestException("Category with id '0123456789ABC' not found."));

        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", "0123456789ABC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportCsv_shouldSetCorrectContentHeaders_forDownload() throws Exception {
        String csvContent = "businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\r\n";

        when(activityExportService.exportCsv(any(ActivityExportFilterDTO.class))).thenReturn(csvContent);

        mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=activities.csv"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv;charset=UTF-8")));
    }
}