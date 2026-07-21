package dev.floelly.activitytrackerapi.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityFilterDTOValidationTest {

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
    void shouldHaveNoViolations_whenFilterIsValid() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T08:00:00Z"),
                Instant.parse("2026-05-25T10:00:00Z"),
                List.of("0123456789abc")
        );

        var violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveViolation_whenFromStartAtIsAfterToStartAt() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T10:00:00Z"),
                Instant.parse("2026-05-25T08:00:00Z"),
                List.of()
        );

        var violations = validator.validate(filter);

        assertThat(violations)
                .extracting(v -> v.getMessage())
                .contains("Bad time range: fromStartAt must be before or equal to toStartAt");
    }

    @Test
    void shouldHaveNoViolations_whenOnlyFromStartAtIsPresent() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T08:00:00Z"),
                null,
                List.of()
        );

        var violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveNoViolations_whenOnlyToStartAtIsPresent() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                Instant.parse("2026-05-25T10:00:00Z"),
                List.of()
        );

        var violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveNoViolations_whenBothAreNull() {
        ActivityFilterDTO filter = new ActivityFilterDTO(null, null, List.of());

        var violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveNoViolations_whenCategoryIsNull() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T08:00:00Z"),
                Instant.parse("2026-05-25T10:00:00Z"),
                null
        );

        var violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveNoViolations_whenCategoryListIsEmpty() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T08:00:00Z"),
                Instant.parse("2026-05-25T10:00:00Z"),
                List.of()
        );

        var violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveNoViolations_whenAllCategoryBusinessIdsAreValid() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of("0123456789abc", "abcdef1234567")
        );

        var violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveViolation_whenCategoryContainsInvalidTSID() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of("invalid-tsid")
        );

        var violations = validator.validate(filter);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldHaveViolation_whenCategoryContainsMultipleInvalidTSIDs() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                null,
                null,
                List.of("short", "also-invalid")
        );

        var violations = validator.validate(filter);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldHaveViolation_whenOneCategoryBusinessIdIsInvalid() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T08:00:00Z"),
                Instant.parse("2026-05-25T10:00:00Z"),
                List.of("0123456789abc", "invalid-tsid")
        );

        var violations = validator.validate(filter);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldHaveViolations_whenTimeRangeIsInvalidAndCategoryContainsInvalidTSID() {
        ActivityFilterDTO filter = new ActivityFilterDTO(
                Instant.parse("2026-05-25T10:00:00Z"),
                Instant.parse("2026-05-25T08:00:00Z"),
                List.of("invalid-tsid")
        );

        var violations = validator.validate(filter);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations)
                .extracting(v -> v.getMessage())
                .contains("Bad time range: fromStartAt must be before or equal to toStartAt");
    }
}