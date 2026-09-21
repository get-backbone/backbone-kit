package io.backbonehq.kit.metrics.impl.faulttolerance;

import io.backbonehq.kit.metrics.impl.faulttolerance.recorder.CircuitBreakerMetricsRecorder;
import io.smallrye.faulttolerance.api.CircuitBreakerState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jboss.logging.Logger;

/**
 * Tracks circuit breaker state transitions and records metrics when state changes occur.
 *
 * <p>This class maintains a cache of previous states per circuit breaker and detects
 * transitions (closed → open, open → half_open, half_open → closed).
 */
@ApplicationScoped
public final class CircuitBreakerStateTracker
{
    private static final Logger LOGGER = Logger.getLogger(CircuitBreakerStateTracker.class);

    private static final Map<CircuitBreakerState, String> STATE_LABELS = Map.of(
        CircuitBreakerState.CLOSED, "closed",
        CircuitBreakerState.OPEN, "open",
        CircuitBreakerState.HALF_OPEN, "half_open"
    );

    private final ConcurrentMap<String, CircuitBreakerState> previousStates = new ConcurrentHashMap<>();

    private final CircuitBreakerMetricsRecorder recorder;

    private final CircuitBreakerStateFetcher stateFetcher;

    @Inject
    public CircuitBreakerStateTracker(final CircuitBreakerMetricsRecorder recorder, final CircuitBreakerStateFetcher stateFetcher)
    {
        this.recorder = recorder;
        this.stateFetcher = stateFetcher;
    }

    /**
     * Records a circuit breaker state transition if the state has changed.
     *
     * @param circuitName The circuit breaker name
     * @param stateBefore The state before method execution (may be null on first invocation)
     */
    public void recordStateTransition(final String circuitName, final CircuitBreakerState stateBefore)
    {
        final CircuitBreakerState stateAfter = stateFetcher.getCurrentState(circuitName);
        if (stateAfter == null)
        {
            return;
        }

        previousStates.put(circuitName, stateAfter);
        if (stateBefore == null)
        {
            // First invocation - no previous state to compare, so no transition to record
            return;
        }
        if (stateBefore == stateAfter)
        {
            return;
        }

        logAndRecord(circuitName, stateBefore, stateAfter);
    }

    private void logAndRecord(final String circuitName, final CircuitBreakerState stateBefore, final CircuitBreakerState stateAfter)
    {
        LOGGER.debugf("Circuit breaker %s state transition: %s → %s",
            circuitName, stateToLabel(stateBefore), stateToLabel(stateAfter));
        recorder.recordCircuitBreakerStateChange(circuitName, stateToLabel(stateAfter));
    }

    /**
     * Gets the previous state for a circuit breaker (if any).
     *
     * @param circuitName The circuit breaker name
     * @return The previous state, or null if this is the first invocation
     */
    public CircuitBreakerState getPreviousState(final String circuitName)
    {
        return previousStates.get(circuitName);
    }

    private static String stateToLabel(final CircuitBreakerState state)
    {
        if (state == null)
        {
            return "unknown";
        }
        return STATE_LABELS.getOrDefault(state, "unknown");
    }
}
