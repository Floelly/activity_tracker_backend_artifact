package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateSubCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.response.CategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoryResponse;
import dev.floelly.activitytrackerapi.exception.NotFoundException;
import dev.floelly.activitytrackerapi.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void postNewCategory_shouldReturnCreatedCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(
                "cat-1",
                "some category",
                "#123456",
                "icon-name",
                "some description",
                List.of()
        );

        when(categoryService.registerNewCategory(any(CreateCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "some category req",
                                    "color": "#654321",
                                    "icon": "icon-name-req",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("cat-1"))
                .andExpect(jsonPath("$.name").value("some category"))
                .andExpect(jsonPath("$.color").value("#123456"))
                .andExpect(jsonPath("$.icon").value("icon-name"))
                .andExpect(jsonPath("$.description").value("some description"));

        verify(categoryService).registerNewCategory(any(CreateCategoryRequest.class));
    }

    @Test
    void postNewCategory_shouldPassValidatedRequestToService() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "some category req",
                            "color": "#654321",
                            "icon": "icon-name-req",
                            "description": "some description req"
                        }
                        """));

        ArgumentCaptor<CreateCategoryRequest> captor =
                ArgumentCaptor.forClass(CreateCategoryRequest.class);

        verify(categoryService).registerNewCategory(captor.capture());

        CreateCategoryRequest dto = captor.getValue();
        assertThat(dto.name()).isEqualTo("some category req");
        assertThat(dto.color()).isEqualTo("#654321");
        assertThat(dto.icon()).isEqualTo("icon-name-req");
        assertThat(dto.description()).isEqualTo("some description req");
    }

    @Test
    void postNewCategory_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "color": "#654321",
                                    "icon": "icon-name-req",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).registerNewCategory(any(CreateCategoryRequest.class));
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(
                "0123456789ABC",
                "some category",
                "#FF0000",
                "dumbbell",
                "some description",
                List.of()
        );

        when(categoryService.updateCategory(any(String.class), any(UpdateCategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/categories/0123456789ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "0123456789ABC",
                                    "name": "some category req",
                                    "color": "#FF0000",
                                    "icon": "dumbbell",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("0123456789ABC"))
                .andExpect(jsonPath("$.name").value("some category"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.icon").value("dumbbell"))
                .andExpect(jsonPath("$.description").value("some description"))
                .andExpect(jsonPath("$.subcategories").isArray())
                .andExpect(jsonPath("$.subcategories").isEmpty());

        verify(categoryService).updateCategory(any(String.class), any(UpdateCategoryRequest.class));
    }

    @Test
    void updateCategory_shouldPassValidatedRequestToService() throws Exception {
        mockMvc.perform(put("/api/categories/0123456789ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "0123456789ABC",
                                    "name": "some category req",
                                    "color": "#FF0000",
                                    "icon": "dumbbell",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<String> categoryIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UpdateCategoryRequest> dtoCaptor = ArgumentCaptor.forClass(UpdateCategoryRequest.class);

        verify(categoryService).updateCategory(categoryIdCaptor.capture(), dtoCaptor.capture());

        assertThat(categoryIdCaptor.getValue()).isEqualTo("0123456789ABC");

        UpdateCategoryRequest dto = dtoCaptor.getValue();
        assertThat(dto.id()).isEqualTo("0123456789ABC");
        assertThat(dto.name()).isEqualTo("some category req");
        assertThat(dto.color()).isEqualTo("#FF0000");
        assertThat(dto.icon()).isEqualTo("dumbbell");
        assertThat(dto.description()).isEqualTo("some description req");
    }

    @Test
    void updateCategory_shouldReturnBadRequest_whenCategoryIdIsInvalid() throws Exception {
        mockMvc.perform(put("/api/categories/0123456789AB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "0123456789ABC",
                                    "name": "some category req",
                                    "color": "#FF0000",
                                    "icon": "dumbbell",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    void updateCategory_shouldReturnBadRequest_whenRequestBodyIsInvalid() throws Exception {
        mockMvc.perform(put("/api/categories/0123456789ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "0123456789ABC",
                                    "name": "",
                                    "color": "#FF0000",
                                    "icon": "dumbbell",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    void deleteCategory_shouldReturnNoContent_whenCategoryWasDeleted() throws Exception {
        mockMvc.perform(delete("/api/categories/0123456789ABC"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).deleteCategory("0123456789ABC");
    }

    @Test
    void deleteCategory_shouldReturnBadRequest_whenCategoryIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/categories/0123456789AB"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    void deleteCategory_shouldReturnNotFound_whenServiceThrowsNotFoundException() throws Exception {
        doThrow(new NotFoundException("Category not found 0123456789ABC"))
                .when(categoryService).deleteCategory("0123456789ABC");

        mockMvc.perform(delete("/api/categories/0123456789ABC"))
                .andExpect(status().isNotFound());

        verify(categoryService).deleteCategory("0123456789ABC");
    }

    @Test
    void getAllCategories_shouldReturnOk() throws Exception {
        CategoriesResponse response = new CategoriesResponse(List.of(
                new CategoryResponse("cat-1", "Sport", "#123456", "sport-icon", "sport description", List.of()),
                new CategoryResponse("cat-2", "Work", "#234567", "work-icon", "work description", List.of(
                        new SubCategoryResponse("sub-cat-1", "Office Work", "office work description")
                ))
        ));

        when(categoryService.findAllCategories()).thenReturn(response);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(2))
                .andExpect(jsonPath("$.categories[0].id").value("cat-1"))
                .andExpect(jsonPath("$.categories[0].name").value("Sport"))
                .andExpect(jsonPath("$.categories[1].color").value("#234567"))
                .andExpect(jsonPath("$.categories[1].icon").value("work-icon"))
                .andExpect(jsonPath("$.categories[1].description").value("work description"));

        verify(categoryService).findAllCategories();
    }

    @Test
    void getAllCategories_shouldReturnOk_OnEmptyList() throws Exception {
        CategoriesResponse response = new CategoriesResponse(List.of());

        when(categoryService.findAllCategories()).thenReturn(response);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(0));

        verify(categoryService).findAllCategories();
    }

    @Test
    void postNewSubCategory_shouldReturnCreatedSubCategory() throws Exception {
        SubCategoryResponse response = new SubCategoryResponse(
                "sub-cat-1",
                "some subcategory",
                "some description"
        );

        when(categoryService.registerNewSubCategory(any(CreateSubCategoryRequest.class), eq("0123456789ABC"))).thenReturn(response);

        mockMvc.perform(post("/api/categories/0123456789ABC/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "some category req",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("sub-cat-1"))
                .andExpect(jsonPath("$.name").value("some subcategory"))
                .andExpect(jsonPath("$.description").value("some description"));

        verify(categoryService).registerNewSubCategory(any(CreateSubCategoryRequest.class), any(String.class));
    }

    @Test
    void postNewSubCategory_shouldPassValidatedRequestToService() throws Exception {
        mockMvc.perform(post("/api/categories/0123456789ABC/subcategories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "some category req",
                            "description": "some description req"
                        }
                        """));

        ArgumentCaptor<CreateSubCategoryRequest> dtoCaptor =
                ArgumentCaptor.forClass(CreateSubCategoryRequest.class);
        ArgumentCaptor<String> categoryIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(categoryService).registerNewSubCategory(dtoCaptor.capture(), categoryIdCaptor.capture());

        assertThat(categoryIdCaptor.getValue()).isEqualTo("0123456789ABC");
        CreateSubCategoryRequest dto = dtoCaptor.getValue();
        assertThat(dto.name()).isEqualTo("some category req");
        assertThat(dto.description()).isEqualTo("some description req");
    }

    @Test
    void postNewSubCategory_shouldReturnBadRequest_whenCategoryIdIsInvalid() throws Exception {
        mockMvc.perform(post("/api/categories/0123456789AB/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "some category req",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).registerNewSubCategory(any(CreateSubCategoryRequest.class), any());
    }

    @Test
    void postNewSubCategory_shouldReturnNotFound_whenServiceThrowsNotFoundException() throws Exception {
        NotFoundException exception = new NotFoundException("Category not found");

        when(categoryService.registerNewSubCategory(any(CreateSubCategoryRequest.class), any(String.class))).thenThrow(exception);

        mockMvc.perform(post("/api/categories/0123456789ABC/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "some category req",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(categoryService).registerNewSubCategory(any(CreateSubCategoryRequest.class), eq("0123456789ABC"));
    }

    @Test
    void postNewSubCategory_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/categories/0123456789ABC/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "some description req"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).registerNewSubCategory(any(CreateSubCategoryRequest.class), any());
    }

    @Test
    void getAllSubCategories_shouldReturnOk() throws Exception {
        SubCategoriesResponse response = new SubCategoriesResponse(List.of(
                new SubCategoryResponse("sub-cat-1", "Running", "running description"),
                new SubCategoryResponse("sub-cat-2", "Swimming", "swimming description")
        ));

        when(categoryService.findAllSubCategoriesByCategoryBusinessId(any())).thenReturn(response);

        mockMvc.perform(get("/api/categories/3216549873210/subcategories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.subcategories").isArray())
                .andExpect(jsonPath("$.subcategories.length()").value(2))
                .andExpect(jsonPath("$.subcategories[0].id").value("sub-cat-1"))
                .andExpect(jsonPath("$.subcategories[0].name").value("Running"))
                .andExpect(jsonPath("$.subcategories[1].description").value("swimming description"));

        verify(categoryService).findAllSubCategoriesByCategoryBusinessId("3216549873210");
    }

    @Test
    void getAllSubCategories_shouldReturnOk_OnEmptyList() throws Exception {
        SubCategoriesResponse response = new SubCategoriesResponse(List.of());

        when(categoryService.findAllSubCategoriesByCategoryBusinessId(any())).thenReturn(response);

        mockMvc.perform(get("/api/categories/3216549873210/subcategories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.subcategories").isArray())
                .andExpect(jsonPath("$.subcategories.length()").value(0));

        verify(categoryService).findAllSubCategoriesByCategoryBusinessId("3216549873210");
    }

    @Test
    void getAllSubCategories_shouldReturnBadRequest_whenCategoryIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/categories/invalid-id/subcategories"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).findAllSubCategoriesByCategoryBusinessId(any());
    }

    @Test
    void getAllSubCategories_shouldReturnNotFound_whenServiceThrowsNotFoundException() throws Exception {
        NotFoundException exception = new NotFoundException("Category not found");

        when(categoryService.findAllSubCategoriesByCategoryBusinessId("3216549873210")).thenThrow(exception);

        mockMvc.perform(get("/api/categories/3216549873210/subcategories"))
                .andExpect(status().isNotFound());

        verify(categoryService).findAllSubCategoriesByCategoryBusinessId("3216549873210");
    }
}