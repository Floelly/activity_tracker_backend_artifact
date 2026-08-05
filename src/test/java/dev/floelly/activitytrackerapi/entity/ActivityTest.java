package dev.floelly.activitytrackerapi.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityTest {

    @Test
    void newActivity_shouldHaveNonNullCollections() {
        Activity activity = new Activity();

        assertThat(activity.getCategoryAllocations()).isNotNull();
        assertThat(activity.getAttributes()).isNotNull();
        assertThat(activity.getTags()).isNotNull();

        assertThat(activity.getCategoryAllocations()).isEmpty();
        assertThat(activity.getAttributes()).isEmpty();
        assertThat(activity.getTags()).isEmpty();
    }
}