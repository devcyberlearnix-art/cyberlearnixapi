# Detailed Login Test
$baseUrl = "http://localhost:8091"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$testEmail = "apitest_$timestamp@cyberlearnix.com"

Write-Host "Test Email: $testEmail"

# Step 1: Register
Write-Host "`n=== Step 1: Register ===" -ForegroundColor Cyan
$registerBody = @{
    email = $testEmail
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

try {
    $registerResponse = Invoke-WebRequest -Uri "$baseUrl/api/v1/auth/register" -Method POST -Body ($registerBody | ConvertTo-Json -Depth 10) -ContentType "application/json" -UseBasicParsing
    Write-Host "Register Status: $($registerResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Register Response: $($registerResponse.Content)" -ForegroundColor White
    
    $registerData = $registerResponse.Content | ConvertFrom-Json
    $otpSessionId = $registerData.data.otpSessionId
    Write-Host "OTP Session ID: $otpSessionId" -ForegroundColor Yellow
} catch {
    Write-Host "Register Failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    exit
}

# Step 2: Check database for user status
Write-Host "`n=== Step 2: Check Database ===" -ForegroundColor Cyan
try {
    $dbCheck = docker exec cyberlearnix-postgres psql -U cyberlearnix -d lms_user_db -c "SELECT id, email, status, role, is_instructor_approved FROM users WHERE email = '$testEmail';"
    Write-Host "Database Record:" -ForegroundColor White
    Write-Host $dbCheck
} catch {
    Write-Host "Database check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 3: Try direct login without OTP verification
Write-Host "`n=== Step 3: Direct Login (Without OTP) ===" -ForegroundColor Cyan
$loginBody = @{
    email = $testEmail
    password = "Test@123456"
    identifier = $testEmail
}

try {
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body ($loginBody | ConvertTo-Json -Depth 10) -ContentType "application/json" -UseBasicParsing
    Write-Host "Login Status: $($loginResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Login Response: $($loginResponse.Content)" -ForegroundColor White
} catch {
    Write-Host "Login Failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

# Step 4: Check if we need to verify OTP first
Write-Host "`n=== Step 4: Check OTP from logs ===" -ForegroundColor Cyan
Write-Host "Checking recent logs for OTP..." -ForegroundColor Yellow
$otpLogs = docker logs cyberlearnix-user-service --tail 20 | Select-String -Pattern "OTP|otp"
if ($otpLogs) {
    Write-Host "OTP found in logs:" -ForegroundColor Green
    Write-Host $otpLogs
} else {
    Write-Host "No OTP found in recent logs" -ForegroundColor Red
}

# Step 5: Try to get OTP from database
Write-Host "`n=== Step 5: Check OTP in Database ===" -ForegroundColor Cyan
try {
    $otpCheck = docker exec cyberlearnix-postgres psql -U cyberlearnix -d lms_user_db -c "SELECT email, otp_code, expires_at FROM otp_codes WHERE email = '$testEmail' ORDER BY created_at DESC LIMIT 1;"
    Write-Host "OTP Record:" -ForegroundColor White
    Write-Host $otpCheck
} catch {
    Write-Host "OTP check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Yellow
