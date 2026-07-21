package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TagRepositoryIT extends MySQLContainerInitializer {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void findByBusinessIdIn_shouldReturnMatchingTags() {
        Tag tag1 = new Tag(
                null,
                "ABC1234567890",
                "Morning Run",
                "#123456",
                "some description",
                1
        );
        Tag tag2 = new Tag(
                null,
                "DEF1234567890",
                "Evening Walk",
                "#654321",
                "another description",
                2
        );
        Tag tag3 = new Tag(
                null,
                "GHI1234567890",
                "Cycling",
                "#abcdef",
                "third description",
                3
        );

        tagRepository.saveAll(List.of(tag1, tag2, tag3));

        Set<Tag> result = tagRepository.findByBusinessIdIn(List.of(
                "ABC1234567890",
                "GHI1234567890"
        ));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Tag::getBusinessId)
                .containsExactlyInAnyOrder("ABC1234567890", "GHI1234567890");
        assertThat(result)
                .extracting(Tag::getLabel)
                .containsExactlyInAnyOrder("Morning Run", "Cycling");
    }

    @Test
    void findByBusinessIdIn_shouldReturnEmptySetWhenNothingMatches() {
        Tag tag = new Tag(
                null,
                "ABC1234567890",
                "Morning Run",
                "#123456",
                "some description",
                1
        );

        tagRepository.save(tag);

        Set<Tag> result = tagRepository.findByBusinessIdIn(List.of(
                "XXX1234567890",
                "YYY1234567890"
        ));

        assertThat(result).isEmpty();
    }

    @Test
    void findByBusinessIdIn_shouldReturnEmptySetForEmptyInput() {
        Tag tag = new Tag(
                null,
                "ABC1234567890",
                "Morning Run",
                "#123456",
                "some description",
                1
        );

        tagRepository.save(tag);

        Set<Tag> result = tagRepository.findByBusinessIdIn(List.of());

        assertThat(result).isEmpty();
    }
}