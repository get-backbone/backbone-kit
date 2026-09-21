package io.backbonehq.kit.security.impl.jwt;

import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import io.backbonehq.kit.security.api.jwt.JwtPrincipalExtractor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Resolves JWT principals from tokens using a list of extractors.
 * <p>
 * This class attempts to extract a principal from a JWT token by trying each
 * registered {@link JwtPrincipalExtractor} in order until one succeeds.
 * </p>
 */
@ApplicationScoped
public final class JwtPrincipalResolver
{
    private final Instance<JwtPrincipalExtractor> extractors;

    @Inject
    public JwtPrincipalResolver(final Instance<JwtPrincipalExtractor> extractors)
    {
        this.extractors = extractors;
    }

    /**
     * Resolves a principal from a JWT token.
     * <p>
     * Attempts to extract a principal by trying each registered extractor in order.
     * Returns the first successful extraction or empty if no extractor succeeds.
     * </p>
     *
     * @param jwtToken the JWT token
     * @return the resolved principal, or empty if none could be extracted
     */
    public Optional<JwtPrincipal> resolveFromToken(final String jwtToken)
    {
        return JwtPayloadParser.parsePayload(jwtToken)
            .flatMap(payload -> StreamSupport.stream(extractors.spliterator(), false)
                .map(extractor -> extractor.extract(payload))
                .flatMap(Optional::stream)
                .findFirst());
    }
}
