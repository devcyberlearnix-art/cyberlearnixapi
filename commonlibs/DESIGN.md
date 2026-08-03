# Common Libraries - Design Document

## Overview
The `commonlibs` module serves as the foundational shared library for the CyberLearnix ecommerce platform. It contains all shared data transfer objects (DTOs), JPA entities, exception handling, repositories, and utility services that are used across multiple microservices.

## Purpose
- **Code Reusability**: Centralize common data models and utilities to avoid duplication across services
- **Consistency**: Ensure consistent data structures and business logic across the distributed system
- **Type Safety**: Provide strongly-typed data contracts between services
- **Maintainability**: Single source of truth for shared domain models

## Architecture

### Package Structure
```
com.CyberLearnix.commonlibs/
├── dto/                    # Data Transfer Objects
├── entity/                 # JPA Entity Models
├── elasticsearch/          # Elasticsearch-specific models
├── exception/             # Common exception classes
├── repository/            # Shared JPA repositories
└── service/               # Shared service interfaces/implementations
```

## Core Components

### 1. Entities (`entity/`)
JPA entity classes representing the core domain models persisted in PostgreSQL.

#### User Domain
- **`UserEntity`**: Core user profile with phone, name, email, addresses
- **`UserSession`**: Active user sessions with JWT tokens and device info
- **`OtpSession`**: OTP verification sessions for authentication
- **`AddressEntity`**: User delivery addresses with geolocation

#### Product Catalog Domain
- **`Product`**: Product master data with pricing, descriptions, images
- **`Category`**: Top-level product categories
- **`SubCategory`**: Secondary categorization level
- **`SubSubCategory`**: Tertiary categorization (leaf level)

#### Store Domain
- **`StoreEntity`**: Store/merchant locations with operational details
- **`Inventory`**: Product availability at specific stores (composite key)
- **`InventoryKey`**: Composite key (storeId, productId) for inventory

#### Shopping & Orders Domain
- **`Cart`**: User shopping cart container
- **`CartItem`**: Individual products in cart with quantity
- **`CartConfiguration`**: Cart-level settings and preferences
- **`Order`**: Order header with status, timestamps, totals
- **`OrderItem`**: Line items in an order
- **`OrderPayment`**: Payment transaction details
- **`OrderDelivery`**: Delivery tracking information
- **`OrderFulfillmentAudit`**: Order lifecycle audit trail
- **`DeliveryCharge`**: Delivery fee configuration

#### Engagement Domain
- **`Banner`**: Promotional banners for landing page

### 2. Data Transfer Objects (`dto/`)
Lightweight objects for service-to-service and client-to-server communication.

#### Authentication & Session DTOs
- `OtpRequestDTO`, `OtpResponseDTO`, `OtpValidationDTO`
- `PhoneOtpRequestDTO`, `PhoneOtpValidationDTO`, `PhoneOtpValidationResponseDTO`
- `SessionInfoDTO`, `SessionManagementResponseDTO`, `DeviceInfoDTO`
- `NewUserRegistrationDTO`, `UserRegistrationDTO`, `UserRegistrationCompleteDTO`
- `UserRegistrationResponseDTO`, `UserResponseDTO`

#### Product & Catalog DTOs
- `ProductRequestDTO`, `ProductResponseDTO`, `ProductAutocompleteDTO`
- `CategoryRequestDTO`, `CategoryResponseDTO`, `CategoryInfo`
- `SubCategoryDTO`, `SubCategoryInfo`
- `SubSubCategoryDTO`, `SubSubCategoryInfo`
- `CategoryHomeSectionDTO`, `CategoryHomeSectionsResponseDTO`

#### Store & Inventory DTOs
- `StoreRequestDTO`, `StoreResponseDTO`, `StoreFullResponseDTO`, `StoreSyncDTO`
- `InventoryRequestDTO`, `InventoryResponseDTO`

#### Cart & Order DTOs
- `CartDto`, `CartItemDto`
- `AddToCartRequest`, `IncrementCartItemRequest`, `DecrementCartItemRequest`
- `UpdateCartItemRequest`, `RemoveCartItemRequest`
- `CreateOrderRequest`, `OrderResponse`, `UpdateOrderStatusRequest`

#### Landing Page DTOs
- `LandingPageSectionDTO`, `LandingPageItemDTO`

#### Merchant DTOs
- `MerchantRegistrationSyncDTO`

#### Generic Response Wrapper
- `ApiResponseWrapper`: Standardized API response envelope

### 3. Exception Handling (`exception/`)
- **`ErrorResponse`**: Detailed error response with validation errors
- **`SimpleErrorResponse`**: Lightweight error response

