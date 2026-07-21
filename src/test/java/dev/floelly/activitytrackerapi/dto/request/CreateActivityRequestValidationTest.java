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

class CreateActivityRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    @Test
    void validRequest_hasNoViolations() {
        var categoryAllocation = new CreateCategoryAllocationRequest(
                50,
                "0A1B2C3D4E5F6",
                null
        );

        var attribute = new CreateActivityAttributeRequest(
                "priority",
                "high",
                Boolean.TRUE,
                0
        );

        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(categoryAllocation),
                List.of(attribute),
                List.of("1A2B3C4D5E6F7")
        );

        Set<ConstraintViolation<CreateActivityRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void validRequestWithEmptyLists_hasNoViolations() {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(),
                List.of(),
                List.of()
        );

        Set<ConstraintViolation<CreateActivityRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void titleTooLong_hasViolationOnTitle() {
        String longTitle = "x".repeat(121);

        var request = new CreateActivityRequest(
                longTitle,
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                List.of("1A2B3C4D5E6F7")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("title"));
    }

    @Test
    void notesTooLong_hasViolationOnNotes() {
        String longNotes = "x".repeat(256);

        var request = new CreateActivityRequest(
                "My Activity",
                longNotes,
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                List.of("1A2B3C4D5E6F7")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("notes"));
    }

    @Test
    void nullStartAt_hasViolationOnStartAt() {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                null,
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                List.of("1A2B3C4D5E6F7")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("startAt"));
    }

    @Test
    void nullEndAt_hasViolationOnEndAt() {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                null,
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                List.of("1A2B3C4D5E6F7")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("endAt"));
    }

    @ParameterizedTest(name = "endAt before startAt: {0} before 2024-01-01T10:00:00Z")
    @ValueSource(strings = {"2024-01-01T10:00:00Z", "2024-01-01T09:59:00Z"})
    void endAtBeforeStartAt_hasViolation(String endAtIsoString) {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse(endAtIsoString),
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                List.of("1A2B3C4D5E6F7")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessageTemplate)
                .anySatisfy(path -> assertThat(path).contains("endAt", "startAt"));
    }

    @Test
    void nullCategoryAllocations_hasViolationOnCategoryAllocations() {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                null,
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                List.of("1A2B3C4D5E6F7")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("categoryAllocations"));
    }

    @Test
    void nullCustomValues_hasViolationOnCustomValues() {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                null,
                List.of("1A2B3C4D5E6F7")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("customValues"));
    }

    @Test
    void nullTagIds_hasViolationOnTagIds() {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                null
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("tagIds"));
    }

    @Test
    void invalidTagId_hasViolationOnTagIdsElement() {
        var request = new CreateActivityRequest(
                "My Activity",
                "Some notes",
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                List.of(
                        new CreateCategoryAllocationRequest(
                                50,
                                "0A1B2C3D4E5F6",
                                null
                        )
                ),
                List.of(
                        new CreateActivityAttributeRequest(
                                "priority",
                                "high",
                                Boolean.TRUE,
                                0
                        )
                ),
                List.of("invalid-tsid")
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path.toString()).startsWith("tagIds"));
    }
}