# Test Script for Admin-to-Instructor APIs through Gateway
# This script tests all admin instructor application endpoints

$baseGatewayUrl = "http://localhost:8080"
$adminServiceUrl = "http://localhost:8087"

Write-Host "=== ADMIN TO INSTRUCTOR API TEST SUITE ===" -ForegroundColor Cyan
Write-Host "Gateway URL: $baseGatewayUrl" -ForegroundColor Yellow
Write-Host "Admin Service URL: $adminServiceUrl" -ForegroundColor Yellow
Write-Host ""

# Step 1: Create a test admin user with known password
Write-Host "Step 1: Creating test admin user..." -ForegroundColor Yellow
$adminEmail = "testadmin@cyberlearnix.com"
$adminPassword = "Test@12345"

$createUserBody = @{
    email = $adminEmail
    password = $adminPassword
    firstName = "Test"
    lastName = "Admin"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseGatewayUrl/api/v1/auth/register" -Method POST -Body $createUserBody -ContentType "application/json" -ErrorAction SilentlyContinue
    Write-Host "✓ User created successfully" -ForegroundColor Green
} catch {
    Write-Host "ℹ User may already exist or registration endpoint is different" -ForegroundColor Cyan
}

# Step 2: Login to get JWT token
Write-Host "`nStep 2: Logging in to get JWT token..." -ForegroundColor Yellow
$loginBody = @{
    email = $adminEmail
    password = $adminPassword
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseGatewayUrl/api/v1/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $responseContent = $response.Content | ConvertFrom-Json
    $token = $responseContent.token
    Write-Host "✓ Login successful" -ForegroundColor Green
    Write-Host "Token preview: $($token.Substring(0, 50))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Login failed: $_" -ForegroundColor Red
    Write-Host "Attempting to use admin service directly..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri "$adminServiceUrl/api/v1/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
        $responseContent = $response.Content | ConvertFrom-Json
        $token = $responseContent.token
        Write-Host "✓ Direct login successful" -ForegroundColor Green
    } catch {
        Write-Host "✗ Direct login also failed. Using placeholder token for endpoint discovery." -ForegroundColor Red
        $token = "placeholder"
    }
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Step 3: Test Get All Applications (NEW - Industry Standard)
Write-Host "`nStep 3: Testing GET /api/v1/admin/instructors/applications (All applications)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$adminServiceUrl/api/v1/admin/instructors/applications?page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✓ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response preview: $($response.Content.Substring(0, 200))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# Step 4: Test Get Applications by Status - APPROVED (NEW - Industry Standard)
Write-Host "`nStep 4: Testing GET /api/v1/admin/instructors/applications?status=APPROVED" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$adminServiceUrl/api/v1/admin/instructors/applications?status=APPROVED&page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✓ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response preview: $($response.Content.Substring(0, 200))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# Step 5: Test Get Applications by Status - PENDING (NEW - Industry Standard)
Write-Host "`nStep 5: Testing GET /api/v1/admin/instructors/applications?status=PENDING" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$adminServiceUrl/api/v1/admin/instructors/applications?status=PENDING&page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✓ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response preview: $($response.Content.Substring(0, 200))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# Step 6: Test Get Applications by Status - REJECTED (NEW - Industry Standard)
Write-Host "`nStep 6: Testing GET /api/v1/admin/instructors/applications?status=REJECTED" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$adminServiceUrl/api/v1/admin/instructors/applications?status=REJECTED&page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✓ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response preview: $($response.Content.Substring(0, 200))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# Step 7: Test Get Applications by Status - Invalid Status (NEW - Industry Standard)
Write-Host "`nStep 7: Testing GET /api/v1/admin/instructors/applications?status=INVALID (Should return 400)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$adminServiceUrl/api/v1/admin/instructors/applications?status=INVALID&page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✗ Should have failed but got: $($response.StatusCode)" -ForegroundColor Red
} catch {
    Write-Host "✓ Correctly returned error (expected)" -ForegroundColor Green
}

# Step 8: Test Legacy Endpoint - APPROVED (Deprecated but should still work)
Write-Host "`nStep 8: Testing GET /api/v1/admin/instructors/applications/status/APPROVED (Legacy)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$adminServiceUrl/api/v1/admin/instructors/applications/status/APPROVED?page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✓ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response preview: $($response.Content.Substring(0, 200))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# Step 9: Test Gateway Route (if gateway is working)
Write-Host "`nStep 9: Testing Gateway route to admin service" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseGatewayUrl/adminservice/api/v1/admin/instructors/applications?page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✓ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response preview: $($response.Content.Substring(0, 200))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Gateway route failed: $_" -ForegroundColor Red
}

Write-Host "`n=== TEST SUITE COMPLETE ===" -ForegroundColor Cyan
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- New industry-standard query parameter endpoints: Tested" -ForegroundColor White
Write-Host "- Legacy path parameter endpoints: Tested" -ForegroundColor White
Write-Host "- Invalid status validation: Tested" -ForegroundColor White
Write-Host "- Gateway routing: Tested" -ForegroundColor White
