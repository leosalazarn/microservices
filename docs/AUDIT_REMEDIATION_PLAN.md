# Audit & Remediation Plan

**Date**: 2026-05-14  
**Prod Readiness**: 🔴 **Not Ready** — 15 blocking (P0), 48 before-GA (P1) CVEs remain  
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

| Severity    | Count | Key Areas                                                                         |
|-------------|-------|-----------------------------------------------------------------------------------|
| 🔴 Critical | 3     | Hardcoded secrets, test resource leak, missing error handler                      |
| 🟠 High     | 4     | Billing mocks, incomplete Event Sourcing, stderr logging, fragile deserialization |
| 🟠 High     | 71    | Dependabot/CVE in Netty, Spring, Tomcat, Kafka, Commons, LZ4, Bouncy Castle, etc. |
| 🟡 Medium   | 5     | Unused deps, version conflicts, Docker tags, missing CI/CD/K8s                    |
| 🔵 Low      | 8     | Dead code, naming, missing endpoints, SECURITY.md, etc.                           |

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

## Remediation Phases

### 🔴 Phase 1 — Critical (Security & Correctness)

| #   | Task                                                      | Files                                                                                       | Status |
|-----|-----------------------------------------------------------|---------------------------------------------------------------------------------------------|--------|
| 1.1 | Replace hardcoded secrets with env var placeholders       | `products/application.yml`, `billing/application.yml`, `docker-compose.yml`, `.env.example` | ✅      |
| 1.2 | Fix ValidatorFactory resource leak in test                | `CreateProductCommandTest.java`                                                             | ✅      |
| 1.3 | Add global `@RestControllerAdvice` for HTTP error mapping | `products/exception/GlobalExceptionHandler.java`                                            | ✅      |

### 🟠 Phase 2 — Event Sourcing Completeness

| #   | Task                                                       | Files                              | Status |
|-----|------------------------------------------------------------|------------------------------------|--------|
| 2.1 | Complete `UpdateProductCommandHandler` with Event Sourcing | `UpdateProductCommandHandler.java` | ⬜      |
| 2.2 | Create `ProductUpdatedEvent` domain event                  | New: `ProductUpdatedEvent.java`    | ⬜      |
| 2.3 | Update `DomainEventPublisher` for `ProductUpdatedEvent`    | `DomainEventPublisher.java`        | ⬜      |
| 2.4 | Add Kafka producer for `ProductUpdatedEvent`               | `EventPublisher.java`              | ⬜      |

### 🟠 Phase 3 — Billing Service Completion

| #   | Task                                               | Files                                                    | Status     |
|-----|----------------------------------------------------|----------------------------------------------------------|------------|
| 3.1 | Add MongoDB dependency to billing                  | `billing/build.gradle`                                   | ❌ Reverted |
| 3.2 | Configure MongoDB in billing application.yml       | `billing/application.yml`                                | ❌ Reverted |
| 3.3 | Create `InvoiceEntity` and `InvoiceRepository`     | New: 2 files                                             | ❌ Reverted |
| 3.4 | Replace mock implementations with real persistence | `InvoiceCommandHandler.java`, `InvoiceQueryHandler.java` | ❌ Reverted |

### 🟠 Phase 4 — Logging & Robustness

| #   | Task                                                       | Files                  | Status |
|-----|------------------------------------------------------------|------------------------|--------|
| 4.1 | Replace `System.err.println` with `log.error()`            | `EventPublisher.java`  | ⬜      |
| 4.2 | Fix Event deserialization (use FQCN instead of SimpleName) | `MongoEventStore.java` | ⬜      |

### 🟡 Phase 5 — Cleanup

