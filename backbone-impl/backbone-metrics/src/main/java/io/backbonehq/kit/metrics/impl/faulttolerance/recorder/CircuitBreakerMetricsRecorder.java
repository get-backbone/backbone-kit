package io.backbonehq.kit.metrics.impl.faulttolerance.recorder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public final class CircuitBreakerMetricsRecorder
{
    private final MeterRegistry meterRegistry;

    @Inject
    public CircuitBreakerMetricsRecorder(final MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Records a circuit breaker state change.
     *
     * @param circuitName The name of the circuit breaker
     * @param state       The new state (e.g., "open", "closed", "half_open")
     */
    public void recordCircuitBreakerStateChange(final String circuitName, final String state)
    {
        Counter.builder("circuit.breaker.state.changes")
            .tag("circuit", circuitName)
            .tag("state", state)
            .description("Circuit breaker state changes")
            .register(meterRegistry)
            .increment();
    }
}
