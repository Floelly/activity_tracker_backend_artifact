package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.TestcontainersConfiguration;
import dev.floelly.activitytrackerapi.dto.response.CategoriesResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MySQLContainer mysql;

    @Test
    void shouldReturn201_onPostCategory_whenValidRequest() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Some Category",
                                    "color": "#123456",
                                    "description": "some category description",
                                    "icon": "icon-name"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400_onPostCategory_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "color": "#123456",
                                    "description": "some category description",
                                    "icon": "icon-name"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200AndUpdatedCategory_onPutCategory_whenValidRequest() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(put("/api/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "%s",
                                    "name": "Updated Category",
                                    "color": "#654321",
                                    "description": "updated description",
                                    "icon": "updated-icon"
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Updated Category"))
                .andExpect(jsonPath("$.color").value("#654321"))
                .andExpect(jsonPath("$.description").value("updated description"))
                .andExpect(jsonPath("$.icon").value("updated-icon"))
                .andExpect(jsonPath("$.subcategories").isArray())
                .andExpect(jsonPath("$.subcategories").isEmpty());
    }

    @Test
    void shouldReturn404_onPutCategory_whenCategoryDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/categories/0123456789ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "0123456789ABC",
                                    "name": "Updated Category",
                                    "color": "#654321",
                                    "description": "updated description",
                                    "icon": "updated-icon"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnCategoriesResponseDTO_onGetAllCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    void shouldReturnNewlyCreatedCategoryWithoutSubCategories_onGetAllCategories() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "CategoryControllerIT",
                                    "color": "#123456",
                                    "description": "some category description",
                                    "icon": "icon-name"
                                }
                                """))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[*].name", hasItem("CategoryControllerIT")))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        CategoriesResponse response = objectMapper.readValue(json, CategoriesResponse.class);

        var category = response.categories().stream()
                .filter(c -> c.name().equals("CategoryControllerIT"))
                .findFirst()
                .orElseThrow();

        assertThat(category.subcategories()).isNotNull();
        assertThat(category.subcategories()).isEmpty();
    }

    @Test
    void shouldReturn201_onPostSubCategory_whenValidRequest() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(post("/api/categories/" + categoryId + "/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Some Subcategory",
                                    "description": "some subcategory description"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400_onPostSubCategory_whenNameIsBlank() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(post("/api/categories/" + categoryId + "/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "some subcategory description"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnSubCategoriesResponseDTO_onGetAllSubCategories() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(get("/api/categories/" + categoryId + "/subcategories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.subcategories").isArray());
    }

    @Test
    void shouldReturnNewlyCreatedSubCategory_onGetAllSubCategories() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(post("/api/categories/" + categoryId + "/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "CategoryControllerIT2",
                                    "description": "some subcategory description"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/categories/" + categoryId + "/subcategories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.subcategories").isArray())
                .andExpect(jsonPath("$.subcategories[*].name", hasItem("CategoryControllerIT2")));
    }

    @Test
    void shouldFindUpdatedCategory_onGetAllCategories_afterSuccessfulPutCategory() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(put("/api/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "%s",
                                    "name": "Updated Category IT",
                                    "color": "#654321",
                                    "description": "updated description it",
                                    "icon": null
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[*].name", hasItem("Updated Category IT")))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        CategoriesResponse response = objectMapper.readValue(json, CategoriesResponse.class);

        var category = response.categories().stream()
                .filter(c -> c.id().equals(categoryId))
                .findFirst()
                .orElseThrow();

        assertThat(category.name()).isEqualTo("Updated Category IT");
        assertThat(category.color()).isEqualTo("#654321");
        assertThat(category.description()).isEqualTo("updated description it");
        assertThat(category.icon()).isNull();
        assertThat(category.subcategories()).isNotNull();
        assertThat(category.subcategories()).isEmpty();
    }

    @Test
    void shouldReturn204_onDeleteCategory_whenValidRequest() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void shouldDeleteCategory_onDeleteCategory_whenValidRequest() throws Exception {
        String categoryId = createCategoryAndGetId();

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[*].id", not(hasItem(categoryId))))
                .andReturn();
    }

    @Test
    void shouldReturn404_onDeleteCategory_whenCategoryDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/categories/0123456789ABC"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400_onDeleteCategory_whenCategoryIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/categories/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409_onDeleteCategory_whenCategoryHasAssignedActivities() throws Exception {
        String categoryId = createCategoryAndGetId();

        createActivityAssignedToCategory(categoryId);

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isConflict());
    }


    @Test
    void shouldReturn400_onGetAllSubCategories_whenCategoryIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/categories/" + "invalid-id" + "/subcategories"))
                .andExpect(status().isBadRequest());
    }

    private String createCategoryAndGetId() throws Exception {
        MvcResult createCategoryResult = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Generated Category",
                                    "color": "#123456",
                                    "description": "some generated category description",
                                    "icon": "icon-name"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(createCategoryResult.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    private void createActivityAssignedToCategory(String categoryId) throws Exception {
        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Test Activity",
                                    "notes": "some notes",
                                    "startAt": "2026-05-25T08:00:00Z",
                                    "endAt": "2026-05-25T09:00:00Z",
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
                                """.formatted(categoryId)))
                .andExpect(status().isCreated());
    }
}