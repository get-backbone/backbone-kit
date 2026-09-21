package io.backbonehq.kit.throttle.impl.key.strategy;

import io.backbonehq.kit.throttle.api.key.resolver.http.IpHeaderRateLimitKeyResolver;
import io.backbonehq.kit.throttle.api.key.resolver.jwt.AuthHeaderRateLimitKeyResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public final class HttpHeaderRateLimitKeyStrategy
{
    @Inject
    AuthHeaderRateLimitKeyResolver authHeaderRateLimitKeyResolver;

    @Inject
    IpHeaderRateLimitKeyResolver ipHeaderRateLimitKeyResolver;

    /**
     * Resolve a rate-limiting key from the request context. Priority: service token -> user token -> auth:unidentified
     * (if Authorization header is present) -> IP-based.
     * <p>
     * If an Authorization header is present, but we cannot extract a service ID or username, we use "auth:unidentified"
     * to prevent falling back to IP-based rate limiting, which could be exploited by sending malformed tokens.
     */
    public String resolve(final ContainerRequestContext requestContext)
    {
        final String authorizationHeader = requestContext.getHeaderString("Authorization");
        return StringUtils.isNotBlank(authorizationHeader) ? authHeaderRateLimitKeyResolver.resolve(
            authorizationHeader) : ipHeaderRateLimitKeyResolver.resolve(requestContext);
    }
}
