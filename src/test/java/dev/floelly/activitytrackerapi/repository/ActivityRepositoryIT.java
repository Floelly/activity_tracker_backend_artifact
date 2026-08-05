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
        Activity activity = new Activity();
        activity.setBusinessId("activity-123");
        activity.setTitle("Morning Run");
        activity.setNotes("Some notes");
        activity.setStartAt(Instant.parse("2024-01-01T10:00:00Z"));
        activity.setEndAt(Instant.parse("2024-01-01T11:00:00Z"));
        activity.setCategoryAllocations(Set.of());
        activity.setAttributes(Set.of());
        activity.setTags(Set.of());
        activity.setCreatedAt(Instant.now());
        activity.setUpdatedAt(null);

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
