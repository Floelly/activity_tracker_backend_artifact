package dev.floelly.activitytrackerapi.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ColorValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    private static Stream<Arguments> validColorCandidates() {
        return Stream.of(
                Arguments.of("#abc", "3 digit hash code (short form of 6 digit hash code)"),
                Arguments.of("#abcd", "4 digit hash code (short form of 8 digit hash code)"),
                Arguments.of("#22c55e", "6 digit hash code"),
                Arguments.of("#22c55cff", "8 digit hash code"),
                Arguments.of(null, "can be null")
        );
    }

    private static Stream<Arguments> invalidColorCandidates() {
        return Stream.of(
                Arguments.of("", "empty"),
                Arguments.of("22c55e", "missingHash"),
                Arguments.of("#22c55z", "invalid characters"),
                Arguments.of("#12345", "length 5 after hashtag")
        );
    }

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @ParameterizedTest(name = "[{index}] {0} is valid because: {1}")
    @MethodSource("validColorCandidates")
    void shouldPass_forValidHexStrings(String value, String expectedMessage) {
        Set<ConstraintViolation<ColorHolder>> violations = validator.validate(new ColorHolder(value));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] {0} is invalid because: {1}")
    @MethodSource("invalidColorCandidates")
    void shouldFail_forInalidHexStrings(String value, String expectedMessage) {
        Set<ConstraintViolation<ColorHolder>> violations = validator.validate(new ColorHolder(value));
        assertThat(violations).hasSize(1);
    }

    private record ColorHolder(@Color String color) {
    }
}