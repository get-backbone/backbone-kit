package io.backbonehq.kit.metrics.impl.domain.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetricsTagSanitizerTest
{
    @Test
    @DisplayName("sanitize returns 'unknown' for null value")
    void sanitize_ReturnsUnknown_ForNullValue()
    {
        assertEquals("unknown", MetricsTagSanitizer.sanitize(null));
    }

    @Test
    @DisplayName("sanitize returns 'unknown' for empty string")
    void sanitize_ReturnsUnknown_ForEmptyString()
    {
        assertEquals("unknown", MetricsTagSanitizer.sanitize(""));
    }

    @Test
    @DisplayName("sanitize replaces colons with underscores")
    void sanitize_ReplacesColons_WithUnderscores()
    {
        assertEquals("exception__CircuitBreakerOpenException", MetricsTagSanitizer.sanitize("exception: CircuitBreakerOpenException"));
        assertEquals("error_type_value", MetricsTagSanitizer.sanitize("error:type:value"));
        assertEquals("key_value", MetricsTagSanitizer.sanitize("key:value"));
    }

    @Test
    @DisplayName("sanitize replaces spaces with underscores")
    void sanitize_ReplacesSpaces_WithUnderscores()
    {
        assertEquals("not_found", MetricsTagSanitizer.sanitize("not found"));
        assertEquals("invalid_credentials", MetricsTagSanitizer.sanitize("invalid credentials"));
        assertEquals("multiple__spaces__between__words", MetricsTagSanitizer.sanitize("multiple  spaces  between  words"));
    }

    @Test
    @DisplayName("sanitize replaces newlines with underscores")
    void sanitize_ReplacesNewlines_WithUnderscores()
    {
        assertEquals("line1_line2", MetricsTagSanitizer.sanitize("line1\nline2"));
        assertEquals("text_with_newline", MetricsTagSanitizer.sanitize("text with\nnewline"));
    }

    @Test
    @DisplayName("sanitize replaces carriage returns with underscores")
    void sanitize_ReplacesCarriageReturns_WithUnderscores()
    {
        assertEquals("line1_line2", MetricsTagSanitizer.sanitize("line1\rline2"));
        assertEquals("text_with_carriage_return", MetricsTagSanitizer.sanitize("text with\rcarriage return"));
    }

    @Test
    @DisplayName("sanitize replaces tabs with underscores")
    void sanitize_ReplacesTabs_WithUnderscores()
    {
        assertEquals("field1_field2", MetricsTagSanitizer.sanitize("field1\tfield2"));
        assertEquals("text_with_tab", MetricsTagSanitizer.sanitize("text with\ttab"));
    }

    @Test
    @DisplayName("sanitize handles multiple problematic characters")
    void sanitize_HandlesMultipleProblematicCharacters()
    {
        assertEquals("exception__CircuitBreakerOpenException_not_found", MetricsTagSanitizer.sanitize(
            "exception: CircuitBreakerOpenException\nnot found"));
        assertEquals("error_type_value_with_spaces", MetricsTagSanitizer.sanitize("error:type:value with spaces"));
        assertEquals("key_value_with_tab", MetricsTagSanitizer.sanitize("key:value\twith\ntab"));
    }

    @Test
    @DisplayName("sanitize truncates values longer than 100 characters")
    void sanitize_TruncatesValues_LongerThan100Characters()
    {
        final String longValue = "a".repeat(150);
        final String result = MetricsTagSanitizer.sanitize(longValue);

        assertEquals(100, result.length());
        assertEquals("a".repeat(97) + "...", result);
    }

    @Test
    @DisplayName("sanitize truncates after character replacement")
    void sanitize_TruncatesAfterCharacterReplacement()
    {
        // Create a string that's 101 chars (50 a's + colon + 50 b's)
        // After replacing colon with underscore, it's still 101 chars, so gets truncated to 100
        final String valueWithColons = "a".repeat(50) + ":" + "b".repeat(50);
        final String result = MetricsTagSanitizer.sanitize(valueWithColons);

        // Should be truncated to 100 chars total: 50 a's + 1 underscore + 46 b's + "..." = 100
        assertEquals(100, result.length());
        assertEquals("a".repeat(50) + "_" + "b".repeat(46) + "...", result);
    }

    @Test
    @DisplayName("sanitize preserves valid values unchanged")
    void sanitize_PreservesValidValues_Unchanged()
    {
        assertEquals("not_found", MetricsTagSanitizer.sanitize("not_found"));
        assertEquals("invalid_credentials", MetricsTagSanitizer.sanitize("invalid_credentials"));
        assertEquals("exception_type", MetricsTagSanitizer.sanitize("exception_type"));
        assertEquals("CircuitBreakerOpenException", MetricsTagSanitizer.sanitize("CircuitBreakerOpenException"));
    }

    @Test
    @DisplayName("sanitize handles values exactly 100 characters")
    void sanitize_HandlesValues_Exactly100Characters()
    {
        final String exactly100Chars = "a".repeat(100);
        final String result = MetricsTagSanitizer.sanitize(exactly100Chars);

        assertEquals(100, result.length());
        assertEquals(exactly100Chars, result);
    }

    @Test
    @DisplayName("sanitize handles values with 101 characters")
    void sanitize_HandlesValues_With101Characters()
    {
        final String value101Chars = "a".repeat(101);
        final String result = MetricsTagSanitizer.sanitize(value101Chars);

        assertEquals(100, result.length());
        assertEquals("a".repeat(97) + "...", result);
    }

    @Test
    @DisplayName("sanitize handles real-world exception reason formats")
    void sanitize_HandlesRealWorldExceptionReasonFormats()
    {
        assertEquals("exception__CircuitBreakerOpenException", MetricsTagSanitizer.sanitize("exception: CircuitBreakerOpenException"));
        assertEquals("exception__IllegalArgumentException", MetricsTagSanitizer.sanitize("exception: IllegalArgumentException"));
        assertEquals("exception__NullPointerException", MetricsTagSanitizer.sanitize("exception: NullPointerException"));
    }

    @Test
    @DisplayName("sanitize handles real-world error message formats")
    void sanitize_HandlesRealWorldErrorMessageFormats()
    {
        assertEquals("not_found", MetricsTagSanitizer.sanitize("not_found"));
        assertEquals("invalid_credentials", MetricsTagSanitizer.sanitize("invalid_credentials"));
        assertEquals("expired_token", MetricsTagSanitizer.sanitize("expired_token"));
        assertEquals("rate_limit_exceeded", MetricsTagSanitizer.sanitize("rate_limit_exceeded"));
    }

    @Test
    @DisplayName("sanitize handles unicode characters")
    void sanitize_HandlesUnicodeCharacters()
    {
        assertEquals("café_naïve", MetricsTagSanitizer.sanitize("café naïve"));
        assertEquals("error__中文", MetricsTagSanitizer.sanitize("error: 中文"));
    }

    @Test
    @DisplayName("sanitize handles special characters that are not problematic")
    void sanitize_HandlesSpecialCharacters_ThatAreNotProblematic()
    {
        assertEquals("error-123", MetricsTagSanitizer.sanitize("error-123"));
        assertEquals("value.with.dots", MetricsTagSanitizer.sanitize("value.with.dots"));
        assertEquals("test_underscore", MetricsTagSanitizer.sanitize("test_underscore"));
    }
}
