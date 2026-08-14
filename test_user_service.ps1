# User Service API Testing Script
$baseUrl = "http://localhost:8091"
$results = @()

function Test-Api {
    param(
        [string]$Method,
        [string]$Endpoint,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [string]$ExpectedStatus = "200",
        [string]$Description = ""
    )
    
    try {
        $url = "$baseUrl$Endpoint"
        $headersObj = @{}
        foreach ($key in $Headers.Keys) {
            $headersObj[$key] = $Headers[$key]
        }
        
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            $response = Invoke-WebRequest -Uri $url -Method $Method -Headers $headersObj -Body $jsonBody -ContentType "application/json" -UseBasicParsing
        } else {
            $response = Invoke-WebRequest -Uri $url -Method $Method -Headers $headersObj -UseBasicParsing
        }
        
        $result = @{
            Method = $Method
            Endpoint = $Endpoint
            Description = $Description
            ExpectedStatus = $ExpectedStatus
            ActualStatus = $response.StatusCode.ToString()
            Response = $response.Content
            Pass = $response.StatusCode.ToString() -eq $ExpectedStatus
        }
    } catch {
        $result = @{
            Method = $Method
            Endpoint = $Endpoint
            Description = $Description
            ExpectedStatus = $ExpectedStatus
            ActualStatus = $_.Exception.Response.StatusCode.value__
            Response = $_.Exception.Message
            Pass = $false
        }
    }
    
    $global:results += $result
    return $result
}

# Test 1: Health Check
Write-Host "Testing Health Check..." -ForegroundColor Cyan
Test-Api -Method "GET" -Endpoint "/actuator/health" -ExpectedStatus "200" -Description "Health Check"

# Test 2: Register User
Write-Host "Testing User Registration..." -ForegroundColor Cyan
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$registerBody = @{
    email = "apitest_$timestamp@cyberlearnix.com"
    password = "Test@123456"
    confirmPassword = "Test@123456"
    firstName = "API"
    lastName = "Test"
    mobileNumber = "1234567890"
    countryCode = "+1"
    dob = "1990-01-01"
    city = "Test City"
    state = "Test State"
    country = "Test Country"
    preferredLanguage = "English"
    organization = "Test Org"
    skills = @("Java", "Spring")
    fieldOfStudy = "Computer Science"
    highestQualification = "Bachelor"
    profilePhoto = ""
}

$registerResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/register" -Body $registerBody -ExpectedStatus "201" -Description "Register User"
$testEmail = $registerBody.email

# Test 3: Login with OTP
Write-Host "Testing Login OTP Request..." -ForegroundColor Cyan
$loginOtpBody = @{
    email = $testEmail
    identifier = $testEmail
}
Test-Api -Method "POST" -Endpoint "/api/v1/auth/login/otp/request" -Body $loginOtpBody -ExpectedStatus "200" -Description "Request Login OTP"

# Test 4: Direct Login
Write-Host "Testing Direct Login..." -ForegroundColor Cyan
$loginBody = @{
    email = $testEmail
    password = "Test@123456"
    identifier = $testEmail
}
$loginResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/login" -Body $loginBody -ExpectedStatus "200" -Description "Direct Login"

