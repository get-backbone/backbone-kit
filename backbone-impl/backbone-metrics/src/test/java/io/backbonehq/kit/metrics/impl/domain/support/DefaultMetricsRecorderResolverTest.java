package io.backbonehq.kit.metrics.impl.domain.support;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.backbonehq.kit.metrics.api.domain.MetricsRecorder;
import io.backbonehq.kit.metrics.api.dto.MetricsResultIndicator;
import jakarta.enterprise.inject.Instance;
import jakarta.interceptor.InvocationContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultMetricsRecorderResolverTest
{
    @SuppressWarnings("unchecked")
    private final Instance<MetricsRecorder> recorders = mock(Instance.class);

    @Test
    @DisplayName("resolve returns empty when recorderClass is null")
    void resolve_ReturnsEmpty_WhenRecorderClassIsNull()
    {
        final DefaultMetricsRecorderResolver resolver = new DefaultMetricsRecorderResolver(recorders);
        final Optional<MetricsRecorder> result = resolver.resolve(null);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("resolve returns CDI-managed recorder when available")
    void resolve_ReturnsCdiManagedRecorder_WhenAvailable()
    {
        final TestMetricsRecorder cdiRecorder = new TestMetricsRecorder();
        @SuppressWarnings("unchecked") final Iterator<MetricsRecorder> iterator = (Iterator<MetricsRecorder>) (Iterator<?>) singletonList(
            cdiRecorder).iterator();
        when(recorders.iterator()).thenReturn(iterator);

        final DefaultMetricsRecorderResolver resolver = new DefaultMetricsRecorderResolver(recorders);
        final Optional<MetricsRecorder> result = resolver.resolve(TestMetricsRecorder.class);

        assertTrue(result.isPresent());
        assertEquals(cdiRecorder, result.get());
    }

    @Test
    @DisplayName("resolve creates recorder via reflection when CDI not available")
    void resolve_CreatesRecorderViaReflection_WhenCdiNotAvailable()
    {
        when(recorders.iterator()).thenReturn(Collections.emptyIterator());

        final DefaultMetricsRecorderResolver resolver = new DefaultMetricsRecorderResolver(recorders);
        final Optional<MetricsRecorder> result = resolver.resolve(TestMetricsRecorder.class);

        assertTrue(result.isPresent());
        assertInstanceOf(TestMetricsRecorder.class, result.get());
    }

    @Test
    @DisplayName("resolve caches recorder instances")
    void resolve_CachesRecorderInstances()
    {
        when(recorders.iterator()).thenReturn(Collections.emptyIterator());

        final DefaultMetricsRecorderResolver resolver = new DefaultMetricsRecorderResolver(recorders);
        final Optional<MetricsRecorder> result1 = resolver.resolve(TestMetricsRecorder.class);
        final Optional<MetricsRecorder> result2 = resolver.resolve(TestMetricsRecorder.class);

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        // Should be the same instance (cached)
        assertEquals(result1.get(), result2.get());
    }

    @Test
    @DisplayName("resolve returns empty when reflection fails")
    void resolve_ReturnsEmpty_WhenReflectionFails()
    {
        // Note: This test intentionally uses a class with a private constructor.
        // The resolver will log a WARNING when it cannot instantiate the recorder,
        // which is expected behavior and documents the failure case.
        when(recorders.iterator()).thenReturn(Collections.emptyIterator());

        final DefaultMetricsRecorderResolver resolver = new DefaultMetricsRecorderResolver(recorders);
        final Optional<MetricsRecorder> result = resolver.resolve(PrivateConstructorRecorder.class);

        assertFalse(result.isPresent());
    }

    // Test helper classes
    public static class TestMetricsRecorder implements MetricsRecorder
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

    @SuppressWarnings("unused")
    public static class PrivateConstructorRecorder extends TestMetricsRecorder
    {
        private PrivateConstructorRecorder()
        {
            // Private constructor - should fail reflection
        }
    }
}
