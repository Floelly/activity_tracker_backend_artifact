package dev.floelly.activitytrackerapi.feature.activitydashboard.dto;

import dev.floelly.activitytrackerapi.feature.activitydashboard.TimeGranularity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ActivitiesDashboardFilterDTOValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void shouldHaveNoViolations_whenRequestIsValid() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                List.of("0123456789ABC")
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveViolation_whenFromIsNull() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                null,
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                List.of("0123456789ABC")
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("from");
    }

    @Test
    void shouldHaveViolation_whenToIsNull() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                null,
                TimeGranularity.WEEK,
                List.of("0123456789ABC")
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("to");
    }

    @Test
    void shouldHaveViolation_whenGranularityIsNull() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                null,
                List.of("0123456789ABC")
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("granularity");
    }

    @Test
    void shouldHaveViolation_whenFromIsAfterTo() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-08T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z"),
                TimeGranularity.WEEK,
                List.of("0123456789ABC")
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Bad time range: 'from' must be before or equal to 'to'");
    }

    @Test
    void shouldHaveViolations_whenFromEqualsTo() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z"),
                TimeGranularity.DAY,
                List.of("0123456789ABC")
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Bad time range: 'from' must be before or equal to 'to'");
    }

    @Test
    void shouldHaveNoViolations_whenCategoryIsNull() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                null
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveNoViolations_whenCategoryIsEmpty() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                List.of()
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveViolation_whenCategoryContainsInvalidTsid() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                List.of("invalid-id")
        );

        Set<ConstraintViolation<ActivitiesDashboardFilterDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .anyMatch(path -> path.contains("category"));
    }

    @Test
    void shouldReturnTrue_whenHasCategoryFilterAndCategoryContainsEntries() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                List.of("0123456789ABC")
        );

        assertThat(request.hasCategoryFilter()).isTrue();
    }

    @Test
    void shouldReturnFalse_whenHasCategoryFilterAndCategoryIsNull() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                null
        );

        assertThat(request.hasCategoryFilter()).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHasCategoryFilterAndCategoryIsEmpty() {
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                List.of()
        );

        assertThat(request.hasCategoryFilter()).isFalse();
    }

    @Test
    void shouldReturnCategoryIds() {
        List<String> categoryIds = new ArrayList<>(List.of("0123456789ABC", "0123456789ABD"));
        ActivitiesDashboardFilterDTO request = new ActivitiesDashboardFilterDTO(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                TimeGranularity.WEEK,
                categoryIds
        );

        assertThat(request.categoryIds()).isEqualTo(categoryIds);
    }
}