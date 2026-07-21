package dev.floelly.activitytrackerapi.controller;

import dev.floelly.activitytrackerapi.dto.request.CreateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.CreateSubCategoryRequest;
import dev.floelly.activitytrackerapi.dto.request.UpdateCategoryRequest;
import dev.floelly.activitytrackerapi.dto.response.CategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoriesResponse;
import dev.floelly.activitytrackerapi.dto.response.SubCategoryResponse;
import dev.floelly.activitytrackerapi.service.CategoryService;
import dev.floelly.activitytrackerapi.validation.ValidTSID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> postNewCategory(@RequestBody @Valid CreateCategoryRequest categoryRequest) {
        CategoryResponse response = categoryService.registerNewCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable @ValidTSID String categoryId,
            @RequestBody @Valid UpdateCategoryRequest categoryRequest) {
        CategoryResponse response = categoryService.updateCategory(categoryId, categoryRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable @ValidTSID String categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CategoriesResponse> getAllCategories() {
        CategoriesResponse response = categoryService.findAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{categoryId}/subcategories")
    public ResponseEntity<SubCategoryResponse> postNewSubCategory(
            @PathVariable @ValidTSID String categoryId,
            @RequestBody @Valid CreateSubCategoryRequest subCategoryReqeust) {
        SubCategoryResponse response = categoryService.registerNewSubCategory(subCategoryReqeust, categoryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<SubCategoriesResponse> getAllSubCategories(@PathVariable @ValidTSID String categoryId) {
        SubCategoriesResponse response = categoryService.findAllSubCategoriesByCategoryBusinessId(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
