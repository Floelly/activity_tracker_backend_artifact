package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.response.AppReferenceDataResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoryResponse;
import dev.floelly.activitytrackerapi.service.AppReferenceDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppReferenceDataController.class)
class AppReferenceDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppReferenceDataService appReferenceDataService;

    @Test
    void getAppReferenceData_shouldReturnOk() throws Exception {
        AppReferenceDataResponse response = new AppReferenceDataResponse(List.of(
                new CategoryResponse(
                        "cat-1",
                        "Health",
                        "#22c55e",
                        "heart",
                        "Health related category",
                        List.of(
                                new SubCategoryResponse(
                                        "sub-1",
                                        "Running",
                                        "Running related subcategory"
                                )
                        )
                ),
                new CategoryResponse(
                        "cat-2",
                        "Work",
                        "#3b82f6",
                        "briefcase",
                        "Work related category",
                        List.of()
                )
        ));

        when(appReferenceDataService.getAppReferenceData()).thenReturn(response);

        mockMvc.perform(get("/api/app-reference-data"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(2))
                .andExpect(jsonPath("$.categories[0].id").value("cat-1"))
                .andExpect(jsonPath("$.categories[0].name").value("Health"))
                .andExpect(jsonPath("$.categories[0].color").value("#22c55e"))
                .andExpect(jsonPath("$.categories[0].icon").value("heart"))
                .andExpect(jsonPath("$.categories[0].description").value("Health related category"))
                .andExpect(jsonPath("$.categories[0].subcategories").isArray())
                .andExpect(jsonPath("$.categories[0].subcategories.length()").value(1))
                .andExpect(jsonPath("$.categories[0].subcategories[0].id").value("sub-1"))
                .andExpect(jsonPath("$.categories[0].subcategories[0].name").value("Running"))
                .andExpect(jsonPath("$.categories[0].subcategories[0].description").value("Running related subcategory"))
                .andExpect(jsonPath("$.categories[1].id").value("cat-2"))
                .andExpect(jsonPath("$.categories[1].name").value("Work"))
                .andExpect(jsonPath("$.categories[1].subcategories", empty()));

        verify(appReferenceDataService).getAppReferenceData();
    }

    @Test
    void getAppReferenceData_shouldReturnOk_whenCategoriesAreEmpty() throws Exception {
        AppReferenceDataResponse response = new AppReferenceDataResponse(List.of());

        when(appReferenceDataService.getAppReferenceData()).thenReturn(response);

        mockMvc.perform(get("/api/app-reference-data"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(0));

        verify(appReferenceDataService).getAppReferenceData();
    }
}