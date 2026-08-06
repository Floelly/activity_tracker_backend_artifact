package dev.floelly.activitytrackerapi.feature.activityexport;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/export-csv")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ExportController {

    private final ExportService exportService;

    @GetMapping
    public ResponseEntity<byte[]> exportCsv(@Valid @ModelAttribute ExportFilterDTO filter) {
        String csv = exportService.exportCsv(filter);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activities.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}