| #   | Task                                                              | Files                                                    | Status |
|-----|-------------------------------------------------------------------|----------------------------------------------------------|--------|
| 5.1 | Remove unused `spring-kafka` from eureka + gateway                | `eureka-server/build.gradle`, `api-gateway/build.gradle` | ✅      |
| 5.2 | Remove dead `BaseController` classes                              | Both `BaseController.java` files                         | ⬜      |
| 5.3 | Remove unused `domainEvents` from `ProductEntity`                 | `ProductEntity.java`                                     | ⬜      |
| 5.4 | Remove unused import in `ProductCreatedEvent`                     | `ProductCreatedEvent.java`                               | ⬜      |
| 5.5 | Rename `ProductLookupEventsHandler` → `ProductLookupEventHandler` | 1 file rename + references                               | ⬜      |
| 5.6 | Pin Docker image versions                                         | `docker-compose.yml`                                     | ⬜      |
| 5.7 | Update `SECURITY.md` with actual policy                           | `SECURITY.md`                                            | ✅      |

### 🟠 Phase 6 — Dependabot Vulnerability Remediation

| #    | Package                          | Issue                                                        | Status  |
|------|----------------------------------|--------------------------------------------------------------|---------|
| 6.1  | `netty-codec-http2`              | HTTP/2 CONTINUATION Frame Flood DoS (#67)                    | ⬜       |
| 6.2  | `netty-codec-http2`              | MadeYouReset HTTP/2 DDoS (#31)                               | ⬜       |
| 6.3  | `netty-codec-http2`              | Decompression bomb via Content-Encoding bypass (#94)         | ⬜       |
| 6.4  | `netty-codec-http`               | HTTP Request Smuggling via Chunked Extension (#66)           | ⬜       |
| 6.5  | `netty-codec-http`               | Decompression bomb via Content-Encoding bypass (#93)         | ⬜       |
| 6.6  | `netty-codec-http`               | HttpClientCodec response desynchronization (#91)             | ⬜       |
| 6.7  | `netty-codec-http`               | CRLF Injection in HttpRequestEncoder (#47)                   | ⬜       |
| 6.8  | `netty-codec-http`               | HTTP Request Smuggling via malformed Transfer-Encoding (#92) | ⬜       |
| 6.9  | `netty-codec-http`               | HTTP Request Smuggling via incorrect chunk size (#88)        | ⬜       |
| 6.10 | `netty-codec`                    | Lz4FrameDecoder resource exhaustion (#90)                    | ⬜       |
| 6.11 | `netty-codec`                    | DoS via zip bomb style attack (#34)                          | ⬜       |
| 6.12 | `netty-codec-dns`                | DNS Codec Input Validation Bypass (#87)                      | ⬜       |
| 6.13 | `netty-handler`                  | SslHandler native crash via malformed packets (#10)          | ⬜       |
| 6.14 | `spring-boot-starter-actuator`   | Auth bypass under Actuator CloudFoundry endpoints (#65)      | ⬜       |
| 6.15 | `spring-boot-starter-actuator`   | Auth bypass under Actuator Health groups (#62)               | ⬜       |
| 6.16 | `spring-boot`                    | EndpointRequest.to() wrong matcher (#16)                     | ⬜       |
| 6.17 | `spring-boot`                    | Predictable temp dir without ownership verification (#79)    | ⬜       |
| 6.18 | `spring-cloud-gateway-server`    | Forwards headers from untrusted proxies (#22)                | ⬜       |
| 6.19 | `spring-cloud-gateway-server`    | Expression Language Injection (#51)                          | ⬜       |
| 6.20 | `spring-core`                    | Annotation detection auth bypass (#36)                       | ⬜       |
| 6.21 | `spring-web`                     | Reflected file download / RFD (#25)                          | ⬜       |
| 6.22 | `spring-webmvc`                  | Path Traversal (#32)                                         | ⬜       |
| 6.23 | `spring-webmvc`                  | Improper Path Limitation with Script View Templates (#63)    | ⬜       |
| 6.24 | `spring-webflux`                 | Improper Path Limitation with Script View Templates (#64)    | ⬜       |
| 6.25 | `spring-security-crypto`         | Password length not enforced (#14)                           | ⬜       |
| 6.26 | `tomcat-embed-core`              | Sensitive info in log file (#70)                             | ⬜       |
| 6.27 | `tomcat-embed-core`              | Improper encoding in JsonAccessLogValve (#69)                | ⬜       |
| 6.28 | `tomcat-embed-core`              | CLIENT_CERT auth (#71)                                       | ✅ Fixed |
| 6.29 | `kafka_2.13`                     | Deserialization of untrusted data (#46)                      | ⬜       |
| 6.30 | `kafka-clients`                  | Producer message corruption via buffer pool race (#74)       | ⬜       |
| 6.31 | `kafka-clients`                  | Arbitrary File Read / SSRF (#24)                             | ⬜       |
| 6.32 | `commons-beanutils`              | Improper access control (#20)                                | ⬜       |
| 6.33 | `commons-io`                     | DoS via XmlStreamReader (#4)                                 | ⬜       |
| 6.34 | `commons-compress`               | OOM unpacking broken Pack200 file (#2)                       | ⬜       |
| 6.35 | `commons-lang`                   | Uncontrolled Recursion (#29)                                 | ⬜       |
| 6.36 | `commons-lang3`                  | Uncontrolled Recursion (#28)                                 | ⬜       |
| 6.37 | `lz4-java`                       | Out-of-bounds memory / DoS (#43)                             | ⬜       |
| 6.38 | `lz4-java`                       | Info leak in safe decompressor (#45)                         | ⬜       |
| 6.39 | `bcprov-jdk18on` (Bouncy Castle) | Covert timing channel (#77)                                  | ⬜       |
| 6.40 | `zookeeper`                      | Improper handling of config values (#55)                     | ⬜       |
| 6.41 | `zookeeper`                      | Reverse-DNS hostname verification bypass (#58)               | ⬜       |
| 6.42 | `httpclient5`                    | Domain checks disabled (#15)                                 | ⬜       |
| 6.43 | `xstream`                        | DoS via stack overflow (#5)                                  | ⬜       |
| 6.44 | `assertj-core`                   | XXE via isXmlEqualTo (#50)                                   | ⬜       |
| 6.45 | `json-smart`                     | Uncontrolled recursion (#11)                                 | ⬜       |
| 6.46 | `jose4j`                         | DoS via compressed JWE (#48)                                 | ⬜       |
| 6.47 | `jackson-core`                   | Number Length Constraint Bypass / DoS (#52)                  | ⬜       |
| 6.48 | `reactor-netty-http`             | Credential leaks during chained redirects (#30)              | ⬜       |
| 6.49 | `logback-core`                   | Expression Language Injection (#7)                           | ⬜       |
| 6.50 | `logback-core`                   | Arbitrary Code Execution (#41)                               | ⬜       |
| 6.51 | `netty-codec-http`               | HTTP/1.0 TE+CL smuggling bypass (#89)                        | ⬜       |
| 6.52 | `netty-codec-http`               | Start-Line Injection smuggling (#78)                         | ⬜       |
| 6.53 | `netty-codec-http`               | Request smuggling via chunk extension parsing (#35)          | ⬜       |
| 6.54 | `netty-common`                   | DoS on Windows (#12)                                         | ⬜       |
| 6.55 | `netty-handler-proxy`            | HTTP Header Injection via HttpProxyHandler (#86)             | ⬜       |
| 6.56 | `spring-webmvc`                  | DoS resolving static resources (#84)                         | ⬜       |
| 6.57 | `spring-webflux`                 | DoS resolving static resources (#83)                         | ⬜       |
| 6.58 | `spring-webmvc`                  | SSE stream corruption (#60)                                  | ⬜       |
| 6.59 | `spring-webflux`                 | SSE stream corruption (#61)                                  | ⬜       |
| 6.60 | `spring-webmvc`                  | Cache poisoning static resources (#82)                       | ⬜       |
| 6.61 | `spring-webflux`                 | Cache poisoning static resources (#81)                       | ⬜       |
| 6.62 | `spring-webflux`                 | DoS with Multipart Temp Files (#80)                          | ⬜       |
| 6.63 | `spring-context`                 | DataBinder Case Sensitive Match Exception (#23)              | ⬜       |
| 6.64 | `commons-compress`               | DoS infinite loop corrupted DUMP file (#3)                   | ⬜       |
| 6.65 | `commons-configuration`          | Uncontrolled Resource Consumption (#17)                      | ⬜       |
| 6.66 | `bcprov-jdk18on` (Bouncy Castle) | LDAP injection (#75)                                         | ⬜       |
| 6.67 | `httpclient` (4.x)               | XSS (#1)                                                     | ⬜       |
| 6.68 | `kafka-clients`                  | Sensitive info in DEBUG logs (#76)                           | ⬜       |
| 6.69 | `rhino`                          | DoS via toFixed() (#44)                                      | ⬜       |
| 6.70 | `logback-core`                   | SSRF (#8)                                                    | ⬜       |
| 6.71 | `logback-core`                   | Classpath instantiation (#49)                                | ⬜       |

---

## 🚦 Risk Triage — Prod Release Labels

| Label                 | Meaning                                                                                     | Action                                                        |
|-----------------------|---------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| 🔴 **P0 — Blocking**  | Ship-blocking. Perimeter/auth/data-integrity risks in direct or exposed deps.               | Fix before any production deployment.                         |
| 🟡 **P1 — Before GA** | Realistic threat under certain conditions (specific configs, feature usage, runtime paths). | Fix before general availability / v1.0 release.               |
| 🔵 **P2 — Accept**    | Test-scope dep, theoretical, or requires non-default config unlikely in this app's runtime. | Monitor Dependabot; fix opportunistically during maintenance. |

### 🔴 P0 — Blocking (15 items)

| #    | Item                                                 | Why                                                 |
|------|------------------------------------------------------|-----------------------------------------------------|
| 6.19 | Gateway EL Injection (#51)                           | Direct dep, expression injection = RCE at perimeter |
| 6.18 | Gateway forwarded headers (#22)                      | Direct dep, header injection at perimeter           |
| 6.14 | Actuator CloudFoundry auth bypass (#65)              | Direct dep, authentication bypass                   |
| 6.15 | Actuator Health groups auth bypass (#62)             | Direct dep, authentication bypass                   |
| 6.20 | Spring annotation detection auth bypass (#36)        | Affects security annotation evaluation              |
| 6.1  | netty-codec-http2 CONTINUATION flood DoS (#67)       | Public-facing DoS, no auth required                 |
| 6.2  | netty-codec-http2 MadeYouReset DDoS (#31)            | Public-facing DoS, no auth required                 |
| 6.4  | netty-codec-http smuggling — Chunked Ext (#66)       | HTTP smuggling at perimeter                         |
| 6.51 | netty-codec-http smuggling — TE+CL (#89)             | HTTP smuggling at perimeter                         |
| 6.52 | netty-codec-http smuggling — Start-Line (#78)        | HTTP smuggling at perimeter                         |
| 6.8  | netty-codec-http smuggling — Transfer-Encoding (#92) | HTTP smuggling at perimeter                         |
| 6.9  | netty-codec-http smuggling — chunk size (#88)        | HTTP smuggling at perimeter                         |
| 6.7  | CRLF Injection in HttpRequestEncoder (#47)           | Header injection at perimeter                       |
| 6.30 | kafka-clients buffer pool race (#74)                 | Message corruption / data integrity loss            |
| 6.26 | Tomcat sensitive info in log file (#70)              | Potential credential leakage in prod logs           |

### 🟡 P1 — Before GA (48 items)

#### Netty (10)

| #    | Item                                                | Why                                                          |
|------|-----------------------------------------------------|--------------------------------------------------------------|
| 6.3  | Decompression bomb — netty-codec-http2 (#94)        | DoS via compressed response; needs specific Content-Encoding |
| 6.5  | Decompression bomb — netty-codec-http (#93)         | Same, http variant                                           |
| 6.6  | HttpClientCodec response desync (#91)               | Needs specific conditions; affects HTTP client path          |
| 6.10 | Lz4FrameDecoder resource exhaustion (#90)           | DoS via LZ4 decompression of untrusted data                  |
| 6.11 | Zip bomb style DoS (#34)                            | Needs crafted compressed input                               |
| 6.12 | DNS Codec Input Validation Bypass (#87)             | Needs DNS resolution context                                 |
| 6.13 | SslHandler native crash (#10)                       | Requires native SSLEngine (not OpenJDK default)              |
| 6.54 | DoS on Windows (#12)                                | Platform-specific (Windows only)                             |
| 6.55 | HTTP Header Injection via HttpProxyHandler (#86)    | Needs proxy configuration                                    |
| 6.53 | Request smuggling via chunk extension parsing (#35) | Low severity; smuggling variant                              |

#### Spring / Spring Boot (17)

| #    | Item                                                      | Why                                                  |
|------|-----------------------------------------------------------|------------------------------------------------------|
| 6.16 | EndpointRequest.to() wrong matcher (#16)                  | Affects actuator security when endpoint is unexposed |
| 6.17 | Predictable temp dir without ownership verification (#79) | Needs local file system access                       |
| 6.21 | Reflected file download / RFD (#25)                       | Needs user interaction / specific browser conditions |
| 6.22 | Path Traversal (#32)                                      | Needs specific MVC endpoint patterns                 |
| 6.23 | Path Limitation — Script View Templates (#63)             | Needs script templates enabled                       |
| 6.24 | Path Limitation — WebFlux (#64)                           | Needs script templates enabled                       |
| 6.25 | Password length not enforced (#14)                        | Mitigated by application-level password policy       |
| 6.56 | DoS resolving static resources — WebMVC (#84)             | Needs static resource serving                        |
| 6.57 | DoS resolving static resources — WebFlux (#83)            | Needs static resource serving                        |
| 6.58 | SSE stream corruption — WebMVC (#60)                      | Needs SSE endpoints                                  |
| 6.59 | SSE stream corruption — WebFlux (#61)                     | Needs SSE endpoints                                  |
| 6.60 | Cache poisoning — WebMVC (#82)                            | Needs cache headers + specific conditions            |
| 6.61 | Cache poisoning — WebFlux (#81)                           | Needs cache headers + specific conditions            |
| 6.62 | DoS with Multipart Temp Files (#80)                       | Needs multipart upload endpoint                      |
| 6.63 | DataBinder Case Sensitive Match Exception (#23)           | Edge case in data binding                            |
| 6.43 | xstream DoS via stack overflow (#5)                       | Needs xstream in runtime path (potentially unused)   |
| 6.47 | jackson-core Number Length Constraint Bypass (#52)        | Needs async parser, unlikely in typical REST         |

#### Tomcat (1)

| #    | Item                                          | Why                              |
|------|-----------------------------------------------|----------------------------------|
| 6.27 | Improper encoding in JsonAccessLogValve (#69) | Needs JsonAccessLogValve enabled |

#### Kafka (3)

| #    | Item                                    | Why                                     |
|------|-----------------------------------------|-----------------------------------------|
| 6.29 | Deserialization of untrusted data (#46) | Depends on Kafka consumer config        |
| 6.31 | Arbitrary File Read / SSRF (#24)        | Needs specific Kafka broker config      |
| 6.68 | Sensitive info in DEBUG logs (#76)      | Needs DEBUG log level for kafka-clients |

#### Apache Commons (3)

| #    | Item                                    | Why                                        |
|------|-----------------------------------------|--------------------------------------------|
| 6.32 | commons-beanutils access control (#20)  | Needs BeanUtils usage with untrusted input |
| 6.33 | commons-io DoS via XmlStreamReader (#4) | Needs XmlStreamReader usage                |
| 6.34 | commons-compress OOM Pack200 (#2)       | Needs Pack200 archive parsing              |

#### Bouncy Castle (2)

| #    | Item                        | Why                                       |
|------|-----------------------------|-------------------------------------------|
| 6.39 | Covert timing channel (#77) | Theoretical crypto timing attack          |
| 6.66 | LDAP injection (#75)        | Needs LDAP integration with Bouncy Castle |

#### ZooKeeper (2)

| #    | Item                                           | Why                         |
|------|------------------------------------------------|-----------------------------|
| 6.40 | Improper config values handling (#55)          | Needs ZooKeeper integration |
| 6.41 | Reverse-DNS hostname verification bypass (#58) | Needs ZooKeeper SSL/TLS     |

#### HTTP Clients (2)

| #    | Item                                     | Why                                |
|------|------------------------------------------|------------------------------------|
| 6.42 | httpclient5 domain checks disabled (#15) | Affects HTTPS connections          |
| 6.67 | httpclient (4.x) XSS (#1)                | Needs browser-interpreted response |

#### Logback (4)

| #    | Item                               | Why                                               |
|------|------------------------------------|---------------------------------------------------|
| 6.49 | Expression Language Injection (#7) | Needs logback config with untrusted input         |
| 6.50 | Arbitrary Code Execution (#41)     | Needs specific logback config for file processing |
| 6.70 | SSRF (#8)                          | Needs logback SocketAppender or similar           |
| 6.71 | Classpath instantiation (#49)      | Needs specific logback config                     |

#### Other (4)

| #    | Item                                     | Why                                       |
|------|------------------------------------------|-------------------------------------------|
| 6.37 | lz4-java OOB memory / DoS (#43)          | Needs LZ4 decompression of untrusted data |
| 6.38 | lz4-java info leak (#45)                 | Needs LZ4 decompression of untrusted data |
| 6.46 | jose4j DoS via compressed JWE (#48)      | Needs JWE token parsing (JWT auth path)   |
| 6.48 | reactor-netty-http credential leak (#30) | Needs chained redirects with auth headers |

### 🔵 P2 — Accept (7 items)

| #    | Item                                             | Why                                                   |
|------|--------------------------------------------------|-------------------------------------------------------|
| 6.44 | assertj-core XXE via isXmlEqualTo (#50)          | Test-scope dependency only                            |
| 6.35 | commons-lang Uncontrolled Recursion (#29)        | Requires crafted deeply-nested input                  |
| 6.36 | commons-lang3 Uncontrolled Recursion (#28)       | Requires crafted deeply-nested input                  |
| 6.64 | commons-compress DUMP infinite loop (#3)         | Requires parsing untrusted DUMP archives              |
| 6.45 | json-smart Uncontrolled Recursion (#11)          | Requires deeply nested JSON                           |
| 6.69 | rhino DoS via toFixed() (#44)                    | Rhino JS engine unlikely in microservice runtime path |
| 6.65 | commons-configuration Resource Consumption (#17) | Requires configuration parsing of untrusted source    |

---

### Additional Fixes Applied

| #   | Task                                                                     | Files                                                                         | Status |
|-----|--------------------------------------------------------------------------|-------------------------------------------------------------------------------|--------|
| A.1 | Upgrade SpringDoc 2.3.0→2.7.0 (compat with Spring Web 6.2)               | `products/build.gradle`, `billing/build.gradle`                               | ✅      |
| A.2 | Upgrade Tomcat 10.1.33→10.1.53 (CVE-2025-24813, CLIENT_CERT auth bypass) | `products/build.gradle`, `billing/build.gradle`, `eureka-server/build.gradle` | ✅      |
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
