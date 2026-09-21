package io.backbonehq.kit.security.impl.reference;

import com.fasterxml.jackson.databind.JsonNode;
import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import io.backbonehq.kit.security.api.jwt.JwtPrincipalExtractor;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/**
 * Reference implementation of OIDC user principal extractor for testing.
 * <p>
 * This extractor supports standard OIDC/OAuth2 claims for extracting user principals from JWT tokens.
 * It provides a simple reference implementation that can be used in tests and as an example for
 * implementing custom extractors.
 * </p>
 * <p>
 * The supported claims are (in order of precedence): {@code email}, {@code preferred_username},
 * {@code username}, and {@code sub} (subject). All of these are standard OIDC/OAuth2 claims.
 * </p>
 */
@ApplicationScoped
public final class ReferenceOidcUserPrincipalExtractor implements JwtPrincipalExtractor
{
    private static final List<String> USERNAME_FIELDS = List.of(
        "email",
        "preferred_username",
        "username",
        "sub"
    );

    @Override
    public Optional<JwtPrincipal> extract(final JsonNode payload)
    {
        for (final String field : USERNAME_FIELDS)
        {
            final JsonNode value = payload.get(field);
            if (value != null && value.isTextual() && !value.asText().isBlank())
            {
                return Optional.of(new JwtPrincipal.User(value.asText()));
            }
        }

        return Optional.empty();
    }
}
