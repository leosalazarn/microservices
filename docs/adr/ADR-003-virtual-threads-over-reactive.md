# ADR-003: Virtual Threads (Project Loom) over Reactive Stack

**Status**: Accepted  
**Date**: 2026-05-15  
**Deciders**: Tech Lead  
**Driver**: Need for high-concurrency I/O handling with simple, debuggable code

## Context

The service handles blocking I/O operations (MongoDB queries, Kafka sends, HTTP calls via Eureka/REST). Under load, traditional Platform Threads per request consumes ~1MB stack per thread — limiting concurrent capacity. Options:

1. **Reactive Stack** (WebFlux, R2DBC): Non-blocking throughout; high learning curve
2. **Virtual Threads** (Project Loom): Blocking code runs on lightweight threads; same simple imperative style

## Decision

Use **Java 21 Virtual Threads** with traditional **Spring Boot MVC + blocking JDBC/drivers**.

```java
// Virtual Threads are the default in Spring Boot 3.4+
spring.threads.virtual.enabled=true
```

### Key implementation choices

- **No WebFlux** — standard `@RestController` with blocking calls
- **Tomcat** as the servlet container (configured to use virtual threads)
- **MongoDB driver** — blocking driver, wrapped in virtual threads
- **Kafka producer** — blocking `KafkaTemplate.send()`, runs on virtual threads

## Consequences

### Positive
- Simple, debuggable code — no `Mono`/`Flux` chains
- Familiar programming model — any Java developer can contribute
- Efficient resource usage — virtual threads cost ~1KB vs 1MB+ for platform threads
- No framework lock-in — standard Spring MVC, swap containers anytime

### Negative
- Pinned threads — synchronized blocks or native calls can pin virtual threads to platform threads (mitigated by avoiding synchronized in hot paths)
- Garbage collection — virtual threads don't reduce object allocation; ZGC still needed for low-pause GC
- Monitoring — older monitoring tools may not differentiate virtual from platform threads (mitigated by JDK 21+ tools support)

## Alternatives Considered

### Spring WebFlux (Reactive Stack)
- **Rejected**: Steep learning curve for the team; harder to debug; incompatible with some blocking libraries
- Benefits (backpressure, zero allocation for no-op) don't justify the complexity for this project's throughput requirements

### Traditional Platform Threads with Thread Pool Tuning
- **Rejected**: At 1MB+ per thread, 1000 concurrent requests = 1GB+ of stack memory
- Virtual threads achieve the same concurrency at ~1MB total per 1000 threads

## Related ADRs
- This is a standalone decision — no direct ADR dependencies
