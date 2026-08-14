# Manual API Test Results

**Test Date:** 2026-08-10 22:04:56

## Summary

- **Total Tests:** 24
- **Passed:** 6
- **Failed:** 23
- **Success Rate:** 25%

## Test Results

| Service | Method | Endpoint | Status | HTTP Status |
|---------|--------|----------|--------|-------------|
| user-service | POST | /api/v1/auth/register | âŒ FAIL | 500 |
| user-service | POST | /api/v1/auth/login/otp/request | âŒ FAIL | 500 |
| user-service | POST | /api/v1/auth/password/forgot | âŒ FAIL | 500 |
| course-service | GET | /api/v1/courses | âœ… PASS | 200 |
| course-service | GET | /api/v1/courses/123e4567-e89b-12d3-a456-426614174000 | âŒ FAIL | 400 |
| course-service | GET | /api/v1/courses/123e4567-e89b-12d3-a456-426614174000/sections | âŒ FAIL | 400 |
| coupon-service | GET | /api/v1/coupons | âŒ FAIL | 401 |
| coupon-service | GET | /api/v1/coupons/validate/TEST123 | âŒ FAIL | 401 |
| coupon-service | GET | /api/v1/coupons/campaigns | âŒ FAIL | 401 |
| notification-service | GET | /api/v1/system/health | âŒ FAIL | 404 |
| notification-service | GET | /api/v1/notifications | âŒ FAIL | 401 |
| notification-service | GET | /api/v1/templates | âŒ FAIL | 404 |
| notification-service | GET | /api/v1/preferences | âŒ FAIL | 404 |
| order-service | GET | /api/v1/orders | âŒ FAIL | 403 |
| order-service | GET | /api/v1/orders/123 | âŒ FAIL | 403 |
| payment-service | GET | /api/v1/payments | âŒ FAIL | 401 |
| payment-service | GET | /api/v1/test/courses | âŒ FAIL | 404 |
| user-service | GET | /api/v1/users/me | âŒ FAIL | 401 |
| cart-service | GET | /api/v1/cart | âŒ FAIL | 401 |
| wishlist-service | GET | /api/v1/wishlist | âŒ FAIL | 401 |
| user-service | GET | /api/v1/users | âŒ FAIL | 401 |
| admin-service | GET | /api/v1/admin/courses | âŒ FAIL | 401 |
| user-service | GET | /api/v1/admin/instructors | âŒ FAIL | 401 |
| course-service | POST | /api/v1/courses | âŒ FAIL | 403 |

