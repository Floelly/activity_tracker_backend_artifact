package dev.floelly.activitytrackerapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubCategoryRequest(

        @NotBlank
        @Size(max = 50)
        String name,

        @Size(max = 255)
        String description
) {

}
