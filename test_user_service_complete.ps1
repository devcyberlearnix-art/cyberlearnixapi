# Complete User Service API Testing Script
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
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "000" }
        $result = @{
            Method = $Method
            Endpoint = $Endpoint
            Description = $Description
            ExpectedStatus = $ExpectedStatus
            ActualStatus = $statusCode
            Response = if ($_.Exception.Response) { 
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $reader.ReadToEnd() 
            } else { $_.Exception.Message }
            Pass = $false
        }
    }
    
    $global:results += $result
    return $result
}

function Get-OtpFromLogs {
    param([string]$email)
    try {
        $logs = docker logs cyberlearnix-user-service --tail 100 2>&1
        $otpMatch = $logs | Select-String -Pattern "OTP value for $email is (\d{6})"
        if ($otpMatch) {
            $otp = $otpMatch.Matches[0].Groups[1].Value
            return $otp
        }
        return $null
    } catch {
        return $null
    }
}

function Get-OtpFromMailhog {
    param([string]$email)
    try {
        $mailhogResponse = Invoke-WebRequest -Uri "http://localhost:8025/api/v2/messages" -UseBasicParsing
        $messages = $mailhogResponse.Content | ConvertFrom-Json
        
        foreach ($msg in $messages.items) {
            if ($msg.from.address -eq "noreply@cyberlearnix.com" -and $msg.to[0].address -eq $email) {
                $subject = $msg.subject
                $body = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($msg.content[0].body))
                
                $otpMatch = $body | Select-String -Pattern "\b\d{6}\b"
                if ($otpMatch) {
                    return $otpMatch.Matches[0].Value
                }
            }
        }
        return $null
    } catch {
        return $null
    }
}

# Test 1: Health Check
Write-Host "=== Testing Health Check ===" -ForegroundColor Cyan
Test-Api -Method "GET" -Endpoint "/actuator/health" -ExpectedStatus "200" -Description "Health Check"

# Test 2: Register User
Write-Host "=== Testing User Registration ===" -ForegroundColor Cyan
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$randomMobile = Get-Random -Minimum 1000000000 -Maximum 9999999999
$testEmail = "apitest_$timestamp@cyberlearnix.com"
Write-Host "Test Email: $testEmail" -ForegroundColor Yellow

