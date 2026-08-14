# Quick test to check if Gateway routing is working
Start-Sleep -Seconds 5

# Test 1: Course service (should work)
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/courses" -Method GET -UseBasicParsing
    Write-Host "✅ Course service routing working - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Course service routing failed - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

# Test 2: Notification system health (new route)
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/system/health" -Method GET -UseBasicParsing
    Write-Host "✅ Notification system health routing working - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Notification system health routing failed - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

# Test 3: Notification templates (new route)
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/templates" -Method GET -UseBasicParsing
    Write-Host "✅ Notification templates routing working - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Notification templates routing failed - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

# Test 4: Notification preferences (new route)
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/preferences" -Method GET -UseBasicParsing
    Write-Host "✅ Notification preferences routing working - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Notification preferences routing failed - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

# Test 5: Payment test courses (new route)
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/test/courses" -Method GET -UseBasicParsing
    Write-Host "✅ Payment test courses routing working - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Payment test courses routing failed - Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}
