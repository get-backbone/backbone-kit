package io.backbonehq.kit.metrics.impl.domain;

import io.backbonehq.kit.metrics.api.domain.MetricsRecorder;
import io.backbonehq.kit.metrics.api.domain.ServiceMetrics;
import io.backbonehq.kit.metrics.api.domain.support.MetricsRecorderResolver;
import io.backbonehq.kit.metrics.api.dto.MetricsResultIndicator;
import io.backbonehq.kit.metrics.impl.dto.OptionalEmptyResult;
import io.backbonehq.kit.metrics.impl.dto.OptionalPresentResult;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * CDI interceptor that automatically collects metrics for methods annotated with {@link ServiceMetrics}. This
 * interceptor removes the need for manual metrics recording in business logic by delegating to type-safe metrics
 * recorder classes.
 *
 * <p>The interceptor:
 * <ul>
 * <li>Extracts the metrics recorder class from the annotation</li>
 * <li>Delegates to the recorder to record metrics based on method result</li>
 * <li>Handles exceptions by delegating to the recorder's exception recording method</li>
 * </ul>
 *
 * <p>Supports methods that return:
 * <ul>
 * <li>{@link MetricsResultIndicator} - passed directly to handler</li>
 * <li>{@code Optional<T>} where T implements MetricsResultIndicator - converted to synthetic indicator based on
 * presence</li>
 * <li>void - handler receives null and should extract data from request context or other sources</li>
 * </ul>
 *
 * <p>Example:
 * <pre>
 * {@code
 * @ServiceMetrics(AuthMetricsRecorder.class)
 * public AuthResponse login(LoginRequest request) {
 * return userAuthenticationProvider.login(request.getUsername(), request.getPassword());
 * }
 * }
 * </pre>
 * The operation name ("login") is automatically extracted from the method name.
 *
 * <p>The interceptor binding uses {@link jakarta.enterprise.util.Nonbinding Nonbinding} on the recorder class value,
 * allowing a single interceptor to handle all recorder types. The actual recorder
 * class is read from the method annotation at runtime.
 */
@ServiceMetrics(MetricsRecorder.class)
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_AFTER) // 3000
public final class ServiceMetricsInterceptor
{
    private final MetricsRecorderResolver recorderResolver;

    @Inject
    public ServiceMetricsInterceptor(final MetricsRecorderResolver recorderResolver)
    {
        this.recorderResolver = recorderResolver;
    }

    @AroundInvoke
    public Object collectMetrics(final InvocationContext context) throws Exception
    {
        final Optional<ResolvedContext> resolved = resolveContext(context);
        if (resolved.isPresent())
        {
            return proceedWithMetrics(context, resolved.get());
        }
        else
        {
            return context.proceed();
        }
    }

    /* -------------------------------------------------
     * Core execution
     * ------------------------------------------------- */

    private Object proceedWithMetrics(final InvocationContext context, final ResolvedContext rc) throws Exception
    {
        putTypeIfPresent(context, rc.annotation());

        try
        {
            final Object result = context.proceed();

            // Always call recordMetrics, even for void methods (pass null as indicator)
            // This allows recorders like ThrottleMetricsRecorder to extract data from request context
            final MetricsResultIndicator indicator = toMetricsResult(result).orElse(null);
            rc.recorder().recordMetrics(context, indicator);

            return result;
        }
        catch (final Exception e)
        {
            rc.recorder().recordException(context, e);
            throw e;
        }
    }

    /* -------------------------------------------------
     * Resolution
     * ------------------------------------------------- */

    private Optional<ResolvedContext> resolveContext(final InvocationContext context)
    {
        return findServiceMetrics(context)
            .flatMap(annotation -> recorderResolver
                .resolve(annotation.value())
                .map(recorder -> new ResolvedContext(annotation, recorder)));
    }

    private Optional<ServiceMetrics> findServiceMetrics(final InvocationContext context)
    {
        return Optional.ofNullable(
            context.getMethod().getAnnotation(ServiceMetrics.class)
        ).or(() -> Optional.ofNullable(
            context.getTarget()
                .getClass()
                .getAnnotation(ServiceMetrics.class)
        ));
    }

    /* -------------------------------------------------
     * Metrics result handling
     * ------------------------------------------------- */

    private Optional<MetricsResultIndicator> toMetricsResult(final Object result)
    {
        if (result instanceof MetricsResultIndicator indicator)
        {
            return Optional.of(indicator);
        }

        // Use traditional instanceof + cast instead of pattern matching to avoid Clover instrumentation issues
        if (result instanceof Optional<?>)
        {
            @SuppressWarnings("unchecked") Optional<?> optional = (Optional<?>) result;
            return Optional.of(
                optional
                    .map(o -> o instanceof MetricsResultIndicator i ? i : OptionalPresentResult.INSTANCE)
                    .orElse(OptionalEmptyResult.INSTANCE)
            );
        }

        return Optional.empty();
    }

    /* -------------------------------------------------
     * Context helpers
     * ------------------------------------------------- */

    private void putTypeIfPresent(final InvocationContext context, final ServiceMetrics annotation)
    {
        final String type = annotation.type();
        if (StringUtils.isNotEmpty(type))
        {
            context.getContextData().put("metrics.type", type);
        }
    }

    /* -------------------------------------------------
     * Internal model
     * ------------------------------------------------- */

    private record ResolvedContext(ServiceMetrics annotation, MetricsRecorder recorder)
    {
    }
}