### 4. Repositories (`repository/`)
Shared Spring Data JPA repository interfaces with custom query methods.

### 5. Elasticsearch Models (`elasticsearch/`)
Domain models optimized for Elasticsearch indexing and search operations.

## Design Patterns

### 1. Shared Kernel Pattern
Commonlibs implements the Shared Kernel pattern from Domain-Driven Design (DDD), where core domain concepts are shared across bounded contexts (microservices).

### 2. Composite Key Pattern
Used in `Inventory` entity with `InventoryKey` for multi-column primary keys (storeId + productId).

### 3. DTO Pattern
Separation of persistence models (entities) from data transfer models (DTOs) to:
- Decouple API contracts from database schema
- Control data exposure (security)
- Enable versioning of APIs independently of data model

### 4. Repository Pattern
JPA repositories provide abstraction over data access layer with:
- Standard CRUD operations
- Custom query methods
- Transaction management

## Data Relationships

### Entity Relationships
```
UserEntity (1) ─── (*) UserSession
UserEntity (1) ─── (*) AddressEntity
UserEntity (1) ─── (1) Cart
Cart (1) ─── (*) CartItem
UserEntity (1) ─── (*) Order
Order (1) ─── (*) OrderItem
Order (1) ─── (1) OrderPayment
Order (1) ─── (1) OrderDelivery
Order (1) ─── (*) OrderFulfillmentAudit

Category (1) ─── (*) SubCategory
SubCategory (1) ─── (*) SubSubCategory
Product (*) ─── (1) SubSubCategory

StoreEntity (1) ─── (*) Inventory
Product (1) ─── (*) Inventory
```

## Technology Stack
- **Java**: JDK 17+
- **Spring Boot**: 3.x
- **Spring Data JPA**: Entity management and repositories
- **PostgreSQL**: Primary relational database
- **Elasticsearch**: Search and analytics
- **Lombok**: Reduce boilerplate code

## Validation
- JSR-303 Bean Validation annotations on DTOs
- Custom validation logic in service layer
- Database constraints on entities

## Best Practices

### Entity Design
1. Use `@Entity` for JPA persistence
2. Implement `@Table` with explicit table names
3. Use appropriate fetch types (LAZY by default)
4. Use `@JsonIgnore` on bidirectional relationships to prevent cycles
5. Override `equals()` and `hashCode()` for entities with natural keys

### DTO Design
1. Keep DTOs immutable where possible
2. Use validation annotations (`@NotNull`, `@Size`, etc.)
3. Separate request and response DTOs
4. Use builder pattern (Lombok `@Builder`) for complex DTOs

### Repository Design
1. Extend `JpaRepository<Entity, ID>`
2. Use method naming conventions for query derivation
3. Use `@Query` for complex queries
4. Prefer projections for read-heavy operations

## Future Enhancements
1. **Versioning**: Introduce API versioning for DTOs (v1, v2 packages)
2. **Validation Groups**: JSR-303 validation groups for create vs update scenarios
3. **Audit Trail**: Add `@CreatedDate`, `@LastModifiedDate` to all entities
4. **Soft Deletes**: Implement soft delete pattern with `@Where` clause
5. **Event Sourcing**: Consider event models for order lifecycle
6. **GraphQL**: Add GraphQL schema definitions alongside DTOs
7. **OpenAPI**: Generate OpenAPI specs from DTOs

## Dependencies
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- PostgreSQL Driver
- Lombok
- Jackson (JSON serialization)

## Usage Guidelines

### For Service Developers
1. **Always use commonlibs DTOs** for inter-service communication
2. **Never modify entities directly** in REST controllers; use DTOs
3. **Map between entities and DTOs** in service layer (use MapStruct for complex mappings)
4. **Extend repositories** in individual services for service-specific queries
5. **Handle exceptions** using commonlibs exception classes

### Versioning Strategy
- Commonlibs version follows semantic versioning (MAJOR.MINOR.PATCH)
- Breaking changes require MAJOR version bump
- All services must align on commonlibs version to ensure compatibility

## Security Considerations
- DTOs should never expose sensitive data (passwords, tokens) directly
- Use `@JsonIgnore` on sensitive entity fields
- Implement field-level encryption for PII in entities
- Validate all input DTOs to prevent injection attacks

## Performance Considerations
- Use DTOs to fetch only required fields (projection)
- Avoid N+1 queries with proper `@EntityGraph` or fetch joins
- Use pagination for list operations
- Cache frequently accessed reference data (categories, stores)

## Testing Strategy
- Unit tests for entity validation logic
- Integration tests for repository methods
- Contract tests for DTOs (ensure backward compatibility)
- Serialization tests for DTOs (JSON marshalling/unmarshalling)
