package io.backbonehq.kit.throttle.api.key.resolver.http;

import jakarta.ws.rs.container.ContainerRequestContext;

public interface IpHeaderRateLimitKeyResolver
{
    String resolve(final ContainerRequestContext requestContext);
}
