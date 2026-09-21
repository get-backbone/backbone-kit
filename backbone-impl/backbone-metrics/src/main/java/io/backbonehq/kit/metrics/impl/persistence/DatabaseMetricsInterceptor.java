package io.backbonehq.kit.metrics.impl.persistence;

import io.backbonehq.kit.metrics.api.persistence.DatabaseMetrics;
import io.backbonehq.kit.metrics.api.persistence.MetricsRecord;
import io.backbonehq.kit.metrics.impl.persistence.recorder.DatabaseMetricsRecorder;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

/**
 * CDI interceptor that automatically collects timing metrics for database operations
 * annotated with {@link DatabaseMetrics}.
 *
 * <p>The interceptor:
 * <ul>
 * <li>Measures method execution time</li>
 * <li>Extracts operation name from method name</li>
 * <li>Extracts entity type from annotation</li>
 * <li>Records metrics directly via Micrometer</li>
 * </ul>
 *
 * <p>This interceptor works with any return type (void, Optional, domain objects, etc.)
 * and records timing regardless of success or failure. Exceptions are re-thrown after
 * metrics are recorded.
 *
 * <p>Example:
 * <pre>
 * {@code
 * @DatabaseMetrics(entity = ActorRecord.class)
 * public class ActorRepository {
 * public void save(final ActorRecord actorRecord) {
 * this.entityManager.persist(actorRecord);
 * }
 * }
 * }
 * </pre>
 * The operation name ("save") is automatically extracted from the method name.
 */
@DatabaseMetrics(entity = MetricsRecord.class)
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_AFTER) // 3000
public final class DatabaseMetricsInterceptor
{
    private static final Logger LOGGER = Logger.getLogger(DatabaseMetricsInterceptor.class);

    private final DatabaseMetricsRecorder databaseMetricsRecorder;

    @Inject
    public DatabaseMetricsInterceptor(DatabaseMetricsRecorder databaseMetricsRecorder)
    {
        this.databaseMetricsRecorder = databaseMetricsRecorder;
    }

    @AroundInvoke
    public Object collectDatabaseMetrics(final InvocationContext context) throws Exception
    {
        LOGGER.debugf("DatabaseMetricsInterceptor invoked for method: %s.%s",
            context.getTarget().getClass().getSimpleName(),
            context.getMethod().getName());

        final DatabaseMetrics annotation = findAnnotation(context);
        if (annotation == null)
        {
            LOGGER.debugf("No @DatabaseMetrics annotation found, proceeding without metrics");
            return context.proceed();
        }

        LOGGER.debugf("Found @DatabaseMetrics annotation with entity: %s", annotation.entity().getSimpleName());

        final Class<? extends MetricsRecord> entity = annotation.entity();
        final String operation = context.getMethod().getName();
        final long startTime = System.nanoTime();

        try
        {
            final Object result = context.proceed();
            final long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            databaseMetricsRecorder.recordDatabaseOperation(operation, entity, durationMs);

            return result;
        }
        catch (final Exception e)
        {
            // Still record timing even on failure - useful for identifying slow failing operations
            final long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            databaseMetricsRecorder.recordDatabaseOperation(operation, entity, durationMs);

            throw e;
        }
    }

    private DatabaseMetrics findAnnotation(final InvocationContext context)
    {
        // Check method-level annotation first
        final DatabaseMetrics methodAnnotation = context.getMethod()
            .getAnnotation(DatabaseMetrics.class);
        if (methodAnnotation != null)
        {
            return methodAnnotation;
        }

        // Fall back to class-level annotation
        // Use getDeclaringClass() instead of getTarget().getClass() because
        // Quarkus creates proxy subclasses (e.g., ActorRepository_Subclass)
        // and the annotation is on the original class
        return context.getMethod().getDeclaringClass().getAnnotation(DatabaseMetrics.class);
    }
}
