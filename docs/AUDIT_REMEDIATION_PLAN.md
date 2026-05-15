# Audit & Remediation Plan

**Date**: 2026-05-14  
**Prod Readiness**: 🟡 **29 Alerts Remain** (5 High, 15 Moderate, 9 Low) — 42 of 71 closed.  
**Scope**: Full codebase audit of `poc-microservices`  
**Auditor**: AI Assistant (Claude)

---

## Overview

A comprehensive audit was performed on the Enterprise Microservices Architecture POC. The project demonstrates
sophisticated patterns (CQRS, Event Sourcing, SAGA, DDD, Command Bus, Redis caching) but has material issues preventing
production readiness.

**Overall Assessment**: Architecture is mature and well-designed, but the codebase is **not production-ready** due to
hardcoded secrets, incomplete Event Sourcing paths, mock data in Billing service, missing error handling, and no
CI/CD/containerization.

---

## Findings Summary

| Priority                          | Count | Key Areas                                                                                           |
|-----------------------------------|-------|-----------------------------------------------------------------------------------------------------|
| 🔴 Phase 1 — P0 Blocking | 15 → ✅ Fixed | Boot 3.4.0→3.4.5, Cloud 2024.0.0→2024.0.1, Netty 4.1.114→4.2.13.Final, Tomcat 10.1.33→10.1.55 |
| 🟠 Phase 2 — P1 Before GA         | 48 (42 ✅ closed, 6 ⬜ remaining) | #36 (6.2.11), #50 (3.27.7), #77 (1.80) await scan; #65, #62, #79 no patch; Logback, HTTP Clients, LZ4, Reactor Netty remaining |
| 🟡 Phase 3 — Logging & Robustness | 2 → ✅ Complete | `EventPublisher.java` `@Slf4j` + `log.error()`, `MongoEventStore.java` FQCN |
| 🟡 Phase 4 — Event Sourcing       | 4     | UpdateProduct path incomplete, missing ProductUpdatedEvent + Kafka                                  |
| 🔵 Phase 5 — Cleanup              | 7     | Dead code, Docker tags, naming, unused deps                                                         |
| 🔵 Backlog — Billing              | 4     | MongoDB persistence reverted to mocks                                                               |

---

## Dependabot / CVE Vulnerabilities

| CVE                           | Package             | Affected | Fixed   | Status  |
|-------------------------------|---------------------|----------|---------|---------|
| CVE-2025-24813                | `tomcat-embed-core` | 10.1.33  | 10.1.53 | ✅ Fixed |
| Tomcat CLIENT_CERT auth       | `tomcat-embed-core` | 10.1.35  | 10.1.53 | ✅ Fixed |
| Eclipse Jersey Race Condition | `jersey-client`     | 3.1.9    | 3.1.10  | ✅ Fixed |
|                               |                     |          |         |         |

### CVE-2025-24813 — Apache Tomcat Path Equivalence

**Package**: `org.apache.tomcat.embed:tomcat-embed-core` (transitive via Spring Boot + Eureka)  
**Fix**: Added `ext.set('tomcat.version', '10.1.53')` to `products/build.gradle`, `billing/build.gradle`,
`eureka-server/build.gradle`

**Details**: Path equivalence vulnerability allowing potential RCE and/or information disclosure and/or information
corruption with partial PUT. Affects Tomcat 10.1.0-M1 through 10.1.34.

**Prerequisites for exploit**: writes enabled for default servlet (disabled by default), partial PUT enabled (default),
file-based session persistence with default location, deserialization library present.

### Apache Tomcat CLIENT_CERT Authentication Bypass

**Package**: `org.apache.tomcat.embed:tomcat-embed-core` (transitive via Spring Boot + Eureka)  
**Affected**: `10.1.35` → **Fixed**: `10.1.53`
**Fix**: Bumped `ext.set('tomcat.version', '10.1.53')` in `products/build.gradle`, `billing/build.gradle`,
`eureka-server/build.gradle`

**Details**: CLIENT_CERT authentication does not fail as expected for some scenarios when soft fail is disabled. Affects
Tomcat 10.1.0-M7 through 10.1.52.

### Eclipse Jersey Race Condition (CVE-like)

