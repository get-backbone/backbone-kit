package io.backbonehq.kit.metrics.impl.faulttolerance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import io.smallrye.faulttolerance.api.CircuitBreakerState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CircuitBreakerStateFetcherTest
{
    private final CircuitBreakerMaintenance circuitBreakerMaintenance = mock(CircuitBreakerMaintenance.class);

    @Test
    @DisplayName("getCurrentState returns state when circuit breaker exists")
    void getCurrentState_ReturnsState_WhenCircuitBreakerExists()
    {
        final String circuitName = "test-circuit";
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenReturn(CircuitBreakerState.OPEN);

        final CircuitBreakerStateFetcher stateFetcher = new CircuitBreakerStateFetcher(circuitBreakerMaintenance);
        final CircuitBreakerState result = stateFetcher.getCurrentState(circuitName);

        assertEquals(CircuitBreakerState.OPEN, result);
    }

    @Test
    @DisplayName("getCurrentState returns null when circuit breaker returns null")
    void getCurrentState_ReturnsNull_WhenCircuitBreakerReturnsNull()
    {
        final String circuitName = "test-circuit";
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenReturn(null);

        final CircuitBreakerStateFetcher stateFetcher = new CircuitBreakerStateFetcher(circuitBreakerMaintenance);
        final CircuitBreakerState result = stateFetcher.getCurrentState(circuitName);

        assertNull(result);
    }

    @Test
    @DisplayName("getCurrentState throws IllegalStateException when circuit breaker doesn't exist")
    void getCurrentState_ThrowsIllegalStateException_WhenCircuitBreakerDoesNotExist()
    {
        final String circuitName = "non-existent-circuit";
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenThrow(new RuntimeException("Circuit breaker 'non-existent-circuit' doesn't exist"));

        final CircuitBreakerStateFetcher stateFetcher = new CircuitBreakerStateFetcher(circuitBreakerMaintenance);
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> stateFetcher.getCurrentState(circuitName));

        assertEquals(
            "Circuit breaker 'non-existent-circuit' is not accessible through CircuitBreakerMaintenance. " + "Circuit breakers must be explicitly named using @CircuitBreakerName. Original error: " + "Circuit breaker 'non-existent-circuit' doesn't exist",
            exception.getMessage());
    }

    @Test
    @DisplayName("getCurrentState throws IllegalStateException for other exceptions")
    void getCurrentState_ThrowsIllegalStateException_ForOtherExceptions()
    {
        final String circuitName = "test-circuit";
        final String errorMessage = "Unexpected error occurred";
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenThrow(new RuntimeException(errorMessage));

        final CircuitBreakerStateFetcher stateFetcher = new CircuitBreakerStateFetcher(circuitBreakerMaintenance);
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> stateFetcher.getCurrentState(circuitName));

        assertEquals("Could not get circuit breaker state for 'test-circuit': " + errorMessage,
            exception.getMessage());
    }

    @Test
    @DisplayName("getCurrentState handles all circuit breaker states")
    void getCurrentState_HandlesAllCircuitBreakerStates()
    {
        final String circuitName = "test-circuit";

        final CircuitBreakerStateFetcher stateFetcher = new CircuitBreakerStateFetcher(circuitBreakerMaintenance);

        // Test CLOSED
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenReturn(CircuitBreakerState.CLOSED);
        assertEquals(CircuitBreakerState.CLOSED, stateFetcher.getCurrentState(circuitName));

        // Test OPEN
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenReturn(CircuitBreakerState.OPEN);
        assertEquals(CircuitBreakerState.OPEN, stateFetcher.getCurrentState(circuitName));

        // Test HALF_OPEN
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenReturn(CircuitBreakerState.HALF_OPEN);
        assertEquals(CircuitBreakerState.HALF_OPEN, stateFetcher.getCurrentState(circuitName));
    }

    @Test
    @DisplayName("getCurrentState preserves exception cause")
    void getCurrentState_PreservesExceptionCause()
    {
        final String circuitName = "test-circuit";
        final RuntimeException originalException = new RuntimeException("Original error");
        when(circuitBreakerMaintenance.currentState(circuitName))
            .thenThrow(originalException);

        final CircuitBreakerStateFetcher stateFetcher = new CircuitBreakerStateFetcher(circuitBreakerMaintenance);
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> stateFetcher.getCurrentState(circuitName));

        assertEquals(originalException, exception.getCause());
    }
}
