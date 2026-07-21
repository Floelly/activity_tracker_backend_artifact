package dev.floelly.activitytrackerapi.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTagRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldPassValidation_whenRequestIsValid() {
        CreateTagRequest request = new CreateTagRequest(
                "Health",
                "#22c55e",
                "Health related tag",
                0
        );

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidation_whenLabelIsBlank() {
        CreateTagRequest request = new CreateTagRequest(
                "",
                "#22c55e",
                "Health related tag",
                0
        );

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("label");
    }

    @Test
    void shouldFailValidation_whenLabelIsTooLong() {
        CreateTagRequest request = new CreateTagRequest(
                "a".repeat(31),
                "#22c55e",
                "Health related tag",
                0
        );

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("label");
    }

    @Test
    void shouldFailValidation_whenDescriptionIsTooLong() {
        CreateTagRequest request = new CreateTagRequest(
                "Health",
                "#22c55e",
                "a".repeat(256),
                0
        );

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }

    @Test
    void shouldFailValidation_whenSortOrderIsNegative() {
        CreateTagRequest request = new CreateTagRequest(
                "Health",
                "#22c55e",
                "Health related tag",
                -1
        );

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("sortOrder");
    }

    @Test
    void shouldFailValidation_whenColorIsInvalid() {
        CreateTagRequest request = new CreateTagRequest(
                "Health",
                "not-a-color",
                "Health related tag",
                0
        );

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("color");
    }
}