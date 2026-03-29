# CyberLearnix API Test Script
# Usage: .\test-apis.ps1
# Prerequisite: docker compose up -d  (all services must be running)

$GW = "http://localhost:8080"
$ErrorActionPreference = "SilentlyContinue"

function Write-Section($title) {
    Write-Host "`n$("="*60)" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host $("="*60) -ForegroundColor Cyan
}

function Test-Api($method, $url, $body, $headers, $description) {
    Write-Host "`n>> $description" -ForegroundColor Yellow
    Write-Host "   $method $url" -ForegroundColor Gray
    try {
        $params = @{ Method = $method; Uri = $url; ErrorAction = "Stop" }
        if ($body)    { $params.Body = ($body | ConvertTo-Json); $params.ContentType = "application/json" }
        if ($headers) { $params.Headers = $headers }
        $resp = Invoke-WebRequest @params -UseBasicParsing
        $respBody = $resp.Content | ConvertFrom-Json -ErrorAction SilentlyContinue
        Write-Host "   Status: $($resp.StatusCode) OK" -ForegroundColor Green
        if ($respBody) { Write-Host "   Body: $($respBody | ConvertTo-Json -Compress)" -ForegroundColor White }
        return $respBody
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        Write-Host "   Status: $code FAILED" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "1. HEALTH CHECKS (via API Gateway)"
# ─────────────────────────────────────────────────────────────────────────────

Test-Api "GET" "$GW/actuator/health"              $null $null "Gateway health"
Test-Api "GET" "$GW/users/actuator/health"         $null $null "Userservice health (via gateway)"
Test-Api "GET" "$GW/courses/actuator/health"       $null $null "Course-service health (via gateway)"
Test-Api "GET" "$GW/api/cart/actuator/health"      $null $null "Cart-service health (via gateway)"
Test-Api "GET" "$GW/api/coupons/actuator/health"   $null $null "Coupon-service health (via gateway)"

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "2. USER AUTH — Register"
# ─────────────────────────────────────────────────────────────────────────────

$registerBody = @{
    firstName = "Test"
    lastName  = "User"
    email     = "testuser@cyberlearnix.com"
    password  = "Test@12345"
    phone     = "+919876543210"
}
$registerResp = Test-Api "POST" "$GW/auth/register" $registerBody $null "Register new user"

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "3. USER AUTH — Login"
# ─────────────────────────────────────────────────────────────────────────────

$loginBody = @{
    email    = "testuser@cyberlearnix.com"
    password = "Test@12345"
}
$loginResp = Test-Api "POST" "$GW/auth/login" $loginBody $null "Login and get JWT"

$token = $null
if ($loginResp -and $loginResp.data -and $loginResp.data.accessToken) {
    $token = $loginResp.data.accessToken
    Write-Host "   JWT Token acquired (${token.Substring(0,20)}...)" -ForegroundColor Green
} elseif ($loginResp -and $loginResp.accessToken) {
    $token = $loginResp.accessToken
    Write-Host "   JWT Token acquired (${token.Substring(0,20)}...)" -ForegroundColor Green
} else {
    Write-Host "   WARNING: Could not extract token from login response" -ForegroundColor DarkYellow
}

$authHeaders = if ($token) { @{ Authorization = "Bearer $token" } } else { @{} }

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "4. USER — Get Profile (authenticated)"
# ─────────────────────────────────────────────────────────────────────────────

Test-Api "GET" "$GW/api/v1/profile" $null $authHeaders "Get user profile"

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "5. COURSE SERVICE — Public endpoints"
# ─────────────────────────────────────────────────────────────────────────────

Test-Api "GET" "$GW/courses"          $null $null        "List all courses (public)"
Test-Api "GET" "$GW/courses?page=0&size=5" $null $authHeaders "List courses paginated (auth)"

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "6. CART SERVICE — Authenticated endpoints"
# ─────────────────────────────────────────────────────────────────────────────

Test-Api "GET" "$GW/api/cart" $null $authHeaders "Get cart"

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "7. COUPON SERVICE — Authenticated endpoints"
# ─────────────────────────────────────────────────────────────────────────────

Test-Api "GET" "$GW/api/coupons" $null $authHeaders "List all coupons"

# ─────────────────────────────────────────────────────────────────────────────
Write-Section "8. SWAGGER UI LINKS"
# ─────────────────────────────────────────────────────────────────────────────

Write-Host ""
Write-Host "  API Documentation (open in browser):" -ForegroundColor Cyan
Write-Host "  Gateway Swagger:   $GW/swagger-ui.html" -ForegroundColor White
Write-Host "  Userservice:       http://localhost:8086/swagger-ui.html" -ForegroundColor White
Write-Host "  Course-service:    http://localhost:8083/swagger-ui.html" -ForegroundColor White
Write-Host "  Cart-service:      http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "  Coupon-service:    http://localhost:8082/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "Done." -ForegroundColor Green
