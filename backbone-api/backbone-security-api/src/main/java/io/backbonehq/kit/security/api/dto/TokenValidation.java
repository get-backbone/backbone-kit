package io.backbonehq.kit.security.api.dto;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;

/** Value object representing the result of token validation. */
public record TokenValidation(boolean valid, AuthIdentity identity, String serviceId, String errorMessage)
{
    public static TokenValidation validIdentity(final AuthIdentity identity)
    {
        return new TokenValidation(true, identity, null, null);
    }

    public static TokenValidation validService(final String serviceId)
    {
        return new TokenValidation(true, null, serviceId, null);
    }

    public static TokenValidation invalid(final String errorMessage)
    {
        return new TokenValidation(false, null, null, errorMessage);
    }

    /**
     * Canonical combinator: return validator result if value is non-null, else failure from supplier.
     */
    public static <T> TokenValidation whenValid(final T value, final Function<T, TokenValidation> validator, final Supplier<TokenValidation> failureSupplier)
    {
        requireNonNull(validator, "validator cannot be null");
        requireNonNull(failureSupplier, "failureSupplier cannot be null");

        return value != null ? validator.apply(value) : failureSupplier.get();
    }

    /**
     * Boolean-based conditional combinator (negated version included).
     */
    public static TokenValidation whenConditionNot(final boolean condition, final Supplier<TokenValidation> validator, final String errorMessage)
    {
        requireNonNull(validator, "validator cannot be null");
        requireNonNull(errorMessage, "errorMessage cannot be null");

        return !condition ? validator.get() : invalid(errorMessage);
    }

    /**
     * Check if this validation result represents a service account.
     *
     * @return true if this is a service token, false if it's an identity token
     */
    public boolean isServiceToken()
    { return StringUtils.isNotEmpty(serviceId); }
}
