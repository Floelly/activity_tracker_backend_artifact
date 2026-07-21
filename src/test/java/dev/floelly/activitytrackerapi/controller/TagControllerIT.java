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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class TagControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private MySQLContainer mysql;

    @Test
    void shouldReturn201_onPostTags_whenValidRequest() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "label": "easy",
                                    "color": "#123456",
                                    "description": "easy to do",
                                    "sortOrder": 0
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400_onPostTags_whenLabelIsBlank() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                        {
                                            "label": "",
                                            "color": "#123456",
                                            "description": "easy to do",
                                            "sortOrder": 0
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTagsResponseDTO_onGetAllTags() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags").isArray());
    }

    @Test
    void shouldReturnNewlyCreatedTag_onGetAllTags() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "label": "TagControllerIT",
                                    "color": "#123456",
                                    "description": "easy to do",
                                    "sortOrder": 0
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags[*].label", hasItem("TagControllerIT")));
    }
}