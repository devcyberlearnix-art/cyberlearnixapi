# Quick Gateway Test - Test the fixes we made
$ErrorActionPreference = "Continue"
$BaseUrl = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Quick Gateway Test - Testing Fixes" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Course Service (Long ID fix)
Write-Host "Test 1: Course Service with Long ID" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/v1/courses/1" -Method GET -UseBasicParsing
    Write-Host "✅ PASS: GET /api/v1/courses/1 - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 404) {
        Write-Host "✅ PASS: GET /api/v1/courses/1 - Status: 404 (course not found, but ID format accepted)" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL: GET /api/v1/courses/1 - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

# Test 2: Course Service sections (Long ID fix)
Write-Host "Test 2: Course Service sections with Long ID" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/v1/courses/1/sections" -Method GET -UseBasicParsing
    Write-Host "✅ PASS: GET /api/v1/courses/1/sections - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 404) {
        Write-Host "✅ PASS: GET /api/v1/courses/1/sections - Status: 404 (course not found, but ID format accepted)" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL: GET /api/v1/courses/1/sections - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

# Test 3: Coupon Service (should now return 401 since catalog fixed)
Write-Host "Test 3: Coupon Service (should require auth)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/v1/coupons" -Method GET -UseBasicParsing
    Write-Host "❌ FAIL: GET /api/v1/coupons - Expected 401, got $($response.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ PASS: GET /api/v1/coupons - Status: 401 (correctly requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL: GET /api/v1/coupons - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

# Test 4: Order Service (should now return 401/403 since catalog fixed)
Write-Host "Test 4: Order Service (should require auth)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/v1/orders" -Method GET -UseBasicParsing
    Write-Host "❌ FAIL: GET /api/v1/orders - Expected 401/403, got $($response.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401 -or $_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "✅ PASS: GET /api/v1/orders - Status: $($_.Exception.Response.StatusCode.value__) (correctly requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL: GET /api/v1/orders - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

# Test 5: Payment Service (should now return 401 since catalog fixed)
Write-Host "Test 5: Payment Service (should require auth)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/v1/payments" -Method GET -UseBasicParsing
    Write-Host "❌ FAIL: GET /api/v1/payments - Expected 401, got $($response.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ PASS: GET /api/v1/payments - Status: 401 (correctly requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL: GET /api/v1/payments - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

# Test 6: Notification Service (should return 401 since catalog fixed)
Write-Host "Test 6: Notification Service (should require auth)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/v1/notifications" -Method GET -UseBasicParsing
    Write-Host "❌ FAIL: GET /api/v1/notifications - Expected 401, got $($response.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ PASS: GET /api/v1/notifications - Status: 401 (correctly requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL: GET /api/v1/notifications - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Quick Gateway Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