$registerBody = @{
    email = $testEmail
    password = "Test@123456"
    confirmPassword = "Test@123456"
    firstName = "API"
    lastName = "Test"
    mobileNumber = $randomMobile.ToString()
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

if ($registerResult.Pass) {
    $registerData = $registerResult.Response | ConvertFrom-Json
    $otpSessionId = $registerData.data.otpSessionId
    Write-Host "OTP Session ID: $otpSessionId" -ForegroundColor Green
    
    Start-Sleep -Seconds 2
    $otp = Get-OtpFromLogs -email $testEmail
    
    if ($otp) {
        Write-Host "OTP retrieved: $otp" -ForegroundColor Green
        
        # Test 3: Verify Email with OTP
        Write-Host "=== Testing Email Verification ===" -ForegroundColor Cyan
        $verifyOtpBody = @{
            email = $testEmail
            otp = $otp
            otpSessionId = $otpSessionId
        }
        $verifyResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/verify-email" -Body $verifyOtpBody -ExpectedStatus "200" -Description "Verify Email with OTP"
        
        if ($verifyResult.Pass) {
            Write-Host "Email verified successfully" -ForegroundColor Green
            
            # Test 4: Direct Login
            Write-Host "=== Testing Direct Login ===" -ForegroundColor Cyan
            $loginBody = @{
                email = $testEmail
                password = "Test@123456"
                identifier = $testEmail
            }
            $loginResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/login" -Body $loginBody -ExpectedStatus "200" -Description "Direct Login"
            
            if ($loginResult.Pass) {
                $loginResponse = $loginResult.Response | ConvertFrom-Json
                
                # Extract tokens based on actual response structure
                if ($loginResponse.authentication) {
                    $accessToken = $loginResponse.authentication.accessToken
                    $refreshToken = $loginResponse.authentication.refreshToken
                } elseif ($loginResponse.data) {
                    $accessToken = $loginResponse.data.accessToken
                    $refreshToken = $loginResponse.data.refreshToken
                } elseif ($loginResponse.accessToken) {
                    $accessToken = $loginResponse.accessToken
                    $refreshToken = $loginResponse.refreshToken
                }
                
                if ($accessToken) {
                    Write-Host "Access Token obtained" -ForegroundColor Green
                    Write-Host "Refresh Token obtained" -ForegroundColor Green
                    
                    # Test 5: Get Profile
                    Write-Host "=== Testing Get User Profile ===" -ForegroundColor Cyan
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Profile"
                    
                    # Test 6: Update Profile
                    Write-Host "=== Testing Update Profile ===" -ForegroundColor Cyan
                    $updateBody = @{ firstName = "API Updated"; lastName = "Test Updated" }
                    Test-Api -Method "PUT" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $updateBody -ExpectedStatus "200" -Description "Update Profile"
                    
                    # Test 7: Get Sessions
                    Write-Host "=== Testing Get User Sessions ===" -ForegroundColor Cyan
                    $sessionsResult = Test-Api -Method "GET" -Endpoint "/api/v1/users/me/sessions" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Sessions"
                    
                    # Test 8: Refresh Token
                    Write-Host "=== Testing Refresh Token ===" -ForegroundColor Cyan
                    $refreshResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/refresh" -Headers @{"Authorization" = "Bearer $refreshToken"} -ExpectedStatus "200" -Description "Refresh Token"
                    
                    if ($refreshResult.Pass) {
                        $refreshResponse = $refreshResult.Response | ConvertFrom-Json
                        if ($refreshResponse.authentication) {
                            $newAccessToken = $refreshResponse.authentication.accessToken
                            Write-Host "New Access Token obtained" -ForegroundColor Green
                            
                            # Test 9: Use new access token
                            Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $newAccessToken"} -ExpectedStatus "200" -Description "Get Profile with new token"
                        }
                    }
                    
                    # Test 10: Change Password
                    Write-Host "=== Testing Change Password ===" -ForegroundColor Cyan
                    $changePasswordBody = @{
                        currentPassword = "Test@123456"
                        newPassword = "NewTest@123456"
                        confirmPassword = "NewTest@123456"
                    }
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/change-password" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $changePasswordBody -ExpectedStatus "200" -Description "Change Password"
                    
                    # Test 11: Logout
                    Write-Host "=== Testing Logout ===" -ForegroundColor Cyan
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/logout" -Headers @{"Authorization" = "Bearer $accessToken"} -Body @{"refreshToken" = $refreshToken} -ExpectedStatus "200" -Description "Logout"
                    
                    # Test 12: Unauthorized access after logout
                    Write-Host "=== Testing Unauthorized Access After Logout ===" -ForegroundColor Cyan
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "401" -Description "Get Profile after logout"
                    
                    # Re-login for further tests
                    Write-Host "=== Re-login for further tests ===" -ForegroundColor Cyan
                    $loginBody = @{
                        email = $testEmail
                        password = "NewTest@123456"
                        identifier = $testEmail
                    }
                    $loginResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/login" -Body $loginBody -ExpectedStatus "200" -Description "Login with new password"
                    
                    if ($loginResult.Pass) {
                        $loginResponse = $loginResult.Response | ConvertFrom-Json
                        if ($loginResponse.authentication) {
                            $accessToken = $loginResponse.authentication.accessToken
                            $refreshToken = $loginResponse.authentication.refreshToken
                        }
                    }
                }
            }
        }
    }
}

# Test 13: Login OTP Request
Write-Host "=== Testing Login OTP Request ===" -ForegroundColor Cyan
Test-Api -Method "POST" -Endpoint "/api/v1/auth/login/otp/request" -Body @{email = $testEmail; identifier = $testEmail} -ExpectedStatus "200" -Description "Request Login OTP"

# Test 14: Forgot Password
Write-Host "=== Testing Forgot Password ===" -ForegroundColor Cyan
Test-Api -Method "POST" -Endpoint "/api/v1/auth/password/forgot" -Body @{email = $testEmail} -ExpectedStatus "200" -Description "Forgot Password"

# Test 15: Get All Users (no auth)
Write-Host "=== Testing Get All Users without auth ===" -ForegroundColor Cyan
Test-Api -Method "GET" -Endpoint "/api/v1/users" -ExpectedStatus "401" -Description "Get All Users without admin token"

# Test 16: Get User by ID (no auth)
Write-Host "=== Testing Get User by ID without auth ===" -ForegroundColor Cyan
Test-Api -Method "GET" -Endpoint "/api/v1/users/00000000-0000-0000-0000-000000000000" -ExpectedStatus "401" -Description "Get User by ID without token"

# Test 17: Update User Status (no auth)
Write-Host "=== Testing Update User Status without auth ===" -ForegroundColor Cyan
Test-Api -Method "PUT" -Endpoint "/api/v1/users/00000000-0000-0000-0000-000000000000/status" -Body @{status = "INACTIVE"} -ExpectedStatus "401" -Description "Update User Status without token"

# Test 18: Delete Account (no auth)
Write-Host "=== Testing Delete Account without auth ===" -ForegroundColor Cyan
Test-Api -Method "DELETE" -Endpoint "/api/v1/users/me" -ExpectedStatus "401" -Description "Delete Account without token"

# Test 19: Switch Role (with auth)
Write-Host "=== Testing Switch Role ===" -ForegroundColor Cyan
if ($accessToken) {
    Test-Api -Method "POST" -Endpoint "/api/v1/auth/switch-role" -Headers @{"Authorization" = "Bearer $accessToken"} -Body @{newRole = "INSTRUCTOR"} -ExpectedStatus "200" -Description "Switch Role"
}

# Test 20: OAuth Endpoints
Write-Host "=== Testing OAuth Endpoints ===" -ForegroundColor Cyan
Test-Api -Method "POST" -Endpoint "/api/v1/auth/google/login" -Body @{token = "fake_token"} -ExpectedStatus "401" -Description "Google OAuth (should fail without real token)"
Test-Api -Method "POST" -Endpoint "/api/v1/auth/github/login" -Body @{code = "fake_code"} -ExpectedStatus "401" -Description "GitHub OAuth (should fail without real code)"
Test-Api -Method "POST" -Endpoint "/api/v1/auth/linkedin/login" -Body @{code = "fake_code"} -ExpectedStatus "401" -Description "LinkedIn OAuth (should fail without real code)"

# Generate Report
Write-Host "=== TEST RESULTS ===" -ForegroundColor Cyan
$passed = ($results | Where-Object { $_.Pass -eq $true }).Count
$failed = ($results | Where-Object { $_.Pass -eq $false }).Count
$total = $results.Count

Write-Host "Total Tests: $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red
Write-Host "Pass Percentage: $([math]::Round(($passed / $total) * 100, 2))%" -ForegroundColor Yellow

Write-Host "`n=== FAILED TESTS ===" -ForegroundColor Red
foreach ($result in $results) {
    if (-not $result.Pass) {
        Write-Host "$($result.Method) $($result.Endpoint) - Expected: $($result.ExpectedStatus), Actual: $($result.ActualStatus)" -ForegroundColor Red
        Write-Host "Response: $($result.Response)" -ForegroundColor Gray
    }
}

# Export results to JSON
$results | ConvertTo-Json -Depth 10 | Out-File -FilePath "user_service_test_results.json"
Write-Host "`nResults exported to user_service_test_results.json" -ForegroundColor Green
