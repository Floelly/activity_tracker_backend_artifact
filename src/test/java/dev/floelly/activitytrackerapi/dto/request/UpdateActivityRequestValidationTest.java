package dev.floelly.activitytrackerapi.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateActivityRequestValidationTest {

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
    void validRequest_hasNoViolations() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789ABC",
                "Some title",
                "Some notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(
                        100,
                        "0123456789ABC",
                        null)
                )
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullNotes_hasNoViolations() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789ABC",
                "crazy Titel",
                null,
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of(new CreateCategoryAllocationRequest(
                        100,
                        "0123456789ABC",
                        null)
                )
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void emptyCategoryAllocations_hasNoViolations() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789ABC",
                "Some title",
                "Some notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullId_hasViolation() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                null,
                "Some title",
                "Some notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("id");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123456789123", "12312313212312"})
    void invalidId_hasViolation(String invalidId) {
        UpdateActivityRequest request = new UpdateActivityRequest(
                invalidId,
                "Some title",
                "Some notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("id");
    }

    @Test
    void titleTooLong_hasViolation() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789123",
                "A".repeat(121),
                "Some notes",
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("title");
    }

    @Test
    void notesTooLong_hasViolation() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789123",
                "Some title",
                "A".repeat(257),
                Instant.parse("2026-05-26T08:00:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("notes");
    }

    @Test
    void nullStartDate_hasViolation() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789123",
                "Some title",
                "Some notes",
                null,
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("startAt");
    }

    @Test
    void nullEndDate_hasViolation() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789123",
                "Some title",
                "Some notes",
                Instant.parse("2026-05-26T09:00:00Z"),
                null,
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("endAt");
    }

    @Test
    void startDateAfterEndDate_hasViolation() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789123",
                "Some title",
                "Some notes",
                Instant.parse("2026-05-26T09:02:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                List.of()
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessageTemplate)
                .anySatisfy(path -> assertThat(path).contains("endAt", "startAt"));
    }

    @Test
    void nullCategoryAllocation_hasViolation() {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "0123456789123",
                "Some title",
                "Some notes",
                Instant.parse("2026-05-26T09:01:00Z"),
                Instant.parse("2026-05-26T09:00:00Z"),
                null
        );

        Set<ConstraintViolation<UpdateActivityRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("categoryAllocations");
    }
}