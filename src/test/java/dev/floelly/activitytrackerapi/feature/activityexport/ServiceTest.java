package dev.floelly.activitytrackerapi.feature.activityexport;

import dev.floelly.activitytrackerapi.entity.Activity;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.entity.CategoryAllocation;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.exception.BadRequestException;
import dev.floelly.activitytrackerapi.repository.ActivityRepository;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExportService exportService;

    @Captor
    private ArgumentCaptor<Specification<Activity>> specCaptor;

    @Test
    void exportCsv_shouldReturnCsvWithHeaderOnly_whenNoActivities() {
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        String result = exportService.exportCsv(null, null, null);

        assertThat(result).isEqualTo("businessId,title,startAt,endAt,durationMinutes,categories,tags,notes\n");
    }

    @Test
    void exportCsv_shouldReturnCsvWithActivityRow_whenActivitiesExist() {
        Category category = new Category();
        category.setBusinessId("cat-1");

        CategoryAllocation allocation = new CategoryAllocation();
        allocation.setCategory(category);

        Tag tag = new Tag();
        tag.setBusinessId("tag-1");

        Activity activity = new Activity();
        activity.setBusinessId("act-1");
        activity.setTitle("Test Activity");
        activity.setNotes("Some notes");
        activity.setStartAt(Instant.parse("2024-01-01T10:00:00Z"));
        activity.setEndAt(Instant.parse("2024-01-01T11:30:00Z"));
        activity.setCategoryAllocations(Set.of(allocation));
        activity.setTags(Set.of(tag));

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity));

        String result = exportService.exportCsv(null, null, null);

        String[] lines = result.split("\n");
        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("businessId,title,startAt,endAt,durationMinutes,categories,tags,notes");
        assertThat(lines[1]).isEqualTo("act-1,Test Activity,2024-01-01T10:00:00Z,2024-01-01T11:30:00Z,90,cat-1,tag-1,Some notes");
    }

    @Test
    void exportCsv_shouldThrowBadRequest_whenCategoryNotFound() {
        when(categoryRepository.findByBusinessId("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportCsv("nonexistent", null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Category not found");

        verifyNoInteractions(activityRepository);
    }

    @Test
    void exportCsv_shouldThrowBadRequest_whenStartDateAfterEndDate() {
        assertThatThrownBy(() -> exportService.exportCsv(null, "2024-01-31T00:00:00Z", "2024-01-01T00:00:00Z"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("startDate must be before endDate");

        verifyNoInteractions(activityRepository);
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void exportCsv_shouldEscapeCommasQuotesAndNewlines() {
        Activity activity = new Activity();
        activity.setBusinessId("act-1");
        activity.setTitle("Title with \"quote\", and comma");
        activity.setNotes("Line 1\nLine 2, with comma");
        activity.setStartAt(Instant.parse("2024-01-01T10:00:00Z"));
        activity.setEndAt(Instant.parse("2024-01-01T10:45:00Z"));
        activity.setCategoryAllocations(Set.of());
        activity.setTags(Set.of());

        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(activity));

        String result = exportService.exportCsv(null, null, null);

        String[] lines = result.split("\n", -1);
        assertThat(lines).hasSize(3);
        assertThat(lines[1]).isEqualTo("act-1,\"Title with \"\"quote\"\", and comma\",2024-01-01T10:00:00Z,2024-01-01T10:45:00Z,45,,,\"Line 1\nLine 2, with comma\"");
    }

    @Test
    void exportCsv_shouldFilterByCategory_whenCategoryIdProvided() {
        Category category = new Category();
        category.setBusinessId("cat-1");
        when(categoryRepository.findByBusinessId("cat-1")).thenReturn(Optional.of(category));
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        exportService.exportCsv("cat-1", null, null);

        verify(activityRepository).findAll(specCaptor.capture());
        Specification<Activity> spec = specCaptor.getValue();
        assertThat(spec).isNotNull();
    }

    @Test
    void exportCsv_shouldFilterByDateRange_whenDatesProvided() {
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        exportService.exportCsv(null, "2024-01-01T00:00:00Z", "2024-01-31T23:59:59Z");

        verify(activityRepository).findAll(specCaptor.capture());
        Specification<Activity> spec = specCaptor.getValue();
        assertThat(spec).isNotNull();
    }
}