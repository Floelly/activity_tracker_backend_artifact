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


class UpdateCategoryRequestValidationTest {

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
        var request = new UpdateCategoryRequest(
                "0Q123123123Q0",
                "Work",
                "#ff0000",
                "briefcase",
                "Work related stuff"
        );

        Set<ConstraintViolation<UpdateCategoryRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123456789123", "12312313212312"})
    void invalidId_hasViolation(String invalidId) {
        var request = new UpdateCategoryRequest(
                invalidId,
                "Valid name",
                "#ff0000",
                "icon",
                "desc"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("id"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   \n"})
    void blankName_hasViolation(String invalidName) {
        var request = new UpdateCategoryRequest(
                "0Q123123123Q1",
                invalidName,
                "#ff0000",
                "icon",
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

        var request = new UpdateCategoryRequest(
                "0Q123123123Q2",
                longName,
                "#ff0000",
                "icon",
                "desc"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"#ffffff", "#000000", "#12abEF"})
    void validColorValues_haveNoColorViolations(String color) {
        var request = new UpdateCategoryRequest(
                "0Q123123123Q3",
                "Work",
                color,
                "icon",
                "desc"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .noneSatisfy(path -> assertThat(path).hasToString("color"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ff0000", "#ff", "#gg0000", "#1234567"})
    void invalidColorValues_haveColorViolation(String color) {
        var request = new UpdateCategoryRequest(
                "0Q123123123Q4",
                "Work",
                color,
                "icon",
                "desc"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("color"));
    }

    @Test
    void iconTooLong_hasViolation() {
        String longIcon = "x".repeat(31);

        var request = new UpdateCategoryRequest(
                "0Q123123123Q5",
                "Work",
                "#ff0000",
                longIcon,
                "desc"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("icon"));
    }

    @Test
    void descriptionTooLong_hasViolation() {
        String longDescription = "x".repeat(256);

        var request = new UpdateCategoryRequest(
                "0Q123123123Q6",
                "Work",
                "#ff0000",
                "icon",
                longDescription
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("description"));
    }
}