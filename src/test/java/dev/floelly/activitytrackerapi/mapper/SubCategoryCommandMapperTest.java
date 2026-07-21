package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateSubCategoryRequest;
import dev.floelly.activitytrackerapi.entity.SubCategory;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        SubCategoryCommandMapperImpl.class,
        StringNormalizer.class
})
class SubCategoryCommandMapperTest {

    @Autowired
    private SubCategoryCommandMapper mapper;

    @Test
    void toEntity_shouldMapAndNormalizeFields() {
        CreateSubCategoryRequest request = new CreateSubCategoryRequest(
                "Run     Flo\n\rRun   ",
                "subcat\n\n\n\n for\r\n\r    running        "
        );

        SubCategory result = mapper.toEntity(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getBusinessId()).isNull();
        assertThat(result.getName()).isEqualTo("Run Flo Run");
        assertThat(result.getDescription()).isEqualTo("subcat\n\nfor\n\nrunning");
        assertThat(result.getCategory()).isNull();
    }

    @Test
    void toEntity_returnsNullForNullRequest() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}