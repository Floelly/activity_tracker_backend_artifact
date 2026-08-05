package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Activity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActivityRepositoryIT extends MySQLContainerInitializer {

    @Autowired
    private ActivityRepository activityRepository;

    @Test
    void findByBusinessId_shouldReturnActivityWhenExists() {
        Activity activity = new Activity(
                null,
                "activity-123",
                "Morning Run",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                Set.of(),
                Set.of(),
                Set.of()
        );
        activity.setCreatedAt(Instant.now());

        Activity saved = activityRepository.save(activity);

        var result = activityRepository.findByBusinessId("activity-123");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getBusinessId()).isEqualTo("activity-123");
        assertThat(result.get().getUpdatedAt()).isNull();
    }

    @Test
    void findByBusinessId_shouldReturnEmptyWhenNotFound() {
        var result = activityRepository.findByBusinessId("does-not-exist");

        assertThat(result).isEmpty();
    }
}