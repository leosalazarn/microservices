# Security Policy

## Reporting a Vulnerability

Please report security vulnerabilities by opening an issue on the GitHub repository or contacting the maintainers directly.

We aim to acknowledge receipt of vulnerability reports within 48 hours and provide a timeline for a fix within 5 business days.

## Supported Versions

This project is a proof-of-concept (POC) and is not currently published as a stable release. All security issues should be reported as described above.

## Secrets Management

- Production secrets must never be committed to source control.
- This project uses HashiCorp Vault for secret management.
- Docker Compose credentials are for local development only.
- See `docs/DOCKER.md` for infrastructure configuration.
