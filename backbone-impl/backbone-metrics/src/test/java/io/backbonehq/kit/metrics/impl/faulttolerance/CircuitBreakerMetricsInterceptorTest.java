package io.backbonehq.kit.metrics.impl.faulttolerance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.smallrye.faulttolerance.api.CircuitBreakerState;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CircuitBreakerMetricsInterceptorTest
{
    private final CircuitBreakerStateTracker stateTracker = mock(CircuitBreakerStateTracker.class);

    private final InvocationContext context = mock(InvocationContext.class);

    @Test
    @DisplayName("collectCircuitBreakerMetrics proceeds without metrics when no CircuitBreaker annotation")
    void collectCircuitBreakerMetrics_ProceedsWithoutMetrics_WhenNoCircuitBreakerAnnotation() throws Exception
    {
        final TestTarget target = new TestTarget();
        when(context.getMethod()).thenReturn(TestTarget.class.getMethod("methodWithoutCircuitBreaker"));
        lenient().when(context.getTarget()).thenReturn(target);
        when(context.proceed()).thenReturn("result");

        final CircuitBreakerMetricsInterceptor interceptor = new CircuitBreakerMetricsInterceptor(stateTracker);
        final Object result = interceptor.collectCircuitBreakerMetrics(context);

        assertEquals("result", result);
        verify(context).proceed();
        verify(stateTracker, never()).recordStateTransition(any(), any());
    }

    @Test
    @DisplayName("collectCircuitBreakerMetrics records state transition when state changes")
    void collectCircuitBreakerMetrics_RecordsStateTransition_WhenStateChanges() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithCircuitBreaker");
        final String circuitName = TestTarget.class.getName() + "#methodWithCircuitBreaker";

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(context.proceed()).thenReturn("result");
        when(stateTracker.getPreviousState(circuitName))
            .thenReturn(CircuitBreakerState.CLOSED);

        final CircuitBreakerMetricsInterceptor interceptor = new CircuitBreakerMetricsInterceptor(stateTracker);
        final Object result = interceptor.collectCircuitBreakerMetrics(context);

        assertEquals("result", result);
        verify(stateTracker).recordStateTransition(circuitName, CircuitBreakerState.CLOSED);
    }

    @Test
    @DisplayName("collectCircuitBreakerMetrics records state transition even when state unchanged")
    void collectCircuitBreakerMetrics_RecordsStateTransition_EvenWhenStateUnchanged() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithCircuitBreaker");
        final String circuitName = TestTarget.class.getName() + "#methodWithCircuitBreaker";

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(context.proceed()).thenReturn("result");
        when(stateTracker.getPreviousState(circuitName))
            .thenReturn(CircuitBreakerState.CLOSED);

        final CircuitBreakerMetricsInterceptor interceptor = new CircuitBreakerMetricsInterceptor(stateTracker);
        final Object result = interceptor.collectCircuitBreakerMetrics(context);

        assertEquals("result", result);
        verify(stateTracker).recordStateTransition(circuitName, CircuitBreakerState.CLOSED);
    }

    @Test
    @DisplayName("collectCircuitBreakerMetrics handles exceptions and still records state")
    void collectCircuitBreakerMetrics_HandlesExceptions_AndStillRecordsState() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithCircuitBreaker");
        final String circuitName = TestTarget.class.getName() + "#methodWithCircuitBreaker";
        final RuntimeException exception = new RuntimeException("Test exception");

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(context.proceed()).thenThrow(exception);
        when(stateTracker.getPreviousState(circuitName))
            .thenReturn(CircuitBreakerState.CLOSED);

        try
        {
            final CircuitBreakerMetricsInterceptor interceptor = new CircuitBreakerMetricsInterceptor(stateTracker);
            interceptor.collectCircuitBreakerMetrics(context);
        }
        catch (final RuntimeException e)
        {
            assertEquals(exception, e);
        }

        verify(stateTracker).recordStateTransition(circuitName, CircuitBreakerState.CLOSED);
    }

    @Test
    @DisplayName("collectCircuitBreakerMetrics handles null previous state")
    void collectCircuitBreakerMetrics_HandlesNullPreviousState() throws Exception
    {
        final TestTarget target = new TestTarget();
        final Method method = TestTarget.class.getMethod("methodWithCircuitBreaker");
        final String circuitName = TestTarget.class.getName() + "#methodWithCircuitBreaker";

        when(context.getMethod()).thenReturn(method);
        lenient().when(context.getTarget()).thenReturn(target);
        when(context.proceed()).thenReturn("result");
        when(stateTracker.getPreviousState(circuitName))
            .thenReturn(null);

        final CircuitBreakerMetricsInterceptor interceptor = new CircuitBreakerMetricsInterceptor(stateTracker);
        final Object result = interceptor.collectCircuitBreakerMetrics(context);

        assertEquals("result", result);
        verify(stateTracker).recordStateTransition(circuitName, null);
    }

    // Test helper class
    @SuppressWarnings("unused")
    static class TestTarget
    {
        public String methodWithoutCircuitBreaker()
        {
            return "result";
        }

        @CircuitBreaker(delay = 1000)
        public String methodWithCircuitBreaker()
        {
            return "result";
        }
    }
}
