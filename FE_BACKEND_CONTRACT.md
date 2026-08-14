# FE/BE API Contract

Status: agreed local contract as of August 13, 2026
Gateway base URL: `http://localhost:8080`

## Official Routes

- Instructor APIs: `/api/v1/instructors/**` is the only supported path. `/instructor/**` is unsupported and FE should remove it now.
- Primary checkout: `POST /api/v1/orders/create`.
- Compatibility checkout: `POST /api/v1/cart/checkout` delegates to order-service and returns a real order. It is deprecated for removal on August 31, 2026 at 6:00 PM IST.
- All FE traffic must use the gateway. No service-specific ports or proxy headers are required.

## Common Envelopes

All endpoints listed in this document return success payloads with these top-level fields:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {},
  "timestamp": "2026-08-12T22:10:59.309528319Z"
}
```

Errors produced by the aligned gateway, cart, wishlist, order, and instructor paths use:

```json
{
  "timestamp": "2026-08-12T22:09:18.013758741Z",
  "status": 409,
  "code": "CONFLICT",
  "message": "Order cannot transition from CANCELLED to CANCELLED",
  "path": "/api/v1/orders/c14d9717-5bfb-4525-aa95-286b6a69f8b1/cancel",
  "traceId": "170e6c9a-921e-4fd0-b287-506a594fca58"
}
```

## Identifier Types

| Identifier | Type | Example |
|---|---|---|
| Public `courseId` | JSON number / Java `Long` | `1` |
| Instructor-service local `courseId` | JSON number / Java `Long` | `5` |
| `orderId` | UUID string | `c14d9717-5bfb-4525-aa95-286b6a69f8b1` |
| `userId` | UUID string | `3be5c653-b22c-4121-adf6-79f971ac7252` |
| Instructor path `{id}` | User UUID string | `b00b333c-7788-4c70-a239-6ff18c34a3b4` |
| Wishlist item ID | UUID string | generated per item |

Use public course IDs for catalog, cart, wishlist, enrollment, orders, and reviews. Use instructor local course IDs only below `/api/v1/instructors/{id}/courses/**`.

## Authentication

Access JWT claims are `sub`, `userId`, `email`, `role`, `type`, `jti`, `iss`, and `aud`. `sub` and `userId` contain the same user UUID. `type` must be `access`.

- Access token expiry: 15 minutes.
- Refresh token expiry: 30 days.
- Refresh: `POST /api/v1/auth/refresh`, refresh token in `Authorization: Bearer <refresh-token>`.
- Logout: `POST /api/v1/auth/logout`, access token in the Authorization header and optional `{ "refreshToken": "..." }` body.
- Cart, wishlist, and owner-facing order routes require `STUDENT`.
- Instructor routes require `INSTRUCTOR`, `MAIN_ADMIN`, or `SUB_ADMIN`.
- Cross-user order reads, cancellation, and refunds return `404` to avoid exposing order existence.
- Admin order-user lookup and arbitrary status updates require `MAIN_ADMIN` or `SUB_ADMIN`.

## Cart

### `GET /api/v1/cart`

No request body.

```json
{
  "success": true,
  "message": "Cart retrieved successfully.",
  "data": {
    "cartId": "57910d43-6e24-3054-9608-1de00aca19c7",
    "totalCourses": 1,
    "courses": [{ "courseId": "1", "courseName": "Spring Boot Microservices Mastery", "price": 2499.0 }]
  },
  "timestamp": "2026-08-12T22:09:05.407170945Z"
}
```

### `DELETE /api/v1/cart/{courseId}`

No request body. `courseId` is numeric. Success `data` contains the removed `courseId`.

### `POST /api/v1/cart/checkout`

No request body. This compatibility route creates a real order and clears the cart.

```json
{
  "success": true,
  "message": "Checkout initiated successfully.",
  "data": {
    "orderId": "c14d9717-5bfb-4525-aa95-286b6a69f8b1",
    "paymentMethod": "RAZORPAY",
    "paymentStatus": "PENDING",
    "totalAmount": 1999.0
  },
  "timestamp": "2026-08-12T22:10:56.437355045Z"
}
```

The only checkout payment method currently emitted is `RAZORPAY`.

## Orders

### `POST /api/v1/orders/create`

```json
{
  "courseIds": [1],
  "couponCode": null
}
```

If `courseIds` is omitted or empty, courses are taken from the authenticated student's cart.

```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "b6d0c6ae-8238-4896-b39c-7ae285078f7e",
    "userId": "3be5c653-b22c-4121-adf6-79f971ac7252",
    "totalAmount": 2499.0,
    "status": "PENDING",
    "createdAt": "2026-08-13T03:39:10.456194828"
  },
  "timestamp": "2026-08-12T22:09:10.486905368Z"
}
```

### `GET /api/v1/orders`

Returns only the authenticated student's orders in `data`. No request body and no user ID query parameter.

### `DELETE /api/v1/orders/{orderId}/cancel`

No request body. Allowed only when the current status is `PENDING`.

### `POST /api/v1/orders/{orderId}/refund`

No request body. Allowed from `PAID` or `COMPLETED`; repeated refund is idempotent.

Allowed statuses: `PENDING`, `PAID`, `COMPLETED`, `FAILED`, `CANCELLED`, `REFUNDED`.

Allowed transitions:

- `PENDING` to `PAID`, `COMPLETED`, `CANCELLED`, or `FAILED`
- `PAID` to `COMPLETED` or `REFUNDED`
- `COMPLETED` to `REFUNDED`
- `FAILED`, `CANCELLED`, and `REFUNDED` are terminal

## Wishlist

### `GET /api/v1/wishlist`

No request body. `data` has `userId`, `totalItems`, and `items`.

### `DELETE /api/v1/wishlist/{courseId}`

No request body. `courseId` is numeric. Success `data` has `courseId` and `removedAt`.

## Instructor

Use the instructor user UUID in `{id}`.

- `GET /api/v1/instructors/{id}/dashboard`: no body; metrics are in `data`.
- `GET /api/v1/instructors/{id}/earnings`: no body; earnings are in `data`.
- `GET /api/v1/instructors/{id}/courses`: no body; `data` is an array.
- `GET /api/v1/instructors/{id}/courses/{courseId}`: no body.
- `DELETE /api/v1/instructors/{id}/courses/{courseId}`: no body; archives the course.

Course creation:

```http
POST /api/v1/instructors/b00b333c-7788-4c70-a239-6ff18c34a3b4/courses
```

```json
{
  "title": "API Design with Spring",
  "subtitle": "Build consistent REST APIs",
  "description": "A practical API design course.",
  "price": 999.0,
  "category": "Backend Development",
  "status": "PUBLISHED",
  "tags": ["Java", "Spring Boot", "REST"],
  "thumbnailUrl": "https://example.com/course.jpg",
  "previewVideoUrl": "https://example.com/preview.mp4"
}
```

Course statuses are `DRAFT`, `PUBLISHED`, and `ARCHIVED`.

## Pagination

The cart, order, wishlist, dashboard, earnings, and instructor course endpoints above are currently unpaginated. `GET /api/v1/admin/users` and `GET /api/v1/users` support `page` and `size`, defaulting to `0` and `10`. Their pagination response includes `totalUsers`, `currentPage`, `totalPages`, and `pageSize`; no `items/content` convention applies to the unpaginated endpoints.

## CORS

The gateway permits local origins including `http://localhost:5173`, forwards `Authorization`, and does not require custom FE proxy headers. FE should send `Content-Type: application/json` where a JSON body exists.

## Uploads

Instructor application: `POST /api/v1/instructors/applications` as `multipart/form-data`.

Required parts: `resume`, `educationalCertificates`, `governmentIdProof`, `passportPhoto`, `bankDetails`, `panDocument`.

Optional parts: `applicationBody`, `experienceLetter`, `internshipCertificate`, `skillCertificates`, `portfolio`, `demoLecturePpt`, `demoLectureRecording`, `projects`, `applicationForm`.

- Maximum individual file: 25 MB.
- Maximum request: 150 MB.
- Profile images accept `image/jpeg`, `image/png`, and `image/webp`.
- Instructor document MIME allowlisting is not implemented yet.
- Virus scanning is not implemented yet; therefore no virus-scan failure response exists. This remains a P1 release risk.

## Stable Test Accounts

All demo users use `Demo@12345`.

- Student: `demo.student1@cyberlearnix.com`
- Student: `demo.student2@cyberlearnix.com`
- Instructor: `demo.instructor1@cyberlearnix.com`
- Instructor: `demo.instructor2@cyberlearnix.com`
- Main admin: `mainadmin@cyberlearnix.com` / `MainAdmin@123`

Published public courses have IDs `1`, `2`, `3`, and `4`.