# Simple test to verify our fixes
$BaseUrl = "http://localhost:8080"

Write-Host "Testing Gateway Fixes:" -ForegroundColor Cyan
Write-Host ""

# Test 1: Course with Long ID
try {
    $r = Invoke-WebRequest -Uri "$BaseUrl/api/v1/courses/1" -UseBasicParsing
    if ($r.StatusCode -eq 200 -or $r.StatusCode -eq 404) {
        Write-Host "✅ Course Long ID: PASS (Status: $($r.StatusCode))" -ForegroundColor Green
    } else {
        Write-Host "❌ Course Long ID: FAIL (Status: $($r.StatusCode))" -ForegroundColor Red
    }
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 404) {
        Write-Host "✅ Course Long ID: PASS (Status: 404 - ID format accepted)" -ForegroundColor Green
    } else {
        Write-Host "❌ Course Long ID: FAIL (Status: $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Red
    }
}

# Test 2: Coupons (should require auth)
try {
    $r = Invoke-WebRequest -Uri "$BaseUrl/api/v1/coupons" -UseBasicParsing
    Write-Host "❌ Coupons: FAIL (Expected 401, got $($r.StatusCode))" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ Coupons: PASS (Status: 401 - requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ Coupons: FAIL (Status: $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Red
    }
}

# Test 3: Orders (should require auth)
try {
    $r = Invoke-WebRequest -Uri "$BaseUrl/api/v1/orders" -UseBasicParsing
    Write-Host "❌ Orders: FAIL (Expected 401/403, got $($r.StatusCode))" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401 -or $_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "✅ Orders: PASS (Status: $($_.Exception.Response.StatusCode.value__) - requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ Orders: FAIL (Status: $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Red
    }
}

# Test 4: Payments (should require auth)
try {
    $r = Invoke-WebRequest -Uri "$BaseUrl/api/v1/payments" -UseBasicParsing
    Write-Host "❌ Payments: FAIL (Expected 401, got $($r.StatusCode))" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ Payments: PASS (Status: 401 - requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ Payments: FAIL (Status: $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Red
    }
}

# Test 5: Notifications (should require auth)
try {
    $r = Invoke-WebRequest -Uri "$BaseUrl/api/v1/notifications" -UseBasicParsing
    Write-Host "❌ Notifications: FAIL (Expected 401, got $($r.StatusCode))" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ Notifications: PASS (Status: 401 - requires auth)" -ForegroundColor Green
    } else {
        Write-Host "❌ Notifications: FAIL (Status: $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Fixes verification complete" -ForegroundColor Cyan
