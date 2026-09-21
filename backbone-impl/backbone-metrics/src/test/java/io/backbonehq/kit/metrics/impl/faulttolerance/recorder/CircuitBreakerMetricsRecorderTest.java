package io.backbonehq.kit.metrics.impl.faulttolerance.recorder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CircuitBreakerMetricsRecorderTest
{
    private final MeterRegistry meterRegistry = mock(MeterRegistry.class);

    private final Counter.Builder counterBuilder = mock(Counter.Builder.class);

    private final Counter counter = mock(Counter.class);

    @Test
    @DisplayName("recordCircuitBreakerStateChange records metric with correct name and tags")
    void recordCircuitBreakerStateChange_RecordsMetric_WithCorrectNameAndTags()
    {
        // Given
        final String circuitName = "CandidateRepository.save";
        final String state = "open";

        try (MockedStatic<Counter> counterMock = mockStatic(Counter.class))
        {
            counterMock.when(() -> Counter.builder("circuit.breaker.state.changes")).thenReturn(counterBuilder);
            when(counterBuilder.tag(eq("circuit"), eq(circuitName))).thenReturn(counterBuilder);
            when(counterBuilder.tag(eq("state"), eq(state))).thenReturn(counterBuilder);
            when(counterBuilder.description(any())).thenReturn(counterBuilder);
            when(counterBuilder.register(eq(meterRegistry))).thenReturn(counter);

            final CircuitBreakerMetricsRecorder recorder = new CircuitBreakerMetricsRecorder(meterRegistry);
            recorder.recordCircuitBreakerStateChange(circuitName, state);

            counterMock.verify(() -> Counter.builder("circuit.breaker.state.changes"));
            verify(counterBuilder).tag("circuit", circuitName);
            verify(counterBuilder).tag("state", state);
            verify(counterBuilder).description("Circuit breaker state changes");
            verify(counterBuilder).register(meterRegistry);
            verify(counter).increment();
        }
    }

    @Test
    @DisplayName("recordCircuitBreakerStateChange handles all circuit breaker states")
    void recordCircuitBreakerStateChange_HandlesAllStates()
    {
        final String circuitName = "TestRepository.method";
        final String[] states = {"closed", "open", "half_open"};

        try (final MockedStatic<Counter> counterMock = mockStatic(Counter.class))
        {
            counterMock.when(() -> Counter.builder(any())).thenReturn(counterBuilder);
            when(counterBuilder.tag(any(), any())).thenReturn(counterBuilder);
            when(counterBuilder.description(any())).thenReturn(counterBuilder);
            when(counterBuilder.register(any())).thenReturn(counter);

            final CircuitBreakerMetricsRecorder recorder = new CircuitBreakerMetricsRecorder(meterRegistry);
            for (final String state : states)
            {
                recorder.recordCircuitBreakerStateChange(circuitName, state);
            }

            verify(counter, Mockito.times(3)).increment();
        }
    }

    @Test
    @DisplayName("recordCircuitBreakerStateChange handles different circuit names")
    void recordCircuitBreakerStateChange_HandlesDifferentCircuitNames()
    {
        final String[] circuitNames = {"ActorRepository.save", "DocumentUploadRepository.upload"};
        final String state = "closed";

        try (final MockedStatic<Counter> counterMock = mockStatic(Counter.class))
        {
            counterMock.when(() -> Counter.builder("circuit.breaker.state.changes")).thenReturn(counterBuilder);
            when(counterBuilder.tag(any(), any())).thenReturn(counterBuilder);
            when(counterBuilder.description(any())).thenReturn(counterBuilder);
            when(counterBuilder.register(eq(meterRegistry))).thenReturn(counter);

            final CircuitBreakerMetricsRecorder recorder = new CircuitBreakerMetricsRecorder(meterRegistry);
            for (final String circuitName : circuitNames)
            {
                recorder.recordCircuitBreakerStateChange(circuitName, state);
            }

            verify(counterBuilder).tag(eq("circuit"), eq("ActorRepository.save"));
            verify(counterBuilder).tag(eq("circuit"), eq("DocumentUploadRepository.upload"));
            verify(counterBuilder, times(2)).tag(eq("state"), eq(state));
            verify(counter, Mockito.times(2)).increment();
        }
    }

    @Test
    @DisplayName("recordCircuitBreakerStateChange uses correct metric name")
    void recordCircuitBreakerStateChange_UsesCorrectMetricName()
    {
        try (final MockedStatic<Counter> counterMock = mockStatic(Counter.class))
        {
            counterMock.when(() -> Counter.builder("circuit.breaker.state.changes")).thenReturn(counterBuilder);
            when(counterBuilder.tag(any(), any())).thenReturn(counterBuilder);
            when(counterBuilder.description(any())).thenReturn(counterBuilder);
            when(counterBuilder.register(any())).thenReturn(counter);

            final CircuitBreakerMetricsRecorder recorder = new CircuitBreakerMetricsRecorder(meterRegistry);
            recorder.recordCircuitBreakerStateChange("Test.method", "open");

            counterMock.verify(() -> Counter.builder("circuit.breaker.state.changes"));
            verify(counter).increment();
        }
    }
}
