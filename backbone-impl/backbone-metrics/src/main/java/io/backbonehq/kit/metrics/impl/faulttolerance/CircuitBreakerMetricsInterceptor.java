package io.backbonehq.kit.metrics.impl.faulttolerance;

import io.backbonehq.kit.metrics.api.faulttolerance.CircuitBreakerMetrics;
import io.smallrye.faulttolerance.api.CircuitBreakerState;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

/**
 * CDI interceptor that automatically collects circuit breaker state change metrics for methods annotated with both
 * {@link CircuitBreaker} and {@link CircuitBreakerMetrics}.
 *
 * <p>The interceptor:
 * <ul>
 * <li>Detects methods with {@link CircuitBreaker} annotation</li>
 * <li>Tracks circuit breaker state before and after method execution</li>
 * <li>Records state transitions (closed → open, open → half_open, half_open → closed)</li>
 * <li>Uses SmallRye Fault Tolerance API to query circuit breaker state</li>
 * </ul>
 *
 * <p>This interceptor works with any return type and records state changes regardless
 * of success or failure. State changes are detected by querying the circuit breaker
 * state before and after method execution.
 *
 * <p>Example:
 * <pre>
 * {@code
 * @CircuitBreakerMetrics
 *
 * @ApplicationScoped
 *                    public class ActorRepository {
 * @CircuitBreaker(delay = 1000)
 *                       public void save(final ActorRecord actorRecord) {
 *                       this.entityManager.persist(actorRecord);
 *                       }
 *                       }
 *                       }
 *                       </pre>
 *
 *                       <p>The circuit breaker name is derived from the fully qualified class and method name:
 *                       {@code "fully.qualified.ClassName#methodName"}.
 */
@CircuitBreakerMetrics
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_AFTER) // 3000
public final class CircuitBreakerMetricsInterceptor
{
    private final CircuitBreakerStateTracker stateTracker;

    @Inject
    public CircuitBreakerMetricsInterceptor(CircuitBreakerStateTracker stateTracker)
    { this.stateTracker = stateTracker; }

    @AroundInvoke
    public Object collectCircuitBreakerMetrics(final InvocationContext context) throws Exception
    {
        final CircuitBreaker circuitBreakerAnnotation = findCircuitBreakerAnnotation(context);
        if (circuitBreakerAnnotation == null)
        {
            // No @CircuitBreaker annotation, proceed without metrics
            return context.proceed();
        }

        final String circuitName = CircuitBreakerNameResolver.resolve(context);
        final CircuitBreakerState stateBefore = stateTracker.getPreviousState(circuitName);

        return proceedAndRecordState(context, circuitName, stateBefore);
    }

    private Object proceedAndRecordState(final InvocationContext context, final String circuitName, final CircuitBreakerState stateBefore) throws Exception
    {
        try
        {
            final Object result = context.proceed();
            stateTracker.recordStateTransition(circuitName, stateBefore);
            return result;
        }
        catch (final Exception e)
        {
            stateTracker.recordStateTransition(circuitName, stateBefore);
            throw e;
        }
    }

    /**
     * Finds the {@link CircuitBreaker} annotation on the method or class.
     *
     * @param context The invocation context
     * @return The annotation, or null if not found
     */
    private CircuitBreaker findCircuitBreakerAnnotation(final InvocationContext context)
    {
        final CircuitBreaker methodAnnotation = context.getMethod().getAnnotation(CircuitBreaker.class);
        return methodAnnotation != null ? methodAnnotation : context.getMethod().getDeclaringClass().getAnnotation(
            CircuitBreaker.class);
    }
}
