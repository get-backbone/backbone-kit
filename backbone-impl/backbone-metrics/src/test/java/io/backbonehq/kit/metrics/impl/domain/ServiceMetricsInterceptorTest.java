package io.backbonehq.kit.metrics.impl.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.backbonehq.kit.metrics.api.domain.MetricsRecorder;
import io.backbonehq.kit.metrics.api.domain.ServiceMetrics;
import io.backbonehq.kit.metrics.api.domain.support.MetricsRecorderResolver;
import io.backbonehq.kit.metrics.api.dto.MetricsResultIndicator;
import io.backbonehq.kit.metrics.impl.dto.OptionalEmptyResult;
import io.backbonehq.kit.metrics.impl.dto.OptionalPresentResult;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServiceMetricsInterceptorTest
{
    private final MetricsRecorderResolver recorderResolver = mock(MetricsRecorderResolver.class);

    private final MetricsRecorder recorder = mock(MetricsRecorder.class);

    private final InvocationContext context = mock(InvocationContext.class);

    @SuppressWarnings("unchecked")
    private final Map<String, Object> contextData = mock(Map.class);

    @Test
    @DisplayName("collectMetrics proceeds without metrics when no ServiceMetrics annotation")
    void collectMetrics_ProceedsWithoutMetrics_WhenNoServiceMetricsAnnotation() throws Exception
    {
        final TestTarget target = new TestTarget();
        when(context.getMethod()).thenReturn(TestTarget.class.getMethod("methodWithoutAnnotation"));
        when(context.getTarget()).thenReturn(target);
        when(context.proceed()).thenReturn("result");

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        final Object result = interceptor.collectMetrics(context);

        assertEquals("result", result);
        verify(context).proceed();
        verify(recorderResolver, never()).resolve(any());
    }

    @Test
    @DisplayName("collectMetrics records metrics for MetricsResultIndicator result")
    void collectMetrics_RecordsMetrics_ForMetricsResultIndicatorResult() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithAnnotation");
        final MetricsResultIndicator result = new TestMetricsResult(true, null);

        when(context.getMethod()).thenReturn(method);
        // getTarget() not needed for method-level annotations, but lenient in case interceptor checks it
        lenient().when(context.getTarget()).thenReturn(target);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenReturn(result);

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        final Object actualResult = interceptor.collectMetrics(context);

        assertEquals(result, actualResult);
        verify(recorder).recordMetrics(context, result);
        verify(recorder, never()).recordException(any(), any());
    }

    @Test
    @DisplayName("collectMetrics records exception when method throws")
    void collectMetrics_RecordsException_WhenMethodThrows() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithAnnotation");
        final RuntimeException exception = new RuntimeException("Test exception");

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenThrow(exception);

        try
        {
            final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
            interceptor.collectMetrics(context);
        }
        catch (final RuntimeException e)
        {
            assertEquals(exception, e);
        }

        verify(recorder).recordException(context, exception);
        verify(recorder, never()).recordMetrics(any(), any());
    }

    @Test
    @DisplayName("collectMetrics converts Optional<MetricsResultIndicator> to indicator")
    void collectMetrics_ConvertsOptional_ToIndicator() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithAnnotation");
        final MetricsResultIndicator indicator = new TestMetricsResult(true, null);
        final Optional<MetricsResultIndicator> optionalResult = Optional.of(indicator);

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenReturn(optionalResult);

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        final Object result = interceptor.collectMetrics(context);

        assertEquals(optionalResult, result);
        verify(recorder).recordMetrics(context, indicator);
    }

    @Test
    @DisplayName("collectMetrics converts Optional.empty() to OptionalEmptyResult")
    void collectMetrics_ConvertsOptionalEmpty_ToOptionalEmptyResult() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithAnnotation");
        final Optional<MetricsResultIndicator> emptyOptional = Optional.empty();

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenReturn(emptyOptional);

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        final Object result = interceptor.collectMetrics(context);

        assertEquals(emptyOptional, result);
        verify(recorder).recordMetrics(context, OptionalEmptyResult.INSTANCE);
    }

    @Test
    @DisplayName("collectMetrics converts Optional<NonIndicator> to OptionalPresentResult")
    void collectMetrics_ConvertsOptionalNonIndicator_ToOptionalPresentResult() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithAnnotation");
        final Optional<String> optionalString = Optional.of("test");

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenReturn(optionalString);

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        final Object result = interceptor.collectMetrics(context);

        assertEquals(optionalString, result);
        verify(recorder).recordMetrics(context, OptionalPresentResult.INSTANCE);
    }

    @Test
    @DisplayName("collectMetrics records for void methods")
    void collectMetrics_Records_ForVoidMethods() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("voidMethodWithAnnotation");

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenReturn(null);

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        interceptor.collectMetrics(context);

        verify(recorder).recordMetrics(any(), eq(null));
    }

    @Test
    @DisplayName("collectMetrics puts type in context when annotation has type")
    void collectMetrics_PutsTypeInContext_WhenAnnotationHasType() throws Exception
    {
        // Given - need a method with type annotation
        final TestTargetWithType target = new TestTargetWithType();
        final Method method = TestTargetWithType.class.getMethod("methodWithType");
        final MetricsResultIndicator result = new TestMetricsResult(true, null);

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(context.getContextData()).thenReturn(contextData);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenReturn(result);

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        interceptor.collectMetrics(context);

        verify(contextData).put("metrics.type", "user");
        verify(recorder).recordMetrics(context, result);
    }

    @Test
    @DisplayName("collectMetrics finds class-level annotation when method-level not present")
    void collectMetrics_FindsClassLevelAnnotation_WhenMethodLevelNotPresent() throws Exception
    {
        final TestTargetWithClassAnnotation target = new TestTargetWithClassAnnotation();
        final Method method = TestTargetWithClassAnnotation.class.getMethod("methodWithoutMethodAnnotation");
        final MetricsResultIndicator result = new TestMetricsResult(true, null);

        when(context.getMethod()).thenReturn(method);
        when(context.getTarget()).thenReturn(target);
        when(recorderResolver.resolve(any())).thenReturn(Optional.of(recorder));
        when(context.proceed()).thenReturn(result);

        final ServiceMetricsInterceptor interceptor = new ServiceMetricsInterceptor(recorderResolver);
        interceptor.collectMetrics(context);

        verify(recorder).recordMetrics(context, result);
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestTarget
    {
        public String methodWithoutAnnotation()
        {
            return "result";
        }

        @ServiceMetrics(TestMetricsRecorder.class)
        public MetricsResultIndicator methodWithAnnotation()
        {
            return new TestMetricsResult(true, null);
        }

        @ServiceMetrics(TestMetricsRecorder.class)
        public void voidMethodWithAnnotation()
        {
            // void method
        }
    }

    @ServiceMetrics(TestMetricsRecorder.class)
    @SuppressWarnings("unused")
    static class TestTargetWithClassAnnotation
    {
        public MetricsResultIndicator methodWithoutMethodAnnotation()
        {
            return new TestMetricsResult(true, null);
        }
    }

    @SuppressWarnings("unused")
    static class TestTargetWithType
    {
        @ServiceMetrics(value = TestMetricsRecorder.class, type = "user")
        public MetricsResultIndicator methodWithType()
        {
            return new TestMetricsResult(true, null);
        }
    }

    record TestMetricsResult(boolean success, String errorMessage) implements MetricsResultIndicator
    {
    }

    static class TestMetricsRecorder implements MetricsRecorder
    {
        @Override
        public void recordMetrics(final InvocationContext context, final MetricsResultIndicator metricsResultIndicator)
        {
            // Test implementation
        }

        @Override
        public void recordException(final InvocationContext context, final Exception exception)
        {
            // Test implementation
        }
    }
}
