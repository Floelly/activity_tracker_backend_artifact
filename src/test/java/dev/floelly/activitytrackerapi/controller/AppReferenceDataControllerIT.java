package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mysql.MySQLContainer;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class AppReferenceDataControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MySQLContainer mysql;

    @Test
    void shouldReturnAppReferenceDataResponse_onGetAppReferenceData() throws Exception {
        mockMvc.perform(get("/api/app-reference-data"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    void shouldReturnNewlyCreatedCategoryWithEmptySubcategories_onGetAppReferenceData() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "AppReferenceDataControllerIT",
                                    "color": "#123456",
                                    "icon": "icon-name",
                                    "description": "some description"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/app-reference-data"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[*].name", hasItem("AppReferenceDataControllerIT")))
                .andExpect(jsonPath(
                        "$.categories[?(@.name == 'AppReferenceDataControllerIT')].subcategories[0]",
                        empty()
                ));
    }
}