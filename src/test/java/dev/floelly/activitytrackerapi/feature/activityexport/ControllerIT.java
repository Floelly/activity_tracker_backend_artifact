package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ControllerIT {

    private static final List<String> EXPECTED_HEADER = List.of(
            "businessId",
            "title",
            "startAt",
            "endAt",
            "durationMinutes",
            "categories",
            "tags",
            "notes"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCsvRowsWithExpectedColumns_onExportActivities() throws Exception {
        String matchingCategoryId = createCategoryAndGetId();
        String secondCategoryId = createCategoryAndGetId();
        String firstTagId = createTagAndGetId(20);
        String secondTagId = createTagAndGetId(10);

        String activityId = createActivityAndGetId(
                "F6 Export Header Activity",
                "Notes for CSV export",
                "2034-01-15T08:00:00Z",
                "2034-01-15T09:30:00Z",
                List.of(matchingCategoryId, secondCategoryId),
                List.of(firstTagId, secondTagId)
        );

        MvcResult result = mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", matchingCategoryId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv;charset=UTF-8")))
                .andReturn();

        List<List<String>> rows = parseCsv(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(rows).hasSize(2);
        assertThat(rows.getFirst()).containsExactlyElementsOf(EXPECTED_HEADER);

        Map<String, String> exportedRow = asRowMap(rows.getFirst(), rows.get(1));
        assertThat(exportedRow)
                .containsEntry("businessId", activityId)
                .containsEntry("title", "F6 Export Header Activity")
                .containsEntry("startAt", "2034-01-15T08:00:00Z")
                .containsEntry("endAt", "2034-01-15T09:30:00Z")
                .containsEntry("durationMinutes", "90")
                .containsEntry("categories", joinSorted(List.of(matchingCategoryId, secondCategoryId)))
                .containsEntry("tags", joinSorted(List.of(firstTagId, secondTagId)))
                .containsEntry("notes", "Notes for CSV export");
    }

    @Test
    void shouldFilterActivitiesByCategory_whenCategoryIdProvided() throws Exception {
        String matchingCategoryId = createCategoryAndGetId();
        String otherCategoryId = createCategoryAndGetId();

        String matchingActivityId = createActivityAndGetId(
                "F6 Category Match",
                "Belongs to the filtered category",
                "2034-02-01T10:00:00Z",
                "2034-02-01T11:00:00Z",
                List.of(matchingCategoryId),
                List.of()
        );
        String otherActivityId = createActivityAndGetId(
                "F6 Category Non-Match",
                "Belongs to a different category",
                "2034-02-01T12:00:00Z",
                "2034-02-01T13:00:00Z",
                List.of(otherCategoryId),
                List.of()
        );
        String noCategoryActivityId = createActivityAndGetId(
                "F6 Category No Match",
                "Has no categories",
                "2034-02-01T14:00:00Z",
                "2034-02-01T15:00:00Z",
                List.of(),
                List.of()
        );

        MvcResult result = mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", matchingCategoryId))
                .andExpect(status().isOk())
                .andReturn();

        List<List<String>> rows = parseCsv(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<String> exportedIds = rows.stream()
                .skip(1)
                .map(List::getFirst)
                .toList();

        assertThat(exportedIds)
                .contains(matchingActivityId)
                .doesNotContain(otherActivityId)
                .doesNotContain(noCategoryActivityId);
    }

    @Test
    void shouldFilterActivitiesByDateRange_whenStartDateAndEndDateProvided() throws Exception {
        createActivityAndGetId(
                "F6 Date Range Before",
                "Before the exported range",
                "2034-03-09T10:00:00Z",
                "2034-03-09T11:00:00Z",
                List.of(),
                List.of()
        );
        String matchingActivityId = createActivityAndGetId(
                "F6 Date Range Match",
                "Inside the exported range",
                "2034-03-10T10:00:00Z",
                "2034-03-10T11:00:00Z",
                List.of(),
                List.of()
        );
        createActivityAndGetId(
                "F6 Date Range After",
                "After the exported range",
                "2034-03-11T10:00:00Z",
                "2034-03-11T11:00:00Z",
                List.of(),
                List.of()
        );

        MvcResult result = mockMvc.perform(get("/api/export-csv")
                        .param("startDate", "2034-03-10T00:00:00Z")
                        .param("endDate", "2034-03-10T23:59:59Z"))
                .andExpect(status().isOk())
                .andReturn();

        List<List<String>> rows = parseCsv(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<String> exportedIds = rows.stream()
                .skip(1)
                .map(List::getFirst)
                .toList();

        assertThat(exportedIds).containsExactly(matchingActivityId);
    }

    @Test
    void shouldReturnBadRequest_whenStartDateIsAfterEndDate() throws Exception {
        mockMvc.perform(get("/api/export-csv")
                        .param("startDate", "2034-04-11T12:00:00Z")
                        .param("endDate", "2034-04-11T11:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenCategoryDoesNotExist() throws Exception {
        String deletedCategoryId = createCategoryAndGetId();

        mockMvc.perform(delete("/api/categories/" + deletedCategoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", deletedCategoryId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSetCsvDownloadHeaders_whenExportActivities() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/export-csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv;charset=UTF-8")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activities.csv"))
                .andReturn();

        assertThat(result.getResponse().getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    }

    @Test
    void shouldEscapeCommasQuotesAndNewlines_whenExportingCsvFields() throws Exception {
        String categoryId = createCategoryAndGetId();
        String activityId = createActivityAndGetId(
                "F6 \"quoted\", title",
                "Line 1\nLine 2, with comma",
                "2034-05-01T08:00:00Z",
                "2034-05-01T08:45:00Z",
                List.of(categoryId),
                List.of()
        );

        MvcResult result = mockMvc.perform(get("/api/export-csv")
                        .param("categoryId", categoryId))
                .andExpect(status().isOk())
                .andReturn();

        List<List<String>> rows = parseCsv(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        Map<String, String> exportedRow = asRowMap(rows.getFirst(), rows.get(1));

        assertThat(exportedRow)
                .containsEntry("businessId", activityId)
                .containsEntry("title", "F6 \"quoted\", title")
                .containsEntry("notes", "Line 1\nLine 2, with comma");
    }

    private Map<String, String> asRowMap(List<String> header, List<String> row) {
        Map<String, String> rowMap = new LinkedHashMap<>();
        for (int i = 0; i < header.size(); i++) {
            rowMap.put(header.get(i), row.get(i));
        }
        return rowMap;
    }

    private List<List<String>> parseCsv(String csvContent) {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < csvContent.length(); i++) {
            char currentCharacter = csvContent.charAt(i);

            if (inQuotes) {
                if (currentCharacter == '"') {
                    if (i + 1 < csvContent.length() && csvContent.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentField.append(currentCharacter);
                }
                continue;
            }

            if (currentCharacter == '"') {
                inQuotes = true;
                continue;
            }

            if (currentCharacter == ',') {
                currentRow.add(currentField.toString());
                currentField.setLength(0);
                continue;
            }

            if (currentCharacter == '\r' || currentCharacter == '\n') {
                if (currentCharacter == '\r' && i + 1 < csvContent.length() && csvContent.charAt(i + 1) == '\n') {
                    i++;
                }
                currentRow.add(currentField.toString());
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                currentField.setLength(0);
                continue;
            }

            currentField.append(currentCharacter);
        }

        if (!currentField.isEmpty() || !currentRow.isEmpty()) {
            currentRow.add(currentField.toString());
            rows.add(currentRow);
        }

        return rows;
    }

    private String joinSorted(List<String> values) {
        return values.stream()
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private String createActivityAndGetId(
            String title,
            String notes,
            String startAt,
            String endAt,
            List<String> categoryIds,
            List<String> tagIds
    ) throws Exception {
        String categoryAllocationsJson = buildCategoryAllocationsJson(categoryIds);
        String tagIdsJson = tagIds.stream()
                .map(tagId -> "\"" + tagId + "\"")
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        MvcResult result = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": %s,
                                    "notes": %s,
                                    "startAt": "%s",
                                    "endAt": "%s",
                                    "categoryAllocations": %s,
                                    "customValues": [],
                                    "tagIds": [%s]
                                }
                                """.formatted(
                                toJsonString(title),
                                toJsonString(notes),
                                startAt,
                                endAt,
                                categoryAllocationsJson,
                                tagIdsJson
                        )))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String buildCategoryAllocationsJson(List<String> categoryIds) {
        if (categoryIds.isEmpty()) {
            return "[]";
        }

        int percentage = 100 / categoryIds.size();
        String allocations = categoryIds.stream()
                .map(categoryId -> """
                        {
                            "percentage": %d,
                            "categoryId": "%s",
                            "subCategoryId": null
                        }
                        """.formatted(percentage, categoryId))
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        return "[" + allocations + "]";
    }

    private String toJsonString(String value) {
        return objectMapper.writeValueAsString(value);
    }

    private String createCategoryAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Generated Export Category",
                                    "color": "#123456",
                                    "description": "generated export category description",
                                    "icon": "icon-name"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String createTagAndGetId(int sortOrder) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "label": "Generated Export Tag",
                                    "color": "#654321",
                                    "description": "generated export tag description",
                                    "sortOrder": %d
                                }
                                """.formatted(sortOrder)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }
}
