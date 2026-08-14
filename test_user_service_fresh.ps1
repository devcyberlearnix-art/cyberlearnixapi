# Fresh User Service API Testing Script
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

# Test 1: Health Check
Write-Host "Testing Health Check..." -ForegroundColor Cyan
Test-Api -Method "GET" -Endpoint "/actuator/health" -ExpectedStatus "200" -Description "Health Check"

# Test 2: Register User
Write-Host "Testing User Registration..." -ForegroundColor Cyan
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
        Write-Host "Testing Email Verification..." -ForegroundColor Cyan
        $verifyOtpBody = @{
            email = $testEmail
            otp = $otp
            otpSessionId = $otpSessionId
        }
        $verifyResult = Test-Api -Method "POST" -Endpoint "/api/v1/auth/verify-email" -Body $verifyOtpBody -ExpectedStatus "200" -Description "Verify Email with OTP"
        
        if ($verifyResult.Pass) {
            Write-Host "Email verified successfully" -ForegroundColor Green
            
            # Test 4: Direct Login
            Write-Host "Testing Direct Login..." -ForegroundColor Cyan
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
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Profile"
                    
                    # Test 6: Update Profile
                    $updateBody = @{ firstName = "API Updated"; lastName = "Test Updated" }
                    Test-Api -Method "PUT" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $updateBody -ExpectedStatus "200" -Description "Update Profile"
                    
                    # Test 7: Get Sessions
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me/sessions" -Headers @{"Authorization" = "Bearer $accessToken"} -ExpectedStatus "200" -Description "Get User Sessions"
                    
                    # Test 8: Refresh Token
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/refresh" -Headers @{"Authorization" = "Bearer $refreshToken"} -ExpectedStatus "200" -Description "Refresh Token"
                    
                    # Test 9: Change Password
                    $changePasswordBody = @{
                        currentPassword = "Test@123456"
                        newPassword = "NewTest@123456"
                        confirmPassword = "NewTest@123456"
                    }
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/change-password" -Headers @{"Authorization" = "Bearer $accessToken"} -Body $changePasswordBody -ExpectedStatus "200" -Description "Change Password"
                    
                    # Test 10: Logout
                    Test-Api -Method "POST" -Endpoint "/api/v1/auth/logout" -Headers @{"Authorization" = "Bearer $accessToken"} -Body @{"refreshToken" = $refreshToken} -ExpectedStatus "200" -Description "Logout"
                    
                    # Test 11: Unauthorized access
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -ExpectedStatus "401" -Description "Get Profile without token"
                    
                    # Test 12: Invalid token
                    Test-Api -Method "GET" -Endpoint "/api/v1/users/me" -Headers @{"Authorization" = "Bearer invalid_token"} -ExpectedStatus "401" -Description "Get Profile with invalid token"
                }
            }
        }
    }
}

# Test 13: Login OTP Request
Test-Api -Method "POST" -Endpoint "/api/v1/auth/login/otp/request" -Body @{email = $testEmail; identifier = $testEmail} -ExpectedStatus "200" -Description "Request Login OTP"

# Test 14: Forgot Password
Test-Api -Method "POST" -Endpoint "/api/v1/auth/password/forgot" -Body @{email = $testEmail} -ExpectedStatus "200" -Description "Forgot Password"

# Test 15: Get All Users (no auth)
Test-Api -Method "GET" -Endpoint "/api/v1/users" -ExpectedStatus "401" -Description "Get All Users without admin token"

# Test 16: Get User by ID (no auth)
Test-Api -Method "GET" -Endpoint "/api/v1/users/00000000-0000-0000-0000-000000000000" -ExpectedStatus "401" -Description "Get User by ID without token"

# Test 17: Update User Status (no auth)
Test-Api -Method "PUT" -Endpoint "/api/v1/users/00000000-0000-0000-0000-000000000000/status" -Body @{"status" = "ACTIVE"} -ExpectedStatus "401" -Description "Update User Status without token"

# Test 18: Delete Account (no auth)
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

$results | Export-Csv -Path "C:\Users\SHIVASAI\OneDrive\Desktop\LMS\user_service_fresh_test_results.csv" -NoTypeInformation
Write-Host "`nResults exported to user_service_fresh_test_results.csv" -ForegroundColor Cyan
