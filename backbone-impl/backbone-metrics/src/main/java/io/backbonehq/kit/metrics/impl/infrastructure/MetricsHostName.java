package io.backbonehq.kit.metrics.impl.infrastructure;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resolves a low-cardinality host identifier for Micrometer {@code instance} tags.
 */
public final class MetricsHostName
{
    private MetricsHostName()
    {
    }

    /**
     * Returns the local hostname, or {@code unknown} when resolution fails.
     */
    public static String localHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (final UnknownHostException error)
        {
            return "unknown";
        }
    }
}
