package io.backbonehq.kit.metrics.impl.domain.support;

/**
 * Utility class for sanitizing tag values for Prometheus metrics.
 * Prometheus tag values must be valid UTF-8 and should not contain certain special characters
 * that can cause export issues.
 */
public final class MetricsTagSanitizer
{
    private static final int MAX_TAG_VALUE_LENGTH = 100;
    private static final String TRUNCATION_SUFFIX = "...";

    private MetricsTagSanitizer()
    {
        // Utility class - prevent instantiation
    }

    /**
     * Sanitizes a tag value for use in Prometheus metrics.
     * Replaces problematic characters and truncates long values to prevent export issues.
     *
     * @param value The tag value to sanitize
     * @return Sanitized tag value safe for use in Prometheus metrics
     */
    public static String sanitize(final String value)
    {
        if (value == null || value.isEmpty())
        {
            return "unknown";
        }

        // Replace problematic characters that can cause Prometheus export issues
        String sanitized = value
            .replace(":", "_")  // Colons can cause issues in Prometheus
            .replace(" ", "_")  // Spaces should be underscores
            .replace("\n", "_")  // Newlines should be removed
            .replace("\r", "_")  // Carriage returns should be removed
            .replace("\t", "_"); // Tabs should be removed

        // Truncate very long values to prevent metric cardinality explosion
        if (sanitized.length() > MAX_TAG_VALUE_LENGTH)
        {
            final int maxLength = MAX_TAG_VALUE_LENGTH - TRUNCATION_SUFFIX.length();
            sanitized = sanitized.substring(0, maxLength) + TRUNCATION_SUFFIX;
        }

        return sanitized;
    }
}
