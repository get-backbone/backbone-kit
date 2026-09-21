# Backbone Kit

[![Lines of Code](https://img.shields.io/endpoint?url=https%3A%2F%2Ftokei.kojix2.net%2Fbadge%2Fgithub%2Fget-backbone%2Fbackbone-kit%2Flines)](https://tokei.kojix2.net/github/get-backbone/backbone-kit)
[![Top Language](https://img.shields.io/endpoint?url=https%3A%2F%2Ftokei.kojix2.net%2Fbadge%2Fgithub%2Fget-backbone%2Fbackbone-kit%2Flanguage)](https://tokei.kojix2.net/github/get-backbone/backbone-kit)
[![Languages](https://img.shields.io/endpoint?url=https%3A%2F%2Ftokei.kojix2.net%2Fbadge%2Fgithub%2Fget-backbone%2Fbackbone-kit%2Flanguages)](https://tokei.kojix2.net/github/get-backbone/backbone-kit)
[![Code to Comment](https://img.shields.io/endpoint?url=https%3A%2F%2Ftokei.kojix2.net%2Fbadge%2Fgithub%2Fget-backbone%2Fbackbone-kit%2Fratio)](https://tokei.kojix2.net/github/get-backbone/backbone-kit)
![License](https://img.shields.io/github/license/get-backbone/backbone-kit)
![Last Commit](https://img.shields.io/github/last-commit/get-backbone/backbone-kit)

[![Quarkus](https://img.shields.io/badge/Quarkus-v3.36.1-blue?logo=quarkus)](https://quarkus.io/)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg)](https://conventionalcommits.org)
[![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)

### CI Status

[![00 🧩 Hygiene checks](https://github.com/get-backbone/backbone-kit/actions/workflows/00-hygiene-check.yml/badge.svg)](https://github.com/get-backbone/backbone-kit/actions/workflows/00-hygiene-check.yml)  
[![01 🚧 Build and test](https://github.com/get-backbone/backbone-kit/actions/workflows/01-build-test.yml/badge.svg)](https://github.com/get-backbone/backbone-kit/actions/workflows/01-build-test.yml)  
[![02 🔎 Static analysis](https://github.com/get-backbone/backbone-kit/actions/workflows/02-static-analysis.yml/badge.svg)](https://github.com/get-backbone/backbone-kit/actions/workflows/02-static-analysis.yml)  
[![03 👊🏽 Auto version bump](https://github.com/get-backbone/backbone-kit/actions/workflows/03-release-bump.yml/badge.svg)](https://github.com/get-backbone/backbone-kit/actions/workflows/03-release-bump.yml)  
[![04 📦 Publish packages](https://github.com/get-backbone/backbone-kit/actions/workflows/04-publish-packages.yml/badge.svg)](https://github.com/get-backbone/backbone-kit/actions/workflows/04-publish-packages.yml)  
[![51 🔎 Code coverage](https://github.com/get-backbone/backbone-kit/actions/workflows/51-code-coverage.yml/badge.svg)](https://github.com/get-backbone/backbone-kit/actions/workflows/51-code-coverage.yml)  

### Code coverage

[![codecov](https://codecov.io/github/get-backbone/backbone-kit/graph/badge.svg?token=RP8Z2NWG9L)](https://app.codecov.io/github/get-backbone/backbone-kit)

---

## Overview

**Backbone Kit** is a collection of infrastructure components that support 👉 **[Backbone](https://backbonehq.io)**.

It provides Quarkus-based components for security, observability, and cross-cutting service concerns — without prescribing domain logic.

Backbone Kit demonstrates the following capabilities that continue with the full Backbone Platform: 
- architectural composure and separation of concerns
- implementation of microservice cross-cutting concerns
- disciplined coding standards, documentation, and test organisation

---

## What Backbone Kit Provides

### 🔒 Security & Protection

- **Rate Limiting & Throttling** (`backbone-throttle`)
  - Deterministic rate limiting with clear separation of authenticated vs. unauthenticated capacity.
  - [Documentation →](backbone-impl/backbone-throttle/README.md)

### 📈 Observability

- **Metrics Framework** (`backbone-metrics`)
  - Micrometer integration with Prometheus-ready metrics.
  - Service, circuit breaker, and database performance recorders
  - [Documentation →](backbone-impl/backbone-metrics/README.md)

- **Prometheus Remote Write** (`backbone-observability-api`, `backbone-observability-aws`)
  - PRW 1.0 encoding from Micrometer `MetricSnapshots` (no text scrape parsing).
  - Scheduled push to Amazon Managed Prometheus with SigV4-signed remote write.
  - Profile-gated via `@LookupIfProperty` — enable only where AMP is configured.
  - [Documentation →](backbone-impl/backbone-observability-aws/README.md)

- **Distributed Tracing (AWS)** (`backbone-observability-aws`)
  - OTLP protobuf export to AWS X-Ray with SigV4 signing.
  - Works with Quarkus OpenTelemetry; no collector sidecar required.
  - [Documentation →](backbone-impl/backbone-observability-aws/README.md)

- **AWS Signed HTTP Transport** (`backbone-http-aws`)
  - Reusable SigV4 signing for outbound HTTP (AMP, X-Ray OTLP, and other AWS endpoints).
  - [Documentation →](backbone-impl/backbone-http-aws/README.md)

- **Health Checks** (`backbone-health-aws`)
  - Liveness and readiness probe support for AWS environments.
  - [Documentation →](backbone-impl/backbone-health-aws/README.md)

- **Structured Logging** (`backbone-logging`)
  - Opt-in method entry logging via `@LogMethodEntry`.
  - HTTP correlation ID propagation (MDC and headers).
  - Sensitive data masking in log output.
  - [Documentation →](backbone-impl/backbone-logging/README.md)

### 🧱 Platform Utilities

- **Common Utilities** (`backbone-common`)
  - Validation, error handling, and shared REST primitives.
  - [Documentation →](backbone-impl/backbone-common/README.md)

Each module is independently usable and documented.

---

## Getting Started

Add the required modules to your project:

```xml
<dependency>
  <groupId>io.backbonehq</groupId>
  <artifactId>backbone-throttle</artifactId>
  <version>2.0.0</version>
</dependency>
```

Each module includes focused documentation and examples.

---

**📚 Documentation**
- [Examples →](examples/)
- [Code Quality & CI Enforcement](CODE_QUALITY.md)

---

## Relationship to Backbone

Backbone Kit is the cross-cutting foundation — rate limiting, observability, structured logging, and the other plumbing most services need before they ship anything a customer or auditor cares about.

Backbone is the same foundation, extended: domain services (auth, users, audit, documents, notifications, BFF), zero-trust security, infrastructure-as-code, and compliance-mapped evidence — licensed and forkable, deployed into your own AWS account.

Backbone Kit is useful either way. If you're building the rest yourself with Quarkus, it's a head start worth getting right the first time. If you'd rather not spend runway rebuilding identity, observability, security and scaling from scratch, then Backbone is the more complete starting point.

---

## Support

Backbone Kit is open-source and community-supported — issues and PRs welcome.

For teams weighing DIY against a complete, licensed foundation, or wanting architectural input on standing up the full stack,
see 👉 [Backbone](https://backbonehq.io).

---

## License

Backbone Kit is licensed under the [MIT License](LICENSE).
