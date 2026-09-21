package io.backbonehq.kit.common.impl.lang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("StringInvariants.allEqualIgnoreCase")
class StringInvariantsTest
{
    // Test data provider
    static Stream<Arguments> provideInvalidStringTriplets()
    {
        return Stream.of(
            // Null values
            Arguments.of(null, "user@example.com", "user@example.com"),
            Arguments.of("user@example.com", null, "user@example.com"),
            Arguments.of("user@example.com", "user@example.com", null),
            Arguments.of(null, null, null),

            // Blank values
            Arguments.of("", "user@example.com", "user@example.com"),
            Arguments.of("user@example.com", " ", "user@example.com"),

            // Partial mismatches
            Arguments.of("user@example.com", "user@example.com", "hacker@example.com"),
            Arguments.of("old@example.com", "new@example.com", "new@example.com")
        );
    }

    @Test
    @DisplayName("Returns true when all strings match (case-insensitive)")
    void allEqualIgnoreCase_AllMatch_ReturnsTrue()
    {
        assertTrue(StringInvariants.allEqualIgnoreCase("user@example.com", "user@example.com", "USER@example.com"));
    }

    @ParameterizedTest(name = "Returns false when inputs are: {0}, {1}, {2}")
    @MethodSource("provideInvalidStringTriplets")
    @DisplayName("Returns false when any string is null, blank, or differs")
    void allEqualIgnoreCase_InvalidCases_ReturnsFalse(final String a, final String b, final String c)
    {
        assertFalse(StringInvariants.allEqualIgnoreCase(a, b, c));
    }
}
