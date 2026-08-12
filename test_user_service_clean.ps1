# Clean User Service API Testing Script
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

# Get OTP from application logs
function Get-OtpFromLogs {
    param([string]$email)
    try {
        $logs = docker logs cyberlearnix-user-service --tail 100 2>&1
        # Match pattern: "OTP value for email@cyberlearnix.com is 123456"
        $otpMatch = $logs | Select-String -Pattern "OTP value for $email is (\d{6})"
        if ($otpMatch) {
            $otp = $otpMatch.Matches[0].Groups[1].Value
            return $otp
        }
        return $null
    } catch {
        Write-Host "Failed to get OTP from logs: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Test 1: Health Check
Write-Host "Testing Health Check..." -ForegroundColor Cyan
Test-Api -Method "GET" -Endpoint "/actuator/health" -ExpectedStatus "200" -Description "Health Check"

# Test 2: Register User
Write-Host "Testing User Registration..." -ForegroundColor Cyan
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$randomMobile = Get-Random -Minimum 1000000000 -Maximum 9999999999
$testEmail = "apitest_$timestamp@cyberlearnix.com"
Write-Host "Test Email: $testEmail" -ForegroundColor Yellow
Write-Host "Test Mobile: +1$randomMobile" -ForegroundColor Yellow

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
    
    # Test 3: Get OTP from logs (for testing purposes)
    Write-Host "Retrieving OTP from logs for testing..." -ForegroundColor Cyan
    Start-Sleep -Seconds 2
    $otp = Get-OtpFromLogs -email $testEmail
    
    if ($otp) {
        Write-Host "OTP retrieved: $otp" -ForegroundColor Green
        
        # Test 4: Verify Email with OTP
        Write-Host "Testing Email Verification..." -ForegroundColor Cyan
        $verifyOtpBody = @{
            email = $testEmail
            otp = $otp
            otpSessionId = $otpSessionId
        }
        $verifyResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/verify-email" -Body $verifyOtpBody -ExpectedStatus "200" -Description "Verify Email with OTP"
        
        if ($verifyResult.Pass) {
            Write-Host "Email verified successfully" -ForegroundColor Green
            
            # Test 5: Direct Login after verification
            Write-Host "Testing Direct Login..." -ForegroundColor Cyan
            $loginBody = @{
                email = $testEmail
                password = "Test@123456"
                identifier = $testEmail
            }
            $loginResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/login" -Body $loginBody -ExpectedStatus "200" -Description "Direct Login"
            
            if ($loginResult.Pass) {
                $loginResponse = $loginResult.Response | ConvertFrom-Json
                Write-Host "Login Response Structure: $($loginResponse | ConvertTo-Json -Depth 10)" -ForegroundColor Yellow
                
                # Handle different response structures
                if ($loginResponse.data) {
                    $accessToken = $loginResponse.data.accessToken
                    $refreshToken = $loginResponse.data.refreshToken
                    $userRole = $loginResponse.data.role
                } elseif ($loginResponse.accessToken) {
                    $accessToken = $loginResponse.accessToken
                    $refreshToken = $loginResponse.refreshToken
                    $userRole = $loginResponse.role
                } else {
                    Write-Host "Unable to extract tokens from response" -ForegroundColor Red
                    $accessToken = $null
                    $refreshToken = $null
                }
                
                if ($accessToken) {
                    Write-Host "Access Token obtained: $($accessToken.Substring(0, 20))..." -ForegroundColor Green
                    Write-Host "User Role: $userRole" -ForegroundColor Green
                } else {
                    Write-Host "Failed to extract access token, skipping authenticated tests" -ForegroundColor Red
                }
                
                # Test 6: Get Profile (only if we have a token)
                if ($accessToken) {
                    Write-Host "Testing Get Profile..." -ForegroundColor Cyan
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Profile"
                    
                    # Test 7: Update Profile
                    Write-Host "Testing Update Profile..." -ForegroundColor Cyan
                    $updateBody = @{
                        firstName = "API Updated"
                        lastName = "Test Updated"
                    }
                    Test-Api -Method "PUT" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $updateBody -ExpectedStatus "200" -Description "Update Profile"
                    
                    # Test 8: Get Sessions
                    Write-Host "Testing Get Sessions..." -ForegroundColor Cyan
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me/sessions" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Sessions"
                    
                    # Test 9: Refresh Token
                    Write-Host "Testing Refresh Token..." -ForegroundColor Cyan
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/refresh" -Headers @{"Authorization" = "Bearer $refreshToken"} -ExpectedStatus "200" -Description "Refresh Token"
                    
                    # Test 10: Change Password
                    Write-Host "Testing Change Password..." -ForegroundColor Cyan
                    $changePasswordBody = @{
                        currentPassword = "Test@123456"
                        newPassword = "NewTest@123456"
                        confirmPassword = "NewTest@123456"
                    }
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/change-password" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $changePasswordBody -ExpectedStatus "200" -Description "Change Password"
                    
                    # Test 11: Logout
                    Write-Host "Testing Logout..." -ForegroundColor Cyan
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/logout" -Headers @{"Authorization" = "Bearer $accessToken"} -Body @{"refreshToken" = $refreshToken} -ExpectedStatus "200" -Description "Logout"
                    
                    # Test 12: Test without token (should fail)
                    Write-Host "Testing unauthorized access..." -ForegroundColor Cyan
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -ExpectedStatus "401" -Description "Get Profile without token"
                    
                    # Test 13: Test with invalid token (should fail)
                    Write-Host "Testing invalid token..." -ForegroundColor Cyan
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer invalid_token"} -ExpectedStatus "401" -Description "Get Profile with invalid token"
                    
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
                        # Handle different response structures
                        if ($loginResponse2.data) {
                            $accessToken2 = $loginResponse2.data.accessToken
                        } elseif ($loginResponse2.accessToken) {
                            $accessToken2 = $loginResponse2.accessToken
                        }
                        
                        if ($accessToken2) {
                            $switchRoleBody = @{
                                targetRole = "INSTRUCTOR"
                            }
                            Test-Api -Method "POST" -Endpoint "/api/v1/auth/switch-role" -Headers @{"Authorization" = "Bearer $accessToken2"} -Body $switchRoleBody -ExpectedStatus "200" -Description "Switch Role"
                        }
                    }
                }
        } else {
            Write-Host "Email verification failed: $($verifyResult.Response)" -ForegroundColor Red
        }
    } else {
        Write-Host "Could not retrieve OTP for testing" -ForegroundColor Red
        Write-Host "Skipping tests that require authentication" -ForegroundColor Yellow
    }
} else {
    Write-Host "Registration failed: $($registerResult.Response)" -ForegroundColor Red
}

# Test 15: Login OTP Request
Write-Host "Testing Login OTP Request..." -ForegroundColor Cyan
$loginOtpBody = @{
    email = $testEmail
    identifier = $testEmail
}
Test-Api -Method "POST" -Endpoint "/api/v1/auth/login/otp/request" -Body $loginOtpBody -ExpectedStatus "200" -Description "Request Login OTP"

# Test 16: Forgot Password
Write-Host "Testing Forgot Password..." -ForegroundColor Cyan
$forgotPasswordBody = @{
    email = $testEmail
}
Test-Api -Method "POST" -Endpoint "/api/v1/auth/password/forgot" -Body $forgotPasswordBody -ExpectedStatus "200" -Description "Forgot Password"

# Test 17: Get all users (admin endpoint - should fail without token)
Write-Host "Testing Get All Users (Admin endpoint)..." -ForegroundColor Cyan
Test-Api -Method "GET" -Endpoint "/api/v1/users" -ExpectedStatus "401" -Description "Get All Users without admin token"

# Test 18: Get user by ID (should fail without token)
Write-Host "Testing Get User by ID (without token)..." -ForegroundColor Cyan
$testUserId = "00000000-0000-0000-0000-000000000000"
Test-Api -Method "GET" -Endpoint "/api/v1/users/$testUserId" -ExpectedStatus "401" -Description "Get User by ID without token"

# Test 19: Update user status (should fail without token)
Write-Host "Testing Update User Status (without token)..." -ForegroundColor Cyan
Test-Api -Method "PUT" -Endpoint "/api/v1/users/$testUserId/status" -Body @{"status" = "ACTIVE"} -ExpectedStatus "401" -Description "Update User Status without token"

# Test 20: Delete account (should fail without token)
Write-Host "Testing Delete Account (without token)..." -ForegroundColor Cyan
Test-Api -Method "DELETE" -Endpoint "/api/v1/users/me" -ExpectedStatus "401" -Description "Delete Account without token"

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
$results | Export-Csv -Path "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\user_service_clean_test_results.csv" -NoTypeInformation
Write-Host "`nResults exported to user_service_clean_test_results.csv" -ForegroundColor Cyan
