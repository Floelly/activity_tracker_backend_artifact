package dev.floelly.activitytrackerapi.validation;

import dev.floelly.activitytrackerapi.dto.request.CreateActivityRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TimeRangeValidatorTest {

    private final TimeRangeValidator validator = new TimeRangeValidator();

    private static Stream<Arguments> cases() {
        Instant now = Instant.now();

        return Stream.of(
                Arguments.of(now, now.plusSeconds(60), true),
                Arguments.of(now, now, false),
                Arguments.of(now.plusSeconds(60), now, false),
                Arguments.of(null, now, true),
                Arguments.of(now, null, true),
                Arguments.of(null, null, true),
                Arguments.of(null, now.plusSeconds(60), true)
        );
    }

    @ParameterizedTest(name = "[{index}] start={0}, end={1} -> valid={2}")
    @MethodSource("cases")
    void shouldValidateTimeRange(Instant startAt, Instant endAt, boolean expected) {
        CreateActivityRequest request = new CreateActivityRequest(
                null, null, startAt, endAt, null, null, null
        );

        boolean result = validator.isValid(request, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void nullShouldBeValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}