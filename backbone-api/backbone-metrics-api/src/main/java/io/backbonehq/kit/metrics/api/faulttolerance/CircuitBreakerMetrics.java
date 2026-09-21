package io.backbonehq.kit.metrics.api.faulttolerance;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods or classes for automatic circuit breaker state change metrics collection. The {@link CircuitBreakerMetricsInterceptor}
 * will automatically record state transitions (open, closed, half_open) for methods annotated with
 * {@link org.eclipse.microprofile.faulttolerance.CircuitBreaker}.
 *
 * <p>This annotation works in conjunction with {@link org.eclipse.microprofile.faulttolerance.CircuitBreaker}.
 * The interceptor extracts:
 * <ul>
 * <li>Circuit breaker name from the method's class and method name</li>
 * <li>State transitions (closed → open, open → half_open, half_open → closed)</li>
 * </ul>
 *
 * <p>The interceptor derives the circuit breaker name from the class and method name
 * ({@code "ClassName#methodName"}) if {@link io.smallrye.faulttolerance.api.CircuitBreakerName}
 * is not present. Alternatively, you can explicitly name the circuit breaker using
 * {@link io.smallrye.faulttolerance.api.CircuitBreakerName}.
 *
 * <p><strong>Note:</strong> Circuit breakers are only accessible through
 * {@link io.smallrye.faulttolerance.api.CircuitBreakerMaintenance} when explicitly named.
 * If the derived name doesn't work, the interceptor will fail fast when attempting to
 * query the circuit breaker state.
 *
 * <p>This annotation can be used on methods or classes. When used on a class,
 * it applies to all methods in that class that have {@link org.eclipse.microprofile.faulttolerance.CircuitBreaker}.
 *
 * <p>Example usage with explicit naming (recommended):
 * <pre>
 * {@code
 * @CircuitBreakerMetrics
 *
 * @ApplicationScoped
 *                    public class ActorRepository {
 * @CircuitBreaker(delay = 1000)
 *                       @CircuitBreakerName("actor-repository-save")
 *                       public void save(final ActorRecord actorRecord) {
 *                       this.entityManager.persist(actorRecord);
 *                       }
 *                       }
 *                       }
 *                       </pre>
 *
 *                       <p>Example usage with derived name (may not work if circuit breaker isn't registered with that name):
 *                       <pre>
 *                       {@code
 *                       @CircuitBreakerMetrics
 * @ApplicationScoped
 *                    public class ActorRepository {
 * @CircuitBreaker(delay = 1000)
 *                       public void save(final ActorRecord actorRecord) {
 *                       this.entityManager.persist(actorRecord);
 *                       }
 *                       }
 *                       }
 *                       </pre>
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CircuitBreakerMetrics
{
}
