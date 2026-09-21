package io.backbonehq.kit.metrics.api.domain;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic service-level metrics collection.
 * The {@link ServiceMetricsInterceptor} will automatically record metrics
 * based on the method's execution result using the specified metrics recorder class.
 *
 * <p>Each metrics recorder class implements specific logic for recording metrics.
 * For example, an AuthMetricsRecorder implementation would handle authentication metrics.
 *
 * <p>This annotation can be used on methods or classes. When used on a class,
 * it applies to all methods in that class.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * @ServiceMetrics(AuthMetricsRecorder.class, type = "user")
 * public AuthResponse login(LoginRequest request) {
 * return userAuthenticationProvider.login(request.getUsername(), request.getPassword());
 * }
 *
 * @ServiceMetrics(AuthMetricsRecorder.class, type = "service")
 *                                            public AuthResponse authenticateService(LoginRequest request) {
 *                                            return serviceAuthenticationProvider.authenticateService(...);
 *                                            }
 *                                            }
 *                                            </pre>
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ServiceMetrics
{
    /**
     * The metrics recorder class to use for recording metrics.
     * Must implement {@link MetricsRecorder}.
     *
     * <p>Marked as {@link Nonbinding} so that different recorder classes are treated
     * as the same interceptor binding. The interceptor reads the actual recorder class
     * from the annotation at runtime.
     *
     * @return the metrics recorder class
     */
    @Nonbinding
    Class<? extends MetricsRecorder> value();

    /**
     * Optional type/category for the metrics. This can be used to add additional
     * metadata tags to metrics (e.g., "user", "service", "internal", "external").
     * The recorder can use this to add a "type" tag to distinguish different categories
     * of operations.
     *
     * <p>If not specified, the recorder may infer the type from the operation name
     * or omit the type tag entirely.
     *
     * @return the type/category for these metrics, or empty string if not specified
     */
    @Nonbinding
    String type() default "";
}
