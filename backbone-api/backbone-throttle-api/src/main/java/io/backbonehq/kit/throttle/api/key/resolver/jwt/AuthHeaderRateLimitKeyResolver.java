package io.backbonehq.kit.throttle.api.key.resolver.jwt;

public interface AuthHeaderRateLimitKeyResolver
{
    String resolve(final String authorizationHeader);
}
