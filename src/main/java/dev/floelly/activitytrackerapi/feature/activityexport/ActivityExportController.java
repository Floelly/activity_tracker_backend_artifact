package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.feature.activityexport.dto.ActivityExportFilterDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ActivityExportController {

    private final ActivityExportService activityExportService;

    @GetMapping("/api/export-csv")
    public void exportCsv(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws IOException {
        ActivityExportFilterDTO filter = new ActivityExportFilterDTO(categoryId, startDate, endDate);
        String csvContent = activityExportService.exportCsv(filter);

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activities.csv");
        response.getWriter().write(csvContent);
    }
}