package io.backbonehq.kit.metrics.api.persistence;

/**
 * Marker interface for entity classes that should be tracked with database operation metrics.
 * Entity classes implementing this interface can be used with {@link DatabaseMetrics} annotation.
 *
 * <p>Provides a static helper method to generate metrics labels from entity class types.
 * By default, uses the simple class name (e.g., "ActorRecord").
 */
public interface MetricsRecord
{
    /**
     * Generates a metrics label for the given entity type.
     * By default, returns the simple class name.
     *
     * @param type The entity class type
     * @return The label to use in metrics (defaults to simple class name)
     */
    static String metricsLabelFor(Class<? extends MetricsRecord> type)
    {
        return type.getSimpleName();
    }
}
