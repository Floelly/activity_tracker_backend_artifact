package dev.floelly.activitytrackerapi.mapper;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.mapper.common.StringNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        TagCommandMapperImpl.class,
        StringNormalizer.class
})
class TagCommandMapperTest {

    @Autowired
    private TagCommandMapper mapper;

    @Test
    void toEntity_shouldMapAndNormalizeFields() {
        CreateTagRequest request = new CreateTagRequest(
                "  Morning   Run  ",
                "#123456",
                "  first line   \r\n  second\t\tline  \r\n\r\n\r\n third line  ",
                5
        );

        Tag result = mapper.toEntity(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getBusinessId()).isNull();
        assertThat(result.getLabel()).isEqualTo("Morning Run");
        assertThat(result.getColorCode()).isEqualTo("#123456");
        assertThat(result.getDescription()).isEqualTo("first line\nsecond line\n\nthird line");
        assertThat(result.getSortOrder()).isEqualTo(5);
    }

    @Test
    void toEntity_shouldReturnNullForNullRequest() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}