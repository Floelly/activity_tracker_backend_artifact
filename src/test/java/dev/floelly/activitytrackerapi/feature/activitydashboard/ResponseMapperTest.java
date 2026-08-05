package dev.floelly.activitytrackerapi.feature.activitydashboard;

import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.ActivitiesDashboardFilterDTO;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.CategoryResponse;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.Response;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.SummaryResponse;
import dev.floelly.activitytrackerapi.feature.activitydashboard.dto.TimeSeriesResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.data.Offset.offset;

class ResponseMapperTest {

    private final ResponseMapper mapper = new ResponseMapper();

    @Test
    void buildResponse_shouldReturnResponseWithFilterSummaryAndTimeSeries() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        Category category2 = createCategory("cat-2", "Work", "#654321", "work");
        List<Category> categories = List.of(category1, category2);

        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z")
        );
        List<PeriodKey> orderedPeriods = List.of(period);

        Map<PeriodKey, Map<String, Long>> aggregated = Map.of(
                period, Map.of("cat-1", 3600L, "cat-2", 1800L)
        );

        Response result = mapper.buildResponse(filter, categories, aggregated, orderedPeriods);

        assertThat(result.filters()).isEqualTo(filter);
        assertThat(result.summary()).isNotNull();
        assertThat(result.summary().totalMinutes()).isEqualTo(90);
        assertThat(result.summary().categories()).hasSize(2);
        assertThat(result.timeSeries()).isNotNull();
        assertThat(result.timeSeries().granularity()).isEqualTo(TimeGranularity.DAY);
        assertThat(result.timeSeries().periods()).hasSize(1);
    }

    @Test
    void buildResponse_shouldSortCategoriesInTimeSeriesLikeSummary() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        // Category 2 has more seconds, so it should appear first in summary (sorted by minutes desc)
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        Category category2 = createCategory("cat-2", "Work", "#654321", "work");
        List<Category> categories = List.of(category1, category2);

        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z")
        );
        List<PeriodKey> orderedPeriods = List.of(period);

        // cat-2 has 7200 seconds (120 min), cat-1 has 3600 seconds (60 min)
        Map<PeriodKey, Map<String, Long>> aggregated = Map.of(
                period, Map.of("cat-1", 3600L, "cat-2", 7200L)
        );

        Response result = mapper.buildResponse(filter, categories, aggregated, orderedPeriods);

        // Summary should be sorted by minutes descending: cat-2 first, then cat-1
        assertThat(result.summary().categories())
                .extracting(CategoryResponse::id)
                .containsExactly("cat-2", "cat-1");

        // TimeSeries periods should have categories in same order as summary
        assertThat(result.timeSeries().periods()).hasSize(1);
        assertThat(result.timeSeries().periods().getFirst().categories())
                .extracting(CategoryResponse::id)
                .containsExactly("cat-2", "cat-1");
    }

    @Test
    void buildResponse_shouldHandleEmptyData() {
        ActivitiesDashboardFilterDTO filter = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z"),
                TimeGranularity.DAY,
                null
        );

        List<Category> categories = List.of();

        PeriodKey period = new PeriodKey(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-02T00:00:00Z")
        );
        List<PeriodKey> orderedPeriods = List.of(period);

        Map<PeriodKey, Map<String, Long>> aggregated = Map.of(period, Map.of());

        Response result = mapper.buildResponse(filter, categories, aggregated, orderedPeriods);

        assertThat(result.summary().totalMinutes()).isZero();
        assertThat(result.summary().averageMinutesPerPeriod()).isZero();
        assertThat(result.summary().categories()).isEmpty();
        assertThat(result.timeSeries().periods()).hasSize(1);
        assertThat(result.timeSeries().periods().getFirst().totalMinutes()).isZero();
        assertThat(result.timeSeries().periods().getFirst().categories()).isEmpty();
    }

    @Test
    void buildSummary_shouldCalculateTotalAndAverageMinutes() {
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        Category category2 = createCategory("cat-2", "Work", "#654321", "work");
        List<Category> categories = List.of(category1, category2);

        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of(
                createPeriodKey("2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z"),
                Map.of("cat-1", 3600L, "cat-2", 1800L),
                createPeriodKey("2023-01-02T00:00:00Z", "2023-01-03T00:00:00Z"),
                Map.of("cat-1", 1800L, "cat-2", 3600L)
        );

        SummaryResponse summary = mapper.buildSummary(categories, secondsByPeriodAndCategory, 2);

        assertThat(summary.totalMinutes()).isEqualTo(180);
        assertThat(summary.averageMinutesPerPeriod()).isEqualTo(90);
        assertThat(summary.categories()).hasSize(2);
    }

    @Test
    void buildSummary_shouldSortCategoriesByMinutesDescending() {
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        Category category2 = createCategory("cat-2", "Work", "#654321", "work");
        List<Category> categories = List.of(category1, category2);

        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of(
                createPeriodKey("2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z"),
                Map.of("cat-1", 7200L, "cat-2", 1800L)
        );

        SummaryResponse summary = mapper.buildSummary(categories, secondsByPeriodAndCategory, 1);

        assertThat(summary.categories()).extracting(CategoryResponse::name)
                .containsExactly("Sport", "Work");
    }

    @Test
    void buildSummary_shouldCalculateCategoryPercentage() {
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        Category category2 = createCategory("cat-2", "Work", "#654321", "work");
        List<Category> categories = List.of(category1, category2);

        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of(
                createPeriodKey("2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z"),
                Map.of("cat-1", 3000L, "cat-2", 1000L)
        );

        SummaryResponse summary = mapper.buildSummary(categories, secondsByPeriodAndCategory, 1);

        CategoryResponse sportCategory = summary.categories().getFirst();
        assertThat(sportCategory.minutes()).isEqualTo(50);
        assertThat(sportCategory.percentage()).isCloseTo(0.75f, offset(0.01f));
    }

    @Test
    void buildSummary_shouldHandleZeroTotalSeconds() {
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        List<Category> categories = List.of(category1);

        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of(
                createPeriodKey("2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z"),
                Map.of()
        );

        SummaryResponse summary = mapper.buildSummary(categories, secondsByPeriodAndCategory, 1);

        assertThat(summary.totalMinutes()).isZero();
        assertThat(summary.averageMinutesPerPeriod()).isZero();
        assertThat(summary.categories().getFirst().percentage()).isZero();
    }

    @Test
    void buildSummary_shouldHandleZeroPeriodCount() {
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        List<Category> categories = List.of(category1);

        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of();

        SummaryResponse summary = mapper.buildSummary(categories, secondsByPeriodAndCategory, 0);

        assertThat(summary.averageMinutesPerPeriod()).isZero();
    }

    @Test
    void buildTimeSeries_shouldCreatePeriodsWithMinutesAndCategories() {
        TimeGranularity granularity = TimeGranularity.DAY;
        List<PeriodKey> orderedPeriods = List.of(
                createPeriodKey("2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z"),
                createPeriodKey("2023-01-02T00:00:00Z", "2023-01-03T00:00:00Z")
        );
        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of(
                orderedPeriods.getFirst(),
                Map.of("cat-1", 3600L),
                orderedPeriods.get(1),
                Map.of("cat-1", 1800L)
        );
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        List<Category> orderedCategories = List.of(category1);

        TimeSeriesResponse timeSeries = mapper.buildTimeSeries(
                granularity, orderedPeriods, secondsByPeriodAndCategory, orderedCategories);

        assertThat(timeSeries.granularity()).isEqualTo(TimeGranularity.DAY);
        assertThat(timeSeries.periods()).hasSize(2);
        assertThat(timeSeries.periods().getFirst().totalMinutes()).isEqualTo(60);
        assertThat(timeSeries.periods().get(1).totalMinutes()).isEqualTo(30);
    }

    @Test
    void buildTimeSeries_shouldHandleEmptyCategoryMapForPeriod() {
        TimeGranularity granularity = TimeGranularity.DAY;
        List<PeriodKey> orderedPeriods = List.of(
                createPeriodKey("2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z")
        );
        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of();
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        List<Category> orderedCategories = List.of(category1);

        TimeSeriesResponse timeSeries = mapper.buildTimeSeries(
                granularity, orderedPeriods, secondsByPeriodAndCategory, orderedCategories);

        assertThat(timeSeries.periods().getFirst().totalMinutes()).isZero();
        assertThat(timeSeries.periods().getFirst().categories().getFirst().minutes()).isZero();
    }

    @Test
    void buildTimeSeries_shouldCalculateCategoryPercentagePerPeriod() {
        TimeGranularity granularity = TimeGranularity.DAY;
        List<PeriodKey> orderedPeriods = List.of(
                createPeriodKey("2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z")
        );
        Map<PeriodKey, Map<String, Long>> secondsByPeriodAndCategory = Map.of(
                orderedPeriods.getFirst(),
                Map.of("cat-1", 3000L, "cat-2", 1000L)
        );
        Category category1 = createCategory("cat-1", "Sport", "#123456", "fitness");
        Category category2 = createCategory("cat-2", "Work", "#654321", "work");
        List<Category> orderedCategories = List.of(category1, category2);

        TimeSeriesResponse timeSeries = mapper.buildTimeSeries(
                granularity, orderedPeriods, secondsByPeriodAndCategory, orderedCategories);

        List<CategoryResponse> categories = timeSeries.periods().getFirst().categories();
        assertThat(categories.getFirst().percentage()).isCloseTo(0.75f, offset(0.01f));
        assertThat(categories.get(1).percentage()).isCloseTo(0.25f, offset(0.01f));
    }

    @Test
    void toDashboardCategory_shouldMapCategoryToDTO() {
        Category category = createCategory("cat-1", "Sport", "#123456", "fitness");

        CategoryResponse dto = mapper.toDashboardCategory(category, 100, 0.5f);

        assertThat(dto.id()).isEqualTo("cat-1");
        assertThat(dto.name()).isEqualTo("Sport");
        assertThat(dto.minutes()).isEqualTo(100);
        assertThat(dto.percentage()).isEqualTo(0.5f);
        assertThat(dto.color()).isEqualTo("#123456");
        assertThat(dto.icon()).isEqualTo("fitness");
    }

    private Category createCategory(String businessId, String name, String colorCode, String iconName) {
        Category category = new Category();
        category.setBusinessId(businessId);
        category.setName(name);
        category.setColorCode(colorCode);
        category.setIconName(iconName);
        return category;
    }

    private PeriodKey createPeriodKey(String from, String to) {
        return new PeriodKey(
                Instant.parse(from),
                Instant.parse(to)
        );
    }
}