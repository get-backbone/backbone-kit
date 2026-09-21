package io.backbonehq.kit.throttle.impl.key.resolver.http;

import io.backbonehq.kit.throttle.api.key.resolver.http.IpHeaderRateLimitKeyResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public final class HttpHeaderRateLimitIpKeyResolver implements IpHeaderRateLimitKeyResolver
{
    @Override
    public String resolve(final ContainerRequestContext requestContext)
    {
        // X-Forwarded-For (first IP)
        final String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        final String ip = extractFirstIp(forwardedFor);
        if (StringUtils.isNotBlank(ip))
        {
            return "ip:" + ip;
        }

        // X-Real-IP
        final String realIp = requestContext.getHeaderString("X-Real-IP");
        if (StringUtils.isNotBlank(realIp))
        {
            return "ip:" + realIp;
        }

        // Remote-Addr (container-specific, often absent)
        final String remoteAddr = requestContext.getHeaderString("Remote-Addr");
        if (StringUtils.isNotBlank(remoteAddr))
        {
            return "ip:" + remoteAddr;
        }

        return "ip:unknown";
    }

    private static String extractFirstIp(final String header)
    {
        if (StringUtils.isBlank(header))
        {
            return null;
        }

        final int comma = header.indexOf(',');
        return (comma >= 0 ? header.substring(0, comma) : header).trim();
    }
}
