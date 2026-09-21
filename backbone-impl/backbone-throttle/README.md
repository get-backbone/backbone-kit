# backbone-throttle

## Overview

`backbone-throttle` provides **deterministic, production-grade rate limiting and throttling** for HTTP services.

It is designed for zero-trust environments and makes rate limiting **explicit, testable, and predictable** under load.

---

## Key Features

- Deterministic rate limiting using Bucket4j
- Explicit separation of authenticated vs unauthenticated capacity
- Request-level enforcement (no implicit or hidden state)
- Quarkus-native integration
- Fully configurable and test-friendly

---

## Design Principles

- Rate limits are **capacity-based**, not best-effort
- Enforcement happens early in the request lifecycle
- Configuration is explicit and override-friendly
- No reliance on global mutable state

---

## Typical Use Cases

- Protecting public APIs from abuse
- Enforcing fair usage across consumers
- Zero-trust perimeter protection
- Load-shedding under extreme traffic

---

## Usage

`backbone-throttle` provides a `RateLimiter` abstraction and a reference filter implementation. You have two options:

### Option 1: Use the Reference Filter (Recommended)

Enable the provided `ReferenceRateLimitingFilter` by setting:

```properties
backbone.rate-limit.reference.enabled=true
```

The filter is automatically wired in and requires no additional code. It:
- Runs before authentication to protect all endpoints
- Uses `HttpHeaderRateLimitKeyStrategy` to extract rate limit keys
- Returns 429 (Too Many Requests) when limits are exceeded

### Option 2: Implement a Custom Filter

If you need custom behavior (e.g., additional logging, metrics, or key resolution), implement your own filter following the pattern of the reference implementation.

### Configuration

Configure rate limits in `application.properties`:

```properties
# Capacity per minute
rate-limit.authenticated-capacity-per-minute=100000
rate-limit.unauthenticated-capacity-per-minute=10000

# Refill rate per second
rate-limit.authenticated-refill-per-second=10000
rate-limit.unauthenticated-refill-per-second=1000
```

---

## Examples

See: [examples/backbone-throttle](../../examples/backbone-throttle) for configuration examples.

See the reference implementation:
- [`ReferenceRateLimitingFilter`](../backbone-throttle/src/main/java/io/backbonehq/kit/throttle/impl/reference/ReferenceRateLimitingFilter.java) -
  A production-ready rate limiting filter implementation

This example demonstrates:
- Implementing a RESTEasy Reactive request filter
- Using the `RateLimiter` abstraction
- Extracting rate limit keys from request headers
- Handling rate limit violations with proper HTTP responses (429 status)
- Adding rate limit headers to responses
- Integration with the rate limiting infrastructure

See also:
- [`RateLimitingIT`](../backbone-throttle/src/test/java/io/backbonehq/kit/throttle/impl/infrastructure/RateLimitingIT.java) -
  Integration tests demonstrating throttling enforcement

---
