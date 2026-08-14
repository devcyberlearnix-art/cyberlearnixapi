# Manual API Testing Script
$BaseUrl = "http://localhost:8080"
$Results = @()

function Test-Endpoint {
    param(
        [string]$Service,
        [string]$Method,
        [string]$Endpoint,
        [string]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    $Url = $BaseUrl + $Endpoint
    try {
        $BodyJson = if ($Body) { $Body | ConvertTo-Json } else { $null }
        $Response = Invoke-WebRequest -Uri $Url -Method $Method -Body $BodyJson -Headers $Headers -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
        @{
            Service = $Service
            Method = $Method
            Endpoint = $Endpoint
            Status = "PASS"
            HttpStatus = $Response.StatusCode
            Response = $Response.Content
        }
    } catch {
        $HttpStatus = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        $ErrorResponse = if ($_.Exception.Response) {
            $Stream = $_.Exception.Response.GetResponseStream()
            $Reader = New-Object System.IO.StreamReader($Stream)
            $Reader.ReadToEnd()
        } else { $_.Exception.Message }
        
        @{
            Service = $Service
            Method = $Method
            Endpoint = $Endpoint
            Status = "FAIL"
            HttpStatus = $HttpStatus
            Response = $ErrorResponse
        }
    }
}

Write-Host "Testing Public Endpoints..." -ForegroundColor Cyan

# User Service - Registration
$Results += Test-Endpoint -Service "user-service" -Method "POST" -Endpoint "/api/v1/auth/register" -Body @{
    firstName = "Test"
    lastName = "User"
    email = "test_$(Get-Date -Format 'yyyyMMddHHmmss')@cyberlearnix.com"
    password = "Test@123456"
    confirmPassword = "Test@123456"
    countryCode = "+91"
    mobileNumber = "9" + (Get-Date -Format 'HHmmss')
    skills = @("Testing")
}

# User Service - Login OTP Request
$Results += Test-Endpoint -Service "user-service" -Method "POST" -Endpoint "/api/v1/auth/login/otp/request" -Body @{
    email = "test@example.com"
}

# User Service - Forgot Password
$Results += Test-Endpoint -Service "user-service" -Method "POST" -Endpoint "/api/v1/auth/password/forgot" -Body @{
    email = "test@example.com"
}

# Course Service - Public endpoints
$Results += Test-Endpoint -Service "course-service" -Method "GET" -Endpoint "/api/v1/courses"
$Results += Test-Endpoint -Service "course-service" -Method "GET" -Endpoint "/api/v1/courses/123e4567-e89b-12d3-a456-426614174000"
$Results += Test-Endpoint -Service "course-service" -Method "GET" -Endpoint "/api/v1/courses/123e4567-e89b-12d3-a456-426614174000/sections"

# Coupon Service - Public endpoints
$Results += Test-Endpoint -Service "coupon-service" -Method "GET" -Endpoint "/api/v1/coupons"
$Results += Test-Endpoint -Service "coupon-service" -Method "GET" -Endpoint "/api/v1/coupons/validate/TEST123"
$Results += Test-Endpoint -Service "coupon-service" -Method "GET" -Endpoint "/api/v1/coupons/campaigns"

# Notification Service - Public endpoints
$Results += Test-Endpoint -Service "notification-service" -Method "GET" -Endpoint "/api/v1/system/health"
$Results += Test-Endpoint -Service "notification-service" -Method "GET" -Endpoint "/api/v1/notifications"
$Results += Test-Endpoint -Service "notification-service" -Method "GET" -Endpoint "/api/v1/templates"
$Results += Test-Endpoint -Service "notification-service" -Method "GET" -Endpoint "/api/v1/preferences"

# Order Service - Public endpoints
$Results += Test-Endpoint -Service "order-service" -Method "GET" -Endpoint "/api/v1/orders"
$Results += Test-Endpoint -Service "order-service" -Method "GET" -Endpoint "/api/v1/orders/123"

# Payment Service - Public endpoints
$Results += Test-Endpoint -Service "payment-service" -Method "GET" -Endpoint "/api/v1/payments"
$Results += Test-Endpoint -Service "payment-service" -Method "GET" -Endpoint "/api/v1/test/courses"

Write-Host ""
Write-Host "Testing Protected Endpoints (without auth)..." -ForegroundColor Cyan

# These should fail with 401/403
$Results += Test-Endpoint -Service "user-service" -Method "GET" -Endpoint "/api/v1/users/me"
$Results += Test-Endpoint -Service "cart-service" -Method "GET" -Endpoint "/api/v1/cart"
$Results += Test-Endpoint -Service "wishlist-service" -Method "GET" -Endpoint "/api/v1/wishlist"
$Results += Test-Endpoint -Service "user-service" -Method "GET" -Endpoint "/api/v1/users"
$Results += Test-Endpoint -Service "admin-service" -Method "GET" -Endpoint "/api/v1/admin/courses"

Write-Host ""
Write-Host "Testing RBAC (should fail with wrong role)..." -ForegroundColor Cyan

# User Service - Admin endpoints (should fail without admin role)
$Results += Test-Endpoint -Service "user-service" -Method "GET" -Endpoint "/api/v1/admin/instructors"

# Course Service - Instructor endpoints (should fail without instructor role)
$Results += Test-Endpoint -Service "course-service" -Method "POST" -Endpoint "/api/v1/courses" -Body @{
    title = "Test Course"
    description = "Test"
}

Write-Host ""
Write-Host "Generating Report..." -ForegroundColor Cyan

$Passed = ($Results | Where-Object { $_.Status -eq "PASS" }).Count
$Failed = ($Results | Where-Object { $_.Status -eq "FAIL" }).Count
$Total = $Results.Count

Write-Host "Total Tests: $Total" -ForegroundColor Cyan
Write-Host "Passed: $Passed" -ForegroundColor Green
Write-Host "Failed: $Failed" -ForegroundColor Red
Write-Host "Success Rate: $([math]::Round(($Passed/$Total)*100, 2))%" -ForegroundColor Cyan

# Save results
$Results | ConvertTo-Json -Depth 10 | Out-File -FilePath "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\MANUAL_TEST_RESULTS.json" -Encoding UTF8

# Generate markdown report
$Markdown = "# Manual API Test Results`n`n"
$Markdown += "**Test Date:** $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n`n"
$Markdown += "## Summary`n`n"
$Markdown += "- **Total Tests:** $Total`n"
$Markdown += "- **Passed:** $Passed`n"
$Markdown += "- **Failed:** $Failed`n"
$Markdown += "- **Success Rate:** $([math]::Round(($Passed/$Total)*100, 2))%`n`n"
$Markdown += "## Test Results`n`n"
$Markdown += "| Service | Method | Endpoint | Status | HTTP Status |`n"
$Markdown += "|---------|--------|----------|--------|-------------|`n"

foreach ($Result in $Results) {
    $Icon = if ($Result.Status -eq "PASS") { "✅" } else { "❌" }
    $Markdown += "| $($Result.Service) | $($Result.Method) | $($Result.Endpoint) | $Icon $($Result.Status) | $($Result.HttpStatus) |`n"
}

$Markdown | Out-File -FilePath "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\MANUAL_TEST_RESULTS.md" -Encoding UTF8

Write-Host "Results saved to MANUAL_TEST_RESULTS.json and MANUAL_TEST_RESULTS.md" -ForegroundColor Green