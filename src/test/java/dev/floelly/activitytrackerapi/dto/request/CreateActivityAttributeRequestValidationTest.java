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
import static org.assertj.core.api.Assertions.fail;

class CreateActivityAttributeRequestValidationTest {

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
        var request = new CreateActivityAttributeRequest(
                "priority",
                "high",
                Boolean.TRUE,
                0
        );

        Set<ConstraintViolation<CreateActivityAttributeRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   \n\t"})
    void blankKey_hasViolationOnKey(String invalidKey) {
        var request = new CreateActivityAttributeRequest(
                invalidKey,
                "value",
                Boolean.TRUE,
                0
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("key"));
    }

    @Test
    void keyTooLong_hasViolationOnKey() {
        String longKey = "x".repeat(51);

        var request = new CreateActivityAttributeRequest(
                longKey,
                "value",
                Boolean.TRUE,
                0
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("key"));
    }

    @Test
    void nullValue_isAllowed() {
        var request = new CreateActivityAttributeRequest(
                "priority",
                null,
                Boolean.TRUE,
                0
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void valueTooLong_hasViolationOnValue() {
        String longValue = "x".repeat(256);

        var request = new CreateActivityAttributeRequest(
                "priority",
                longValue,
                Boolean.TRUE,
                0
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("value"));
    }

    @Test
    void nullShowInOverview_hasViolationOnShowInOverview() {
        var request = new CreateActivityAttributeRequest(
                "priority",
                "value",
                null,
                0
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("showInOverview"));
    }

    @Test
    void zeroSortOrder_isValid() {
        var request = new CreateActivityAttributeRequest(
                "priority",
                "value",
                Boolean.TRUE,
                0
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void negativeSortOrder_hasViolationOnSortOrder() {
        var request = new CreateActivityAttributeRequest(
                "priority",
                "value",
                Boolean.TRUE,
                -1
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .anySatisfy(path -> assertThat(path).hasToString("sortOrder"));
    }

    @Test
    void shouldFail() {
        fail("test failure");
    }
}