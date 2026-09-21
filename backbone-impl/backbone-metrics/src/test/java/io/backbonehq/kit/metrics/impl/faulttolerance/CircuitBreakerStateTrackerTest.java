package io.backbonehq.kit.metrics.impl.faulttolerance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.backbonehq.kit.metrics.impl.faulttolerance.recorder.CircuitBreakerMetricsRecorder;
import io.smallrye.faulttolerance.api.CircuitBreakerState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CircuitBreakerStateTrackerTest
{
    private final CircuitBreakerMetricsRecorder recorder = mock(CircuitBreakerMetricsRecorder.class);

    private final CircuitBreakerStateFetcher stateFetcher = mock(CircuitBreakerStateFetcher.class);

    @Test
    @DisplayName("recordStateTransition does nothing when state is null")
    void recordStateTransition_DoesNothing_WhenStateIsNull()
    {
        final String circuitName = "test-circuit";
        when(stateFetcher.getCurrentState(circuitName)).thenReturn(null);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuitName, CircuitBreakerState.CLOSED);

        verify(recorder, never()).recordCircuitBreakerStateChange(any(), any());
    }

    @Test
    @DisplayName("recordStateTransition does not record when state unchanged")
    void recordStateTransition_DoesNotRecord_WhenStateUnchanged()
    {
        final String circuitName = "test-circuit";
        final CircuitBreakerState state = CircuitBreakerState.CLOSED;
        when(stateFetcher.getCurrentState(circuitName)).thenReturn(state);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuitName, state);

        verify(recorder, never()).recordCircuitBreakerStateChange(any(), any());
    }

    @Test
    @DisplayName("recordStateTransition records when state changes from CLOSED to OPEN")
    void recordStateTransition_Records_WhenStateChangesFromClosedToOpen()
    {
        final String circuitName = "test-circuit";
        when(stateFetcher.getCurrentState(circuitName))
            .thenReturn(CircuitBreakerState.OPEN);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuitName, CircuitBreakerState.CLOSED);

        verify(recorder).recordCircuitBreakerStateChange(circuitName, "open");
    }

    @Test
    @DisplayName("recordStateTransition records when state changes from OPEN to HALF_OPEN")
    void recordStateTransition_Records_WhenStateChangesFromOpenToHalfOpen()
    {
        final String circuitName = "test-circuit";
        when(stateFetcher.getCurrentState(circuitName))
            .thenReturn(CircuitBreakerState.HALF_OPEN);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuitName, CircuitBreakerState.OPEN);

        verify(recorder).recordCircuitBreakerStateChange(circuitName, "half_open");
    }

    @Test
    @DisplayName("recordStateTransition records when state changes from HALF_OPEN to CLOSED")
    void recordStateTransition_Records_WhenStateChangesFromHalfOpenToClosed()
    {
        final String circuitName = "test-circuit";
        when(stateFetcher.getCurrentState(circuitName))
            .thenReturn(CircuitBreakerState.CLOSED);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuitName, CircuitBreakerState.HALF_OPEN);

        verify(recorder).recordCircuitBreakerStateChange(circuitName, "closed");
    }

    @Test
    @DisplayName("recordStateTransition handles first invocation with null previous state")
    void recordStateTransition_HandlesFirstInvocation_WithNullPreviousState()
    {
        final String circuitName = "test-circuit";
        when(stateFetcher.getCurrentState(circuitName))
            .thenReturn(CircuitBreakerState.CLOSED);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuitName, null);

        verify(recorder, never()).recordCircuitBreakerStateChange(any(), any());
    }

    @Test
    @DisplayName("getPreviousState returns null for first invocation")
    void getPreviousState_ReturnsNull_ForFirstInvocation()
    {
        final String circuitName = "test-circuit";

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        final CircuitBreakerState result = stateTracker.getPreviousState(circuitName);

        assertNull(result);
    }

    @Test
    @DisplayName("getPreviousState returns previous state after recording")
    void getPreviousState_ReturnsPreviousState_AfterRecording()
    {
        final String circuitName = "test-circuit";
        when(stateFetcher.getCurrentState(circuitName))
            .thenReturn(CircuitBreakerState.OPEN);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuitName, CircuitBreakerState.CLOSED);
        final CircuitBreakerState result = stateTracker.getPreviousState(circuitName);

        assertEquals(CircuitBreakerState.OPEN, result);
    }

    @Test
    @DisplayName("recordStateTransition handles multiple circuit breakers independently")
    void recordStateTransition_HandlesMultipleCircuitBreakers_Independently()
    {
        final String circuit1 = "circuit-1";
        final String circuit2 = "circuit-2";
        when(stateFetcher.getCurrentState(circuit1))
            .thenReturn(CircuitBreakerState.OPEN);
        when(stateFetcher.getCurrentState(circuit2))
            .thenReturn(CircuitBreakerState.CLOSED);

        final CircuitBreakerStateTracker stateTracker = new CircuitBreakerStateTracker(recorder, stateFetcher);
        stateTracker.recordStateTransition(circuit1, CircuitBreakerState.CLOSED);
        stateTracker.recordStateTransition(circuit2, CircuitBreakerState.CLOSED);

        verify(recorder).recordCircuitBreakerStateChange(eq(circuit1), eq("open"));
        verify(recorder, never()).recordCircuitBreakerStateChange(eq(circuit2), any());
    }
}
