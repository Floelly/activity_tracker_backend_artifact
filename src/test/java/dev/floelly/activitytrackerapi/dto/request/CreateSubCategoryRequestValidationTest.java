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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSubCategoryRequestValidationTest {

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
        var request = new CreateSubCategoryRequest(
                "Work",
                "Work related stuff"
        );

        Set<ConstraintViolation<CreateSubCategoryRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   \n"})
    void blankName_hasViolation(String invalidName) {
        var request = new CreateSubCategoryRequest(
                invalidName,
                "desc"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("name"));
    }

    @Test
    void nameTooLong_hasViolation() {
        String longName = "x".repeat(51);

        var request = new CreateSubCategoryRequest(
                longName,
                "desc"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("name"));
    }

    @Test
    void descriptionTooLong_hasViolation() {
        String longDescription = "x".repeat(256);

        var request = new CreateSubCategoryRequest(
                "Work",
                longDescription
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("description"));
    }
}