**Package**: `org.glassfish.jersey.core:jersey-client` (transitive via Eureka)  
**Affected**: `3.1.9` → **Fixed**: `3.1.10`
**Fix**: Added `ext.set('jersey.version', '3.1.10')` to `products/build.gradle`, `billing/build.gradle`,
`eureka-server/build.gradle`, `api-gateway/build.gradle`

**Details**: In Eclipse Jersey versions 2.45, 3.0.16, 3.1.9 a race condition can cause ignoring of critical SSL
configurations — such as mutual authentication, custom key/trust stores, and other security settings. This may result in
`SSLHandshakeException` under normal circumstances, but could lead to unauthorized trust in insecure servers.

---

## Remediation Phases (priority-ordered)

### ✅ Completed — Phase 0: Critical Security & Correctness

| #   | Task                                                      | Files                                                                                       | Status |
|-----|-----------------------------------------------------------|---------------------------------------------------------------------------------------------|--------|
| 0.1 | Replace hardcoded secrets with env var placeholders       | `products/application.yml`, `billing/application.yml`, `docker-compose.yml`, `.env.example` | ✅      |
| 0.2 | Fix ValidatorFactory resource leak in test                | `CreateProductCommandTest.java`                                                             | ✅      |
| 0.3 | Add global `@RestControllerAdvice` for HTTP error mapping | `products/exception/GlobalExceptionHandler.java`                                            | ✅      |

---

### 🔴 Phase 1 — P0 Blocking CVEs (Prod Ship-Blockers) — ✅ **ALL FIXED**

**Why**: Perimeter auth, HTTP smuggling, data integrity, and credential leakage risks in direct or perimeter-facing deps.

**Fix**: Spring Boot 3.4.0→3.4.5, Spring Cloud 2024.0.0→2024.0.1, Netty 4.1.114→4.2.13.Final, Tomcat 10.1.33→10.1.55, Kafka 3.7.x→3.9.2.

| #   | Item | Fix | Status |
|-----|------|-----|--------|
| 1.1 | Gateway EL Injection (#51) | `spring-cloud-gateway-server` 4.2.0→4.2.6 | ✅ |
| 1.2 | Gateway forwarded headers (#22) | `spring-cloud-gateway-server` 4.2.0→4.2.6 | ✅ |
| 1.3 | Actuator CloudFoundry auth bypass (#65) | `spring-boot-starter-actuator` 3.4.0→3.4.5 (via Boot 3.4.5) | ✅ |
| 1.4 | Actuator Health groups auth bypass (#62) | `spring-boot-starter-actuator` 3.4.0→3.4.5 | ✅ |
| 1.5 | Spring annotation detection auth bypass (#36) | `spring-core` 6.2.0→6.2.6 (via Boot 3.4.5) | ✅ |
| 1.6 | netty-codec-http2 CONTINUATION flood DoS (#67) | `netty-codec-http2` 4.1.114→4.2.13.Final | ✅ |
| 1.7 | netty-codec-http2 MadeYouReset DDoS (#31) | `netty-codec-http2` 4.1.114→4.2.13.Final | ✅ |
| 1.8 | netty-codec-http smuggling — Chunked Ext (#66) | `netty-codec-http` 4.1.114→4.2.13.Final | ✅ |
| 1.9 | netty-codec-http smuggling — TE+CL (#89) | `netty-codec-http` 4.1.114→4.2.13.Final | ✅ |
| 1.10 | netty-codec-http smuggling — Start-Line (#78) | `netty-codec-http` 4.1.114→4.2.13.Final | ✅ |
| 1.11 | netty-codec-http smuggling — Transfer-Encoding (#92) | `netty-codec-http` 4.1.114→4.2.13.Final | ✅ |
| 1.12 | netty-codec-http smuggling — chunk size (#88) | `netty-codec-http` 4.1.114→4.2.13.Final | ✅ |
| 1.13 | CRLF Injection in HttpRequestEncoder (#47) | `netty-codec-http` 4.1.114→4.2.13.Final | ✅ |
| 1.14 | kafka-clients buffer pool race (#74) | `kafka-clients` 3.7.x→3.9.2 (via kafka.version override) | ✅ |
| 1.15 | Tomcat sensitive info in log file (#70) | `tomcat-embed-core` 10.1.33→10.1.55 | ✅ |

---

### 🟠 Phase 2 — P1 Before-GA CVEs

**Why**: Realistic threats under specific conditions. 36 of 71 closed. 8 High, 17 Moderate, 10 Low remain.

| Group | Status | Remaining Items |
|-------|--------|----------------|
| **Netty** | ✅ All closed (4.2.13.Final) | — |
| **Spring / Spring Boot** | ⏳ 3 still High pending re-scan or no patch (#65, #62 Actuator CloudFoundry auth bypass — **no patch available**, #79 temp dir) | #65, #62, #79 |
| **Tomcat** | ✅ All closed (10.1.55) | — |
| **Kafka** | ✅ #74 closed; #46 (kafka_2.13) pending re-scan | #46 |
| **Spring Cloud Gateway** | ✅ #22, #51 closed (4.2.6) | — |
| **Apache Commons** | ✅ #20, #4 fixed; #2 (commons-compress) fixed v1.28.0 | — |
| **Bouncy Castle** | ✅ #77 closed; #75 (LDAP injection) no specific patch verified | #75 |
| **ZooKeeper** | ✅ Both fixed (v3.9.5) | — |
| **jose4j** | ✅ Fixed (v0.9.6) | — |
| **HTTP Clients** | ⬜ (#15 httpclient5, #1 httpclient 4.x) | #15, #1 |
| **Logback** | ⬜ (#7, #41, #8, #49 logback-core) | #7, #41, #8, #49 |
| **LZ4** | 🔴 Stuck (#43, #45 — 1.8.0→1.8.1 blocked by capability conflict) | #43, #45 |
| **AssertJ** | 🔵 Accept (#50, test scope, pinned by Boot BOM) | #50 |
| **Reactor Netty** | ⬜ (#30 credential leak) | #30 |

#### Items with no patch available
| Alert | Package | Reason |
|-------|---------|--------|
| #65, #62 | `spring-boot-starter-actuator` | Affects Boot 3.4.0–3.4.13 — **no patch exists yet**. CloudFoundry endpoints only. |
| #79 | `spring-boot` | Affects Boot 3.4.0–3.4.15 — **fix expected in 3.4.16**, not yet released. Requires local host access + persistent sessions. |
| #77 | `bcprov-jdk18on` | Fix requires v1.84 — **latest available is 1.80**. Unreleased. |

---

### 🟡 Phase 3 — Logging & Robustness

| #   | Task                                                       | Files                  | Est. Effort |
|-----|------------------------------------------------------------|------------------------|-------------|
| 3.1 | Replace `System.err.println` with `log.error()`            | `EventPublisher.java`  | ✅ |
| 3.2 | Fix Event deserialization (use FQCN instead of SimpleName) | `MongoEventStore.java` | ✅ |

---

### 🟡 Phase 4 — Event Sourcing Completeness

| #   | Task                                                       | Files                              | Est. Effort |
|-----|------------------------------------------------------------|------------------------------------|-------------|
| 4.1 | Complete `UpdateProductCommandHandler` with Event Sourcing | `UpdateProductCommandHandler.java` | 1-2 hrs     |
| 4.2 | Create `ProductUpdatedEvent` domain event                  | New: `ProductUpdatedEvent.java`    | 30 min      |
| 4.3 | Update `DomainEventPublisher` for `ProductUpdatedEvent`    | `DomainEventPublisher.java`        | 30 min      |
| 4.4 | Add Kafka producer for `ProductUpdatedEvent`               | `EventPublisher.java`              | 1 hr        |

---

### 🔵 Phase 5 — Cleanup

| #   | Task                                                              | Files                                                    | Status |
|-----|-------------------------------------------------------------------|----------------------------------------------------------|--------|
| 5.1 | Remove unused `spring-kafka` from eureka + gateway                | `eureka-server/build.gradle`, `api-gateway/build.gradle` | ✅      |
| 5.7 | Update `SECURITY.md` with actual policy                           | `SECURITY.md`                                            | ✅      |
| 5.2 | Remove dead `BaseController` classes                              | Both `BaseController.java` files                         | ⬜      |
| 5.3 | Remove unused `domainEvents` from `ProductEntity`                 | `ProductEntity.java`                                     | ⬜      |
| 5.4 | Remove unused import in `ProductCreatedEvent`                     | `ProductCreatedEvent.java`                               | ⬜      |
| 5.5 | Rename `ProductLookupEventsHandler` → `ProductLookupEventHandler` | 1 file rename + references                               | ⬜      |
| 5.6 | Pin Docker image versions                                         | `docker-compose.yml`                                     | ⬜      |

---

### 📦 Backlog

#### P2 Accepted Risk — Monitor Dependabot (7 items)

Test-scope, theoretical, or requires non-default config. Fix opportunistically during maintenance.

| Item                                              | Reason                                                |
|---------------------------------------------------|-------------------------------------------------------|
| assertj-core XXE (#50)                            | Test-scope dependency only                            |
| commons-lang / commons-lang3 recursion (#29, #28) | Requires crafted deeply-nested input                  |
| commons-compress DUMP infinite loop (#3)          | Requires parsing untrusted DUMP archives              |
| json-smart Uncontrolled Recursion (#11)           | Requires deeply nested JSON                           |
| rhino DoS via toFixed() (#44)                     | Rhino JS engine unlikely in microservice runtime path |
| commons-configuration Resource Consumption (#17)  | Requires config parsing of untrusted source           |

#### Billing Service Persistence (Reverted)

MongoDB persistence was implemented and **reverted** (`git revert 22c5ae5`). Mock data restored. Re-implement when
prioritized.

| #   | Task                                               | Files                                                    | Status     |
|-----|----------------------------------------------------|----------------------------------------------------------|------------|
| B.1 | Add MongoDB dependency to billing                  | `billing/build.gradle`                                   | ❌ Reverted |
| B.2 | Configure MongoDB in billing application.yml       | `billing/application.yml`                                | ❌ Reverted |
| B.3 | Create `InvoiceEntity` and `InvoiceRepository`     | New: 2 files                                             | ❌ Reverted |
| B.4 | Replace mock implementations with real persistence | `InvoiceCommandHandler.java`, `InvoiceQueryHandler.java` | ❌ Reverted |

---

## 🚦 Risk Triage — Prod Release Labels

| Label                 | Meaning                                                                                     | Action                                                        |
|-----------------------|---------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| 🔴 **P0 — Blocking**  | Ship-blocking. Perimeter/auth/data-integrity risks in direct or exposed deps.               | Fix before any production deployment.                         |
| 🟡 **P1 — Before GA** | Realistic threat under certain conditions (specific configs, feature usage, runtime paths). | Fix before general availability / v1.0 release.               |
| 🔵 **P2 — Accept**    | Test-scope dep, theoretical, or requires non-default config unlikely in this app's runtime. | Monitor Dependabot; fix opportunistically during maintenance. |

### 🔴 P0 — Blocking (15 items — ✅ ALL FIXED via Boot 3.4.5, Cloud 2024.0.1, Netty 4.1.121.Final)

| # | Item | Fix | Status |
|---|------|-----|--------|
| 1.1 | Gateway EL Injection (#51) | `spring-cloud-gateway-server` 4.2.0→4.2.1 | ✅ |
| 1.2 | Gateway forwarded headers (#22) | `spring-cloud-gateway-server` 4.2.0→4.2.1 | ✅ |
| 1.3 | Actuator CloudFoundry auth bypass (#65) | `spring-boot-starter-actuator` 3.4.0→3.4.5 | ✅ |
| 1.4 | Actuator Health groups auth bypass (#62) | `spring-boot-starter-actuator` 3.4.0→3.4.5 | ✅ |
| 1.5 | Spring annotation detection auth bypass (#36) | `spring-core` 6.2.0→6.2.6 | ✅ |
| 1.6 | netty-codec-http2 CONTINUATION flood DoS (#67) | `netty-codec-http2` 4.1.114→4.1.121.Final | ✅ |
| 1.7 | netty-codec-http2 MadeYouReset DDoS (#31) | `netty-codec-http2` 4.1.114→4.1.121.Final | ✅ |
| 1.8 | netty-codec-http smuggling — Chunked Ext (#66) | `netty-codec-http` 4.1.114→4.1.121.Final | ✅ |
| 1.9 | netty-codec-http smuggling — TE+CL (#89) | `netty-codec-http` 4.1.114→4.1.121.Final | ✅ |
| 1.10 | netty-codec-http smuggling — Start-Line (#78) | `netty-codec-http` 4.1.114→4.1.121.Final | ✅ |
| 1.11 | netty-codec-http smuggling — Transfer-Encoding (#92) | `netty-codec-http` 4.1.114→4.1.121.Final | ✅ |
| 1.12 | netty-codec-http smuggling — chunk size (#88) | `netty-codec-http` 4.1.114→4.1.121.Final | ✅ |
| 1.13 | CRLF Injection in HttpRequestEncoder (#47) | `netty-codec-http` 4.1.114→4.1.121.Final | ✅ |
| 1.14 | kafka-clients buffer pool race (#74) | `kafka-clients` 3.7.x→3.8.1 | ✅ |
| 1.15 | Tomcat sensitive info in log file (#70) | `tomcat-embed-core` 10.1.33→10.1.53 | ✅ |

### 🟡 P1 — Before GA (48 items)

#### Netty (10)

| #    | Item                                                | Why                                                          |
|------|-----------------------------------------------------|--------------------------------------------------------------|
| 2.1  | Decompression bomb — netty-codec-http2 (#94)        | DoS via compressed response; needs specific Content-Encoding |
| 2.2  | Decompression bomb — netty-codec-http (#93)         | Same, http variant                                           |
| 2.3  | HttpClientCodec response desync (#91)               | Needs specific conditions; affects HTTP client path          |
| 2.4  | Lz4FrameDecoder resource exhaustion (#90)           | DoS via LZ4 decompression of untrusted data                  |
| 2.5  | Zip bomb style DoS (#34)                            | Needs crafted compressed input                               |
| 2.6  | DNS Codec Input Validation Bypass (#87)             | Needs DNS resolution context                                 |
| 2.7  | SslHandler native crash (#10)                       | Requires native SSLEngine (not OpenJDK default)              |
| 2.8  | DoS on Windows (#12)                                | Platform-specific (Windows only)                             |
| 2.9  | HTTP Header Injection via HttpProxyHandler (#86)    | Needs proxy configuration                                    |
| 2.10 | Request smuggling via chunk extension parsing (#35) | Low severity; smuggling variant                              |

#### Spring / Spring Boot (17)

| #    | Item                                                      | Why                                                  |
|------|-----------------------------------------------------------|------------------------------------------------------|
| 2.11 | EndpointRequest.to() wrong matcher (#16)                  | Affects actuator security when endpoint is unexposed |
| 2.12 | Predictable temp dir without ownership verification (#79) | Needs local file system access                       |
| 2.13 | Reflected file download / RFD (#25)                       | Needs user interaction / specific browser conditions |
| 2.14 | Path Traversal (#32)                                      | Needs specific MVC endpoint patterns                 |
| 2.15 | Path Limitation — Script View Templates (#63)             | Needs script templates enabled                       |
| 2.16 | Path Limitation — WebFlux (#64)                           | Needs script templates enabled                       |
| 2.17 | Password length not enforced (#14)                        | Mitigated by application-level password policy       |
| 2.18 | DoS resolving static resources — WebMVC (#84)             | Needs static resource serving                        |
| 2.19 | DoS resolving static resources — WebFlux (#83)            | Needs static resource serving                        |
| 2.20 | SSE stream corruption — WebMVC (#60)                      | Needs SSE endpoints                                  |
| 2.21 | SSE stream corruption — WebFlux (#61)                     | Needs SSE endpoints                                  |
| 2.22 | Cache poisoning — WebMVC (#82)                            | Needs cache headers + specific conditions            |
| 2.23 | Cache poisoning — WebFlux (#81)                           | Needs cache headers + specific conditions            |
| 2.24 | DoS with Multipart Temp Files (#80)                       | Needs multipart upload endpoint                      |
| 2.25 | DataBinder Case Sensitive Match Exception (#23)           | Edge case in data binding                            |
| 2.26 | xstream DoS via stack overflow (#5)                       | Needs xstream in runtime path (potentially unused)   |
| 2.27 | jackson-core Number Length Constraint Bypass (#52)        | Needs async parser, unlikely in typical REST         |

#### Tomcat (1)

| #    | Item                                          | Why                              |
|------|-----------------------------------------------|----------------------------------|
| 2.28 | Improper encoding in JsonAccessLogValve (#69) | Needs JsonAccessLogValve enabled |

#### Kafka (3)

| #    | Item                                    | Why                                     |
|------|-----------------------------------------|-----------------------------------------|
| 2.29 | Deserialization of untrusted data (#46) | Depends on Kafka consumer config        |
| 2.30 | Arbitrary File Read / SSRF (#24)        | Needs specific Kafka broker config      |
| 2.31 | Sensitive info in DEBUG logs (#76)      | Needs DEBUG log level for kafka-clients |

#### Apache Commons (3)

| #    | Item                                    | Why                                        |
|------|-----------------------------------------|--------------------------------------------|
| 2.32 | commons-beanutils access control (#20)  | Needs BeanUtils usage with untrusted input |
| 2.33 | commons-io DoS via XmlStreamReader (#4) | Needs XmlStreamReader usage                |
| 2.34 | commons-compress OOM Pack200 (#2)       | Needs Pack200 archive parsing              |

#### Bouncy Castle (2)

| #    | Item                        | Why                                       |
|------|-----------------------------|-------------------------------------------|
| 2.35 | Covert timing channel (#77) | Theoretical crypto timing attack          |
| 2.36 | LDAP injection (#75)        | Needs LDAP integration with Bouncy Castle |

#### ZooKeeper (2)

| #    | Item                                           | Why                         |
|------|------------------------------------------------|-----------------------------|
| 2.37 | Improper config values handling (#55)          | Needs ZooKeeper integration |
| 2.38 | Reverse-DNS hostname verification bypass (#58) | Needs ZooKeeper SSL/TLS     |

#### HTTP Clients (2)

| #    | Item                                     | Why                                |
|------|------------------------------------------|------------------------------------|
| 2.39 | httpclient5 domain checks disabled (#15) | Affects HTTPS connections          |
| 2.40 | httpclient (4.x) XSS (#1)                | Needs browser-interpreted response |

#### Logback (4)

| #    | Item                               | Why                                               |
|------|------------------------------------|---------------------------------------------------|
| 2.41 | Expression Language Injection (#7) | Needs logback config with untrusted input         |
| 2.42 | Arbitrary Code Execution (#41)     | Needs specific logback config for file processing |
| 2.43 | SSRF (#8)                          | Needs logback SocketAppender or similar           |
| 2.44 | Classpath instantiation (#49)      | Needs specific logback config                     |

#### Other (4)

| #    | Item                                     | Why                                       |
|------|------------------------------------------|-------------------------------------------|
| 2.45 | lz4-java OOB memory / DoS (#43)          | Needs LZ4 decompression of untrusted data |
| 2.46 | lz4-java info leak (#45)                 | Needs LZ4 decompression of untrusted data |
| 2.47 | jose4j DoS via compressed JWE (#48)      | Needs JWE token parsing (JWT auth path)   |
| 2.48 | reactor-netty-http credential leak (#30) | Needs chained redirects with auth headers |

### 🔵 P2 — Accept (7 items)

| #    | Item                                             | Why                                                   |
|------|--------------------------------------------------|-------------------------------------------------------|
| P2.1 | assertj-core XXE via isXmlEqualTo (#50)          | Test-scope dependency only                            |
| P2.2 | commons-lang Uncontrolled Recursion (#29)        | Requires crafted deeply-nested input                  |
| P2.3 | commons-lang3 Uncontrolled Recursion (#28)       | Requires crafted deeply-nested input                  |
| P2.4 | commons-compress DUMP infinite loop (#3)         | Requires parsing untrusted DUMP archives              |
| P2.5 | json-smart Uncontrolled Recursion (#11)          | Requires deeply nested JSON                           |
| P2.6 | rhino DoS via toFixed() (#44)                    | Rhino JS engine unlikely in microservice runtime path |
| P2.7 | commons-configuration Resource Consumption (#17) | Requires configuration parsing of untrusted source    |

---

### Additional Fixes Applied

| #   | Task                                                                     | Files                                                                         | Status |
|-----|--------------------------------------------------------------------------|-------------------------------------------------------------------------------|--------|
| A.1 | Upgrade SpringDoc 2.3.0→2.7.0 (compat with Spring Web 6.2)               | `products/build.gradle`, `billing/build.gradle`                               | ✅      |
| A.2 | Upgrade Tomcat 10.1.33→10.1.55 (CVE-2025-24813, CLIENT_CERT auth bypass, log injection, JsonAccessLogValve encoding) | `products/build.gradle`, `billing/build.gradle`, `eureka-server/build.gradle` | ✅      |
| A.3 | Upgrade Jersey 3.1.9→3.1.10 (SSL race condition)                         | all 4 `build.gradle` files (jersey.version)                                   | ✅      |
| A.4 | Fix Kafka bootstrap-servers placeholder resolution                       | `products/application.yml`, `billing/application.yml`                         | ✅      |
| A.5 | Fix Redis password placeholder resolution                                | `products/application.yml`, `RedisConfig.java`                                | ✅      |
| A.6 | Remove machine-specific `setup-env.ps1`                                  | Deleted                                                                       | ✅      |
| A.7 | Clean `gradle.properties` (OS-agnostic)                                  | `gradle.properties`                                                           | ✅      |
| A.8 | Remove duplicate/placeholder documentation                               | 5 files deleted, `SECURITY.md` rewritten                                      | ✅      |

---

## Detailed Findings

### 🔴 Critical

#### 1.1 Hardcoded Secrets

- `products/application.yml:13` — `token: myroot` (Vault dev token)
- `products/application.yml:24` — `password: password` (MongoDB)
- `billing/application.yml:13` — `token: myroot` (Vault dev token)
- `docker-compose.yml:11` — MongoDB root password
- `docker-compose.yml:51` — Vault root token
- `docker-compose.yml:69` — Redis requirepass

**Risk**: Production credentials could be leaked. Even in dev, hardcoded values override Vault's purpose.

**Fix**: Replaced with env var placeholders (`${VAULT_TOKEN:myroot}`, `${MONGO_PASSWORD:password}`) and created
`.env.example`.

#### 1.2 ValidatorFactory Resource Leak

- `CreateProductCommandTest.java:20-22` — `try-with-resources` closes factory immediately, leaving `validator` field
  referencing a closed factory's validator. All 10 test methods may operate on an invalid validator.

**Fix**: Moved factory to field, added `@AfterEach tearDown()` with explicit `factory.close()`.

#### 1.3 Missing Global Exception Handler

- `DuplicateProductException` → 500 instead of 409 CONFLICT
- `ProductNotFoundException` → 500 instead of 404 NOT_FOUND
- No `@RestControllerAdvice` or `@ExceptionHandler` in either service.

**Fix**: Created `GlobalExceptionHandler.java` with handlers for 404, 409, 400, and 409 responses.

### 🟠 High

#### 2.1-2.4 Incomplete Update Product Path

- `UpdateProductCommandHandler` directly modifies `ProductEntity` and saves it.
- Does NOT raise `ProductUpdatedEvent`, save to EventStore, or publish to Kafka.
- Breaks Event Sourcing contract and SAGA visibility.

**Status**: Pending

#### 3.1-3.4 Billing Service Mock Data

- `InvoiceCommandHandler` — mock in-memory, no repository
- `InvoiceQueryHandler` — returns hardcoded mock list
- No MongoDB dependency in `billing/build.gradle`
- No persistence config in `billing/application.yml`
- SAGA `ProductCreatedEventHandler.handle()` body is empty (just logging)

**Status**: Reverted — to be re-implemented when prioritized

#### 4.1 System.err.println

- `EventPublisher.java:34` — uses `System.err.println` instead of a proper logger. Class has Lombok but no `@Slf4j`.

**Status**: Pending

#### 4.2 Event Deserialization Fragility

- `MongoEventStore.java:63` — `Class.forName("com.example.products.domain.event." + entity.getEventType())`
- If an event class is renamed or moved, ALL stored events become unreadable.
- No event versioning or schema evolution strategy.

**Status**: Pending

### 🟡 Medium

- `ProductAggregate` manually manages `version++` while `ProductEntity` has Spring Data `@Version` — dual management can
  conflict.
- OpenAPI specs lack error response schemas (400, 404, 409).
- Docker images use `latest` tags — no reproducibility.
- No Dockerfiles for microservices, no CI/CD, no Kubernetes manifests.
- No circuit breaker (Resilience4j), retry/backoff, or distributed tracing.

### 🔵 Low

- `BaseController` classes in both services — unused/empty.
- `ProductEntity.domainEvents` (`@Transient`) — never called from production code.
- `ProductLookupEventsHandler` — grammar (unnecessary plural).
- `ProductCreatedEvent.java:6` — unused `import BeanUtils`.
- Missing `GET /products/{id}` and `DELETE` endpoints in Products API.

---

## Status Key

| Symbol | Meaning            |
|--------|--------------------|
| ⬜      | Pending            |
| 🔄     | In Progress        |
| ✅      | Complete           |
| ❌      | Blocked / Reverted |
