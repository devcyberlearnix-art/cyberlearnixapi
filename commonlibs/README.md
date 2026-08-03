# commonlibs

Shared kernel module for cross-cutting technical code used by multiple services.

## Allowed in this module

- API envelope models (for example, generic response wrappers)
- Common error model / error codes
- Cross-cutting utilities (logging, tracing, correlation helpers)
- Reusable security helpers that are domain-agnostic
- Shared test utility abstractions

## Not allowed in this module

- Domain entities from any service
- Spring Data repositories from any service
- Service-specific business exceptions
- Business logic tied to one service domain

## Package guidance

Use only cross-cutting package areas under:

- `com.cyberlearnix.commonlibs.api`
- `com.cyberlearnix.commonlibs.error`
- `com.cyberlearnix.commonlibs.security`
- `com.cyberlearnix.commonlibs.util`
- `com.cyberlearnix.commonlibs.logging`

Avoid creating domain folders such as `entity`, `repository`, and service-specific `dto` here.
