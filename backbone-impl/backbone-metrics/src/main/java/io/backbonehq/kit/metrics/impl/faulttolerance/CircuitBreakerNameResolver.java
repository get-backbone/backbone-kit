package io.backbonehq.kit.metrics.impl.faulttolerance;

import io.smallrye.faulttolerance.api.CircuitBreakerName;
import jakarta.interceptor.InvocationContext;

/**
 * Resolves circuit breaker names from method annotations or class/method names.
 *
 * <p>If {@link CircuitBreakerName} is present, that name is used. Otherwise, the name is derived
 * from the class simple name and method name: {@code "ClassName#methodName"}.
 *
 * <p>Note: Circuit breakers are only accessible through
 * {@link io.smallrye.faulttolerance.api.CircuitBreakerMaintenance} when
 * they are explicitly named. If the derived name doesn't work, the interceptor will fail fast
 * when attempting to query the circuit breaker state.
 */
public final class CircuitBreakerNameResolver
{
    private CircuitBreakerNameResolver()
    {
        // Utility class
    }

    /**
     * Builds the circuit breaker name from the class and method name, or from explicit {@link CircuitBreakerName}
     * annotation.
     *
     * @param context The invocation context
     * @return The circuit breaker name (from annotation or derived from class#method)
     */
    public static String resolve(final InvocationContext context)
    {
        final CircuitBreakerName methodAnnotation = context.getMethod().getAnnotation(CircuitBreakerName.class);
        final CircuitBreakerName classAnnotation = context.getMethod().getDeclaringClass().getAnnotation(
            CircuitBreakerName.class);
        return resolveAnnotationValue(methodAnnotation, classAnnotation, context);
    }

    private static String resolveAnnotationValue(final CircuitBreakerName methodAnnotation, final CircuitBreakerName classAnnotation, final InvocationContext context)
    {
        if (methodAnnotation != null && !methodAnnotation.value().isEmpty())
        {
            return methodAnnotation.value();
        }
        if (classAnnotation != null && !classAnnotation.value().isEmpty())
        {
            return classAnnotation.value() + "#" + context.getMethod().getName();
        }
        return context.getMethod().getDeclaringClass().getName() + "#" + context.getMethod().getName();
    }
}
