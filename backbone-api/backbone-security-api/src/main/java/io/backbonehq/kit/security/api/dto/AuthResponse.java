package io.backbonehq.kit.security.api.dto;

import static java.util.Objects.requireNonNull;

import io.backbonehq.kit.metrics.api.dto.MetricsResultIndicator;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Value object representing the result of an authentication operation.
 */
@Schema(description = "Authentication response containing tokens and user information")
public record AuthResponse(
                           @Schema(description = "Indicates if authentication was successful", examples = "true") boolean success,
                           @Schema(description = "JWT access token for API authentication") String accessToken,
                           @Schema(description = "JWT ID token containing user identity claims") String idToken,
                           @Schema(description = "Refresh token for obtaining new access tokens") String refreshToken,
                           @Schema(description = "Token expiration timestamp") Instant expiresAt,
                           @Schema(description = "Authenticated user information") AuthIdentity user,
                           @Schema(description = "Error message if authentication failed", examples = "Invalid credentials") String errorMessage
) implements MetricsResultIndicator
{
    public static AuthResponse success(final String accessToken, final String idToken, final String refreshToken, final Instant expiresAt, final AuthIdentity user)
    {
        return new AuthResponse(true, accessToken, idToken, refreshToken, expiresAt, user, null);
    }

    public static AuthResponse failure(final String errorMessage)
    {
        return new AuthResponse(false, null, null, null, null, null, errorMessage);
    }

    public static <T> AuthResponse whenPresent(final T value, final Function<T, AuthResponse> onPresent, final Supplier<AuthResponse> onAbsent)
    {
        requireNonNull(onPresent, "onPresent cannot be null");
        requireNonNull(onAbsent, "onAbsent cannot be null");

        return value == null ? onAbsent.get() : onPresent.apply(value);
    }
}
