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

class CreateCategoryAllocationRequestValidationTest {

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
        var request = new CreateCategoryAllocationRequest(
                50,
                "0A1B2C3D4E5F6",
                "0Q5N3JEHCT9KH"
        );

        Set<ConstraintViolation<CreateCategoryAllocationRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100})
    void percentageWithinRange_isValid(int percentage) {
        var request = new CreateCategoryAllocationRequest(
                percentage,
                "0A1B2C3D4E5F6",
                "0Q5N3JEHCT9KH"
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, 101, 300})
    void percentageOutside1To100_hasViolationOnPercentage(int percentage) {
        var request = new CreateCategoryAllocationRequest(
                percentage,
                "0A1B2C3D4E5F6",
                "0Q5N3JEHCT9KH"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("percentage"));
    }

    @Test
    void nullCategoryId_hasViolationOnCategoryId() {
        var request = new CreateCategoryAllocationRequest(
                50,
                null,
                "0Q5N3JEHCT9KH"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("categoryId"));
    }

    @Test
    void invalidCategoryId_hasViolationOnCategoryId() {
        var request = new CreateCategoryAllocationRequest(
                50,
                "invalid-tsid",
                "0Q5N3JEHCT9KH"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("categoryId"));
    }

    @Test
    void nullSubCategoryId_isAllowed() {
        var request = new CreateCategoryAllocationRequest(
                50,
                "0A1B2C3D4E5F6",
                null
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidSubCategoryId_hasViolationOnSubCategoryId() {
        var request = new CreateCategoryAllocationRequest(
                50,
                "0A1B2C3D4E5F6",
                "invalid-tsid"
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("subCategoryId"));
    }
}