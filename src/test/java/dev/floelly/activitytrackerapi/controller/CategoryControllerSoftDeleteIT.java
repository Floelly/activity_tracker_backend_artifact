package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.TestcontainersConfiguration;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SuppressWarnings("SqlNoDataSourceInspection")
class CategoryControllerSoftDeleteIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deleteCategory_shouldSoftDeleteCategory_whenValidId() throws Exception {
        String categoryId = createCategoryAndGetId("F5 Soft Delete Category 1");

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertCategoryRowStillExistsWithDeletedTimestamp(categoryId);
    }

    @Test
    void deleteCategory_shouldBeIdempotent_whenAlreadySoftDeleted() throws Exception {
        String categoryId = createCategoryAndGetId("F5 Soft Delete Category 2");

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertCategoryRowStillExistsWithDeletedTimestamp(categoryId);
    }

    @Test
    void getCategories_shouldNotIncludeDeletedCategories_byDefault() throws Exception {
        String categoryId = createCategoryAndGetId("F5 Soft Delete Category 3");

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories[*].id", not(hasItem(categoryId))));
    }

    @Test
    void softDeletedCategory_shouldNotAppearInAppReferenceData() throws Exception {
        String categoryId = createCategoryAndGetId("F5 Soft Delete Category 4");

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/app-reference-data"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories[*].id", not(hasItem(categoryId))));
    }

    @Test
    void existingActivitiesWithDeletedCategory_shouldStillBeQueryable() throws Exception {
        String categoryId = createCategoryAndGetId("F5 Soft Delete Category 5");
        String activityId = createActivityWithCategoryAndGetId(categoryId);

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/activities/" + activityId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.categoryAllocations").isArray())
                .andExpect(jsonPath("$.categoryAllocations.length()").value(1))
                .andExpect(jsonPath("$.categoryAllocations[0].category.id").value(categoryId));

        assertCategoryRowStillExistsWithDeletedTimestamp(categoryId);
    }

    @Test
    void createActivity_shouldFailWhenReferencingDeletedCategory_forNewActivities() throws Exception {
        String categoryId = createCategoryAndGetId("F5 Soft Delete Category 6");

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createActivityPayloadWithCategory(categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Throwable resolvedException = result.getResolvedException();
                    assertThat(resolvedException).isNotNull();
                    assertThat(resolvedException)
                            .isInstanceOf(BadRequestException.class)
                            .hasMessageContaining("deleted");
                });
    }

    @Test
    void updateActivity_shouldFailWhenReferencingDeletedCategory_forUpdatingActivities() throws Exception {
        String activityId = createActivityAndGetId();
        String categoryId = createCategoryAndGetId("F5 Soft Delete Category 7");

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/activities/" + activityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateActivityPayloadWithCategory(activityId, categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Throwable resolvedException = result.getResolvedException();
                    assertThat(resolvedException).isNotNull();
                    assertThat(resolvedException)
                            .isInstanceOf(BadRequestException.class)
                            .hasMessageContaining("deleted");
                });
    }

    private void assertCategoryRowStillExistsWithDeletedTimestamp(String categoryId) {
        Integer categoryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM category WHERE business_id = ?",
                Integer.class,
                categoryId
        );
        Integer deletedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM category WHERE business_id = ? AND deleted_at IS NOT NULL",
                Integer.class,
                categoryId
        );

        assertThat(categoryCount).isEqualTo(1);
        assertThat(deletedCount).isEqualTo(1);
    }

    private String createActivityAndGetId() throws Exception {
        return createActivityAndGetId("""
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [],
                    "customValues": [],
                    "tagIds": []
                }
                """);
    }

    private String createActivityWithCategoryAndGetId(String categoryId) throws Exception {
        return createActivityAndGetId(createActivityPayloadWithCategory(categoryId));
    }

    private String createActivityAndGetId(String payloadAsJson) throws Exception {
        MvcResult createActivityResult = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(createActivityResult.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String createCategoryAndGetId(String categoryName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "%s",
                                    "color": "#123456",
                                    "description": "some category description",
                                    "icon": "icon-name"
                                }
                                """, categoryName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private String createActivityPayloadWithCategory(String categoryId) {
        return String.format("""
                {
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [
                        {
                            "percentage": 100,
                            "categoryId": "%s",
                            "subCategoryId": null
                        }
                    ],
                    "customValues": [],
                    "tagIds": []
                }
                """, categoryId);
    }

    private String updateActivityPayloadWithCategory(String activityId, String categoryId) {
        return String.format("""
                {
                    "id": "%s",
                    "title": "Some Activity",
                    "notes": "Some notes",
                    "startAt": "2023-01-01T00:00:00Z",
                    "endAt": "2023-01-01T01:00:00Z",
                    "categoryAllocations": [
                        {
                            "percentage": 100,
                            "categoryId": "%s",
                            "subCategoryId": null
                        }
                    ]
                }
                """, activityId, categoryId);
    }
}


