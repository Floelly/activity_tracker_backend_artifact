package dev.floelly.activitytrackerapi.controller;

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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class TagControllerQueryIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MySQLContainer mysql;

    @Test
    void getTags_shouldReturnDefaultLimit10AndSortedLabels_whenNoQueryAndNoLimit() throws Exception {
        seedTags(uniqueToken(), 12);

        MvcResult result = mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        List<String> labels = extractLabels(result);

        assertThat(labels).hasSize(10);
        assertSortedByStringCompare(labels);
    }

    @Test
    void getTags_shouldTreatEmptyQueryAsNull_whenQueryParamIsEmptyString() throws Exception {
        seedTags(uniqueToken(), 12);

        MvcResult withoutQuery = mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult withEmptyQuery = mockMvc.perform(get("/api/tags").param("query", ""))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(extractLabels(withEmptyQuery))
                .containsExactlyElementsOf(extractLabels(withoutQuery));
    }

    @Test
    void getTags_shouldTreatBlankQueryAsNull_whenQueryContainsOnlyWhitespace() throws Exception {
        seedTags(uniqueToken(), 12);

        MvcResult withoutQuery = mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult withBlankQuery = mockMvc.perform(get("/api/tags").param("query", "   "))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(extractLabels(withBlankQuery))
                .containsExactlyElementsOf(extractLabels(withoutQuery));
    }

    @Test
    void getTags_shouldSearchByTrimmedQuery_caseInsensitiveSubstring() throws Exception {
        String token = "PrOj" + uniqueToken();
        String expectedOne = "AA-" + token + "-One";
        String expectedTwo = "AA-" + token.toLowerCase() + "-Two";

        createTag(expectedOne, 10);
        createTag(expectedTwo, 1);
        createTag("ZZ-Other-" + uniqueToken(), 5);

        MvcResult result = mockMvc.perform(get("/api/tags")
                        .param("query", "  " + token.toLowerCase() + "  ")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> labels = extractLabels(result);

        assertThat(labels)
                .containsExactlyInAnyOrder(expectedOne, expectedTwo)
                .allMatch(label -> label.toLowerCase().contains(token.toLowerCase()));
        assertSortedByStringCompare(labels);
    }

    @Test
    void getTags_shouldReturnSameResultsForLowerAndUpperCaseQuery() throws Exception {
        String token = "MiXeD" + uniqueToken();

        createTag("A-" + token + "-Alpha", 7);
        createTag("A-" + token.toLowerCase() + "-Beta", 2);

        MvcResult lowerCaseResult = mockMvc.perform(get("/api/tags")
                        .param("query", token.toLowerCase())
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult upperCaseResult = mockMvc.perform(get("/api/tags")
                        .param("query", token.toUpperCase())
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(extractLabels(lowerCaseResult))
                .containsExactlyElementsOf(extractLabels(upperCaseResult));
    }

    @Test
    void getTags_shouldReturnEmptyList_whenQueryMatchesNothing() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/tags")
                        .param("query", "NO_MATCH_" + uniqueToken())
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(extractLabels(result)).isEmpty();
    }

    @Test
    void getTags_shouldSortByLabelAsc_whenMultipleTagsMatchQuery() throws Exception {
        String token = "SRT" + uniqueToken();

        createTag("X-" + token + "-C", 1);
        createTag("X-" + token + "-A", 3);
        createTag("X-" + token + "-B", 2);

        MvcResult result = mockMvc.perform(get("/api/tags")
                        .param("query", token)
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(extractLabels(result))
                .containsExactly("X-" + token + "-A", "X-" + token + "-B", "X-" + token + "-C");
    }

    @Test
    void getTags_shouldRespectLimit_whenLimitProvidedWithQuery() throws Exception {
        String token = "LIM" + uniqueToken();

        createTag("Y-" + token + "-C", 5);
        createTag("Y-" + token + "-A", 4);
        createTag("Y-" + token + "-B", 3);

        MvcResult result = mockMvc.perform(get("/api/tags")
                        .param("query", token)
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(extractLabels(result))
                .containsExactly("Y-" + token + "-A", "Y-" + token + "-B");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 101})
    void getTags_shouldReturnBadRequest_whenLimitOutsideAllowedRange(int invalidLimit) throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("limit", String.valueOf(invalidLimit)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100})
    void getTags_shouldAcceptBoundaryLimits_whenWithinAllowedRange(int validLimit) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/tags")
                        .param("limit", String.valueOf(validLimit)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(extractLabels(result)).hasSizeLessThanOrEqualTo(validLimit);
    }

    @Test
    void getTags_shouldReturnBadRequest_whenQueryExceedsMaxLength() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("query", "x".repeat(51)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTags_shouldAcceptMaxLengthQuery_whenExactly50Chars() throws Exception {
        String query = "x".repeat(50);

        mockMvc.perform(get("/api/tags")
                        .param("query", query)
                        .param("limit", "100"))
                .andExpect(status().isOk());
    }

    private void seedTags(String token, int amount) throws Exception {
        for (int i = 0; i < amount; i++) {
            String label = "A-" + token + "-" + String.format("%02d", i);
            createTag(label, amount - i);
        }
    }

    private void createTag(String label, int sortOrder) throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "label": "%s",
                                    "color": "#123456",
                                    "description": "query test data",
                                    "sortOrder": %s
                                }
                                """.formatted(label, sortOrder)))
                .andExpect(status().isCreated());
    }

    private List<String> extractLabels(MvcResult result) throws Exception {
        var root = objectMapper.readTree(result.getResponse().getContentAsString());
        var tags = root.get("tags");

        List<String> labels = new ArrayList<>();
        for (var tag : tags) {
            labels.add(tag.get("label").stringValue());
        }

        return labels;
    }

    private void assertSortedByStringCompare(List<String> labels) {
        List<String> expected = new ArrayList<>(labels);
        expected.sort(String::compareTo);
        assertThat(labels).containsExactlyElementsOf(expected);
    }

    private String uniqueToken() {
        return Long.toHexString(System.nanoTime());
    }
}

