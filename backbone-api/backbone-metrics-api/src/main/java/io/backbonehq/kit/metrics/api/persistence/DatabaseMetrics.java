package io.backbonehq.kit.metrics.api.persistence;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark repository methods for automatic database operation metrics collection.
 * The {@link DatabaseMetricsInterceptor} will automatically record timing metrics
 * for database operations (PostgreSQL, DynamoDB, etc.).
 *
 * <p>This annotation extracts:
 * <ul>
 * <li>Operation name from the method name (e.g., "save", "findByActorId")</li>
 * <li>Entity type from the annotation's {@code entity} parameter</li>
 * <li>Duration from method execution time</li>
 * </ul>
 *
 * <p>This annotation can be used on methods or classes. When used on a class,
 * it applies to all methods in that class.
 *
 * <p>Example usage:
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
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DatabaseMetrics
{
    /**
     * The entity type for this database operation.
     * The entity class must implement {@link MetricsRecord}.
     * This is used as a tag in the metrics to distinguish operations on different entity types.
     *
     * @return the entity type class
     */
    @Nonbinding
    Class<? extends MetricsRecord> entity();
}
