package io.backbonehq.kit.security.api.dto;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class AuthResponseTest
{
    private static final AuthIdentity TEST_USER = new AuthIdentity("user-id", "username", "email@example.com", List.of("user"), now());

    // --- Helpers to reduce boilerplate ---
    private static Supplier<AuthResponse> successResponse()
    {
        return () -> AuthResponse.success("token", "id", "refresh", now().plusSeconds(3600), TEST_USER);
    }

    private static Supplier<AuthResponse> failureResponse(@SuppressWarnings("SameParameterValue") final String message)
    {
        return () -> AuthResponse.failure(message);
    }

    private static <T> Function<T, AuthResponse> validator()
    {
        return t -> AuthResponse.success("token", "id", "refresh", now().plusSeconds(3600), TEST_USER);
    }

    // --- whenPresent tests ---

    @Test
    void whenPresent_nonNull_executesOnPresent()
    {
        final String value = "component";

        final AuthResponse result = AuthResponse.whenPresent(value, validator(), failureResponse("fail"));

        assertTrue(result.success(), "Validator should execute for non-null value");
        assertEquals(TEST_USER, result.user());
    }

    @Test
    void whenPresent_null_executesOnAbsent()
    {
        final AuthResponse result = AuthResponse.whenPresent(null, validator(), failureResponse("fail"));

        assertFalse(result.success(), "Failure supplier should execute for null value");
        assertEquals("fail", result.errorMessage());
        assertNull(result.accessToken());
    }

    @Test
    void whenPresent_nullValidator_throwsNPE()
    {
        final NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> AuthResponse.whenPresent("value", null, failureResponse("fail"))
        );

        assertEquals("onPresent cannot be null", ex.getMessage());
    }

    @Test
    void whenPresent_nullSupplier_throwsNPE()
    {
        final NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> AuthResponse.whenPresent(null, validator(), null)
        );

        assertEquals("onAbsent cannot be null", ex.getMessage());
    }

    // --- Example usage with inline supplier for failure ---

    @Test
    void whenPresent_failureMessageInline()
    {
        final String value = null;

        final AuthResponse result = AuthResponse.whenPresent(
            value,
            validator(),
            () -> AuthResponse.failure("Component missing")
        );

        assertFalse(result.success());
        assertEquals("Component missing", result.errorMessage());
    }
}
