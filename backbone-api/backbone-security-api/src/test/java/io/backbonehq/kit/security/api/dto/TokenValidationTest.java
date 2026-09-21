package io.backbonehq.kit.security.api.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class TokenValidationTest
{
    private static final AuthIdentity TEST_USER = new AuthIdentity("user-id", "username", "email@example.com", List.of("user"), Instant.now());

    // --- Helpers to reduce boilerplate ---
    private static Supplier<TokenValidation> validResult()
    {
        return () -> TokenValidation.validIdentity(TEST_USER);
    }

    private static Supplier<TokenValidation> invalidResult(@SuppressWarnings("SameParameterValue") final String message)
    {
        return () -> TokenValidation.invalid(message);
    }

    private static <T> Function<T, TokenValidation> validator()
    {
        return t -> TokenValidation.validIdentity(TEST_USER);
    }

    // --- whenValid tests ---

    @Test
    void whenValid_nonNull_executesValidator()
    {
        final TokenValidation result = TokenValidation.whenValid("token", validator(), invalidResult("fail"));

        assertTrue(result.valid(), "Validator should execute for non-null value");
        assertEquals(TEST_USER, result.identity());
    }

    @Test
    void whenValid_null_executesFailureSupplier()
    {
        final TokenValidation result = TokenValidation.whenValid(null, validator(), invalidResult("fail"));

        assertFalse(result.valid(), "Failure supplier should execute for null value");
        assertEquals("fail", result.errorMessage());
    }

    @Test
    void whenValid_nullValidator_throwsNPE()
    {
        final NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> TokenValidation.whenValid("token", null, invalidResult("fail"))
        );

        assertEquals("validator cannot be null", ex.getMessage());
    }

    @Test
    void whenValid_nullFailureSupplier_throwsNPE()
    {
        final NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> TokenValidation.whenValid(null, validator(), null)
        );

        assertEquals("failureSupplier cannot be null", ex.getMessage());
    }

    // --- whenConditionNot tests ---

    @Test
    void whenConditionNot_conditionFalse_executesValidator()
    {
        final TokenValidation result = TokenValidation.whenConditionNot(false, validResult(), "error");

        assertTrue(result.valid(), "Validator should execute when condition is false");
        assertEquals(TEST_USER, result.identity());
    }

    @Test
    void whenConditionNot_conditionTrue_returnsInvalid()
    {
        final TokenValidation result = TokenValidation.whenConditionNot(true, validResult(), "error");

        assertFalse(result.valid(), "Should return invalid when condition is true");
        assertEquals("error", result.errorMessage());
    }

    @Test
    void whenConditionNot_nullValidator_throwsNPE()
    {
        final NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> TokenValidation.whenConditionNot(false, null, "error")
        );

        assertEquals("validator cannot be null", ex.getMessage());
    }

    @Test
    void whenConditionNot_nullErrorMessage_throwsNPE()
    {
        final NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> TokenValidation.whenConditionNot(true, validResult(), null)
        );

        assertEquals("errorMessage cannot be null", ex.getMessage());
    }
}
