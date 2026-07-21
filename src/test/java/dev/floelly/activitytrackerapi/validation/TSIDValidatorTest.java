package dev.floelly.activitytrackerapi.validation;

import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TSIDValidatorTest {

    private final TSIDValidator validator = new TSIDValidator();

    private static Stream<Arguments> invalidTsidCases() {
        return Stream.of(
                Arguments.of("", "empty"),
                Arguments.of("123", "too short"),
                Arguments.of("123456789012", "length 12"),
                Arguments.of("12345678901234", "length 14"),
                Arguments.of("!!!!!!!!!!!!!", "invalid characters"),
                Arguments.of("abcdefghijklm", "not a valid TSID")
        );
    }

    @Test
    void shouldAcceptNullWhenNullable() {
        validator.initialize(annotation(true));

        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void shouldRejectNullWhenNotNullable() {
        validator.initialize(annotation(false));

        assertThat(validator.isValid(null, null)).isFalse();
    }

    @ParameterizedTest(name = "[{index}] {0} is invalid because: {1}")
    @MethodSource("invalidTsidCases")
    void shouldRejectInvalidTsids(String value, String reason) {
        validator.initialize(annotation(false));

        assertThat(validator.isValid(value, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0AXS751X00W7J",
            "0AXS751X00W7K"
    })
    void shouldAcceptValidTsid(String value) {
        validator.initialize(annotation(false));

        assertThat(validator.isValid(value, null)).isTrue();
    }

    private ValidTSID annotation(boolean nullable) {
        return new ValidTSID() {
            @Override
            public boolean nullable() {
                return nullable;
            }

            @Override
            public String message() {
                return "must be a valid 13-character TSID";
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ValidTSID.class;
            }
        };
    }
}