# Parse login response to get token
if ($loginResult.Pass) {
    $loginResponse = $loginResult.Response | ConvertFrom-Json
    $accessToken = $loginResponse.data.accessToken
    $refreshToken = $loginResponse.data.refreshToken
    
    Write-Host "Access Token obtained: $($accessToken.Substring(0, 20))..." -ForegroundColor Green
    
    # Test 5: Get Profile
    Write-Host "Testing Get Profile..." -ForegroundColor Cyan
    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Profile"
    
    # Test 6: Update Profile
    Write-Host "Testing Update Profile..." -ForegroundColor Cyan
    $updateBody = @{
        firstName = "API Updated"
        lastName = "Test Updated"
    }
    Test-Api -Method "PUT" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $updateBody -ExpectedStatus "200" -Description "Update Profile"
    
    # Test 7: Get Sessions
    Write-Host "Testing Get Sessions..." -ForegroundColor Cyan
    Test-Api -Method "GET" -Endpoint "/api/v1/users/me/sessions" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Sessions"
    
    # Test 8: Refresh Token
    Write-Host "Testing Refresh Token..." -ForegroundColor Cyan
    Test-Api -Method "POST" -Endpoint "/api/v1/auth/refresh" -Headers @{"Authorization" = "Bearer $refreshToken"} -ExpectedStatus "200" -Description "Refresh Token"
    
    # Test 9: Change Password
    Write-Host "Testing Change Password..." -ForegroundColor Cyan
    $changePasswordBody = @{
        currentPassword = "Test@123456"
        newPassword = "NewTest@123456"
        confirmPassword = "NewTest@123456"
    }
    Test-Api -Method "POST" -Endpoint "/api/v1/auth/change-password" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $changePasswordBody -ExpectedStatus "200" -Description "Change Password"
    
    # Test 10: Logout
    Write-Host "Testing Logout..." -ForegroundColor Cyan
    Test-Api -Method "POST" -Endpoint "/api/v1/auth/logout" -Headers @{"Authorization" = "Bearer $accessToken"} -Body @{"refreshToken" = $refreshToken} -ExpectedStatus "200" -Description "Logout"
    
    # Test 11: Test without token (should fail)
    Write-Host "Testing unauthorized access..." -ForegroundColor Cyan
    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -ExpectedStatus "401" -Description "Get Profile without token"
    
    # Test 12: Test with invalid token (should fail)
    Write-Host "Testing invalid token..." -ForegroundColor Cyan
    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer invalid_token"} -ExpectedStatus "401" -Description "Get Profile with invalid token"
}

# Test 13: Forgot Password
Write-Host "Testing Forgot Password..." -ForegroundColor Cyan
$forgotPasswordBody = @{
    email = $testEmail
}
Test-Api -Method "POST" -Endpoint "/api/v1/auth/password/forgot" -Body $forgotPasswordBody -ExpectedStatus "200" -Description "Forgot Password"

# Test 14: Switch Role (with new login)
Write-Host "Testing Role Switch..." -ForegroundColor Cyan
# Re-login after password change
$loginBody2 = @{
    email = $testEmail
    password = "NewTest@123456"
    identifier = $testEmail
}
$loginResult2 = Test-Api -Method "POST" -Endpoint "/api/v1/auth/login" -Body $loginBody2 -ExpectedStatus "200" -Description "Re-login for role switch"

if ($loginResult2.Pass) {
    $loginResponse2 = $loginResult2.Response | ConvertFrom-Json
    $accessToken2 = $loginResponse2.data.accessToken
    
    $switchRoleBody = @{
        targetRole = "INSTRUCTOR"
    }
    Test-Api -Method "POST" -Endpoint "/api/v1/auth/switch-role" -Headers @{"Authorization" = "Bearer $accessToken2"} -Body $switchRoleBody -ExpectedStatus "200" -Description "Switch Role"
}

# Generate Report
Write-Host "`n=== USER SERVICE API TEST RESULTS ===" -ForegroundColor Yellow
Write-Host "Total Tests: $($results.Count)" -ForegroundColor White
Write-Host "Passed: $($results.Where({$_.Pass}).Count)" -ForegroundColor Green
Write-Host "Failed: $($results.Where({-not $_.Pass}).Count)" -ForegroundColor Red

Write-Host "`nDetailed Results:" -ForegroundColor Yellow
$results | ForEach-Object {
    $color = if ($_.Pass) { "Green" } else { "Red" }
    Write-Host "[$($_.Method)] $($_.Endpoint) - $($_.Description)" -ForegroundColor $color
    Write-Host "  Expected: $($_.ExpectedStatus), Actual: $($_.ActualStatus)" -ForegroundColor White
    if (-not $_.Pass) {
        Write-Host "  Response: $($_.Response)" -ForegroundColor Red
    }
}

# Export to CSV
$results | Export-Csv -Path "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\user_service_test_results.csv" -NoTypeInformation
Write-Host "`nResults exported to user_service_test_results.csv" -ForegroundColor Cyan
