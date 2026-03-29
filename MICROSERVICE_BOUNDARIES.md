# Microservice Boundaries and Shared Code Rules

This repo follows service ownership first, with shared code only for stable cross-cutting concerns.

## 1) Service Ownership (per service)
Keep these inside each service module (`userservice`, `course-service`, `cart-service`, `coupon-service`, `apigateway` where applicable):

- `entity` (JPA/domain models)
- `repository` (Spring Data interfaces)
- domain/business `exceptions`
- service-specific request/response DTOs
- business services and controllers

Reason: each service must evolve and deploy independently.

## 2) What Can Go in `commonlibs`
Only stable, cross-cutting technical code:

- common API envelope (`ApiResponse`) if truly identical for all services
- error code model and generic exception handler helpers
- logging/tracing/correlation-id utilities
- security helpers that are not domain-specific
- technical base classes (for example, audit timestamps)

## 3) What Must Not Go in `commonlibs`
Do not centralize:

- service domain entities
- repositories
- service-owned business exceptions
- business logic tied to one domain

## 4) Shared Contracts Pattern
If DTOs must be shared, use versioned contract modules (for example `contracts-user`, `contracts-course`) and keep only external API/event schemas there.

- Backward compatible changes preferred
- Version contract modules explicitly
- No direct service-to-service code imports

## 5) Suggested Package Shape Per Service
Use this structure in each service:

- `config`
- `controller`
- `service`
- `repository`
- `entity`
- `dto` (or split into `dto.request` and `dto.response`)
- `exception`
- `security`
- `util`

## 6) Practical Rule of Thumb
If changing a class forces multiple services to release together, that class likely does not belong in shared libraries.
