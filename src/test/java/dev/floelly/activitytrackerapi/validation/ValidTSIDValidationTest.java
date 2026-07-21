package dev.floelly.activitytrackerapi.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidTSIDValidationTest {

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
    @DisplayName("@ValidTSID(nullable = true) allows null")
    void shouldAllowNullWhenNullableIsTrue() {
        Set<ConstraintViolation<NullableHolder>> violations =
                validator.validate(new NullableHolder(null));

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("@ValidTSID(nullable = false) rejects null")
    void shouldRejectNullWhenDefaultAnnotation() {
        Set<ConstraintViolation<NotNullableHolder>> violations =
                validator.validate(new NotNullableHolder(null));

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("value");
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "0AXS751X00W7J",
            "0AXS751X00W7K"
    })
    void shouldAcceptValidTsidStrings(String value) {
        Set<ConstraintViolation<NotNullableHolder>> violations =
                validator.validate(new NotNullableHolder(value));

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "123",
            "123456789012",
            "12345678901234",
    })
    void shouldRejectInvalidTsidStrings(String value) {
        Set<ConstraintViolation<NotNullableHolder>> violations =
                validator.validate(new NotNullableHolder(value));

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("value");
    }

    private record NullableHolder(@ValidTSID(nullable = true) String value) {
    }

    private record NotNullableHolder(@ValidTSID String value) {
    }
}