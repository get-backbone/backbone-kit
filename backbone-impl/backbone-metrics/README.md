# backbone-metrics

## Overview

`backbone-metrics` provides **consistent, production-ready metrics and observability primitives** for backend services.

It standardises how services emit metrics and correlation data, enabling reliable monitoring and troubleshooting.

---

## Key Features

- Micrometer-based metrics integration
- Prometheus-ready configuration
- Correlation ID propagation
- Structured logging conventions
- Low-friction instrumentation patterns

---

## Design Principles

- Metrics should be cheap, explicit, and consistent
- Correlation must work across service boundaries
- Observability is a platform concern, not an afterthought

---

## Typical Use Cases

- Service-level metrics (latency, throughput, errors)
- Cross-service request tracing
- Operational dashboards
- Alerting and SLO tracking

---

## Usage

`backbone-metrics` provides the `@ServiceMetrics` annotation for automatic metrics collection. To use it, you need to:

1. **Implement a `MetricsRecorder`** - Create a class that implements `MetricsRecorder` to define how metrics are
   recorded
2. **Annotate methods** - Use `@ServiceMetrics(YourRecorder.class)` on methods you want to instrument
3. **Return `MetricsResultIndicator`** - Methods should return types that implement `MetricsResultIndicator` for
   automatic success/failure detection

### Basic Usage

```java
// 1. Implement a MetricsRecorder
@ApplicationScoped
public class AuthMetricsRecorder implements MetricsRecorder
{
    @Inject
    MeterRegistry meterRegistry;

    @Override
    public void recordMetrics(InvocationContext context, MetricsResultIndicator indicator)
    {
        String operation = context.getMethod().getName();
        String status = indicator.success() ? "success" : "failure";

        Counter.builder("auth.operations")
            .tag("operation", operation)
            .tag("status", status)
            .register(meterRegistry)
            .increment();
    }

    @Override
    public void recordException(InvocationContext context, Exception exception)
    {
        // Record exception metrics
    }
}

// 2. Use @ServiceMetrics annotation
@ServiceMetrics(AuthMetricsRecorder.class)
public AuthResponse authenticate(String username, String password)
{
    // Your logic here
    return new AuthResponse(success, token, errorMessage);
}

// 3. Return type implements MetricsResultIndicator
public record AuthResponse(boolean success, String token, String errorMessage)
    implements MetricsResultIndicator
{
}
```

The interceptor automatically:

- Extracts the operation name from the method
- Records metrics based on the result indicator
- Handles exceptions separately
- Supports optional type categorization via the `type` parameter

---

## Examples

See: [examples/backbone-metrics](../../examples/backbone-metrics) for code examples.

See the reference implementation:

- [`ThrottleMetricsRecorder`](../backbone-metrics/src/main/java/io/backbonehq/kit/metrics/impl/domain/recorder/ThrottleMetricsRecorder.java) -
  A production-ready example of a `MetricsRecorder` implementation that records rate-limiting metrics

This example demonstrates:

- Implementing the `MetricsRecorder` interface
- Recording metrics using Micrometer
- Handling void methods (extracting data from request context)
- Recording exceptions separately
- Using metrics tags for categorization

---
