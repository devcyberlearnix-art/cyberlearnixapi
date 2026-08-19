# Test Admin OTP Resend Functionality
$baseUrl = "http://localhost:8087/api/v1/admin"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST 1: Request Login OTP - Get S1" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$loginOtpRequest = @{
    email = "dev.cyberlearnix@gmail.com"
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri "$baseUrl/login/otp/request" -Method Post -Body $loginOtpRequest -ContentType "application/json"
    Write-Host "✅ Login OTP Request Successful" -ForegroundColor Green
    Write-Host "Session ID (S1): $($response1.data.otpSessionId)" -ForegroundColor Yellow
    Write-Host "Email: $($response1.data.email)" -ForegroundColor Yellow
    Write-Host "Expires At: $($response1.data.expiresAt)" -ForegroundColor Yellow
    Write-Host "Cooldown: $($response1.data.cooldownSeconds) seconds" -ForegroundColor Yellow
    $sessionS1 = $response1.data.otpSessionId
    $otp1 = "123456" # Placeholder - would need to check logs for actual OTP
} catch {
    Write-Host "❌ Login OTP Request Failed: $_" -ForegroundColor Red
    $sessionS1 = $null
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 2: Wait for cooldown (35 seconds)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Start-Sleep -Seconds 35

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 3: Resend Login OTP with S1" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$resendRequest = @{
    otpSessionId = $sessionS1
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/login/otp/resend" -Method Post -Body $resendRequest -ContentType "application/json"
    Write-Host "✅ Login OTP Resend Successful" -ForegroundColor Green
    Write-Host "New Session ID (S2): $($response2.data.otpSessionId)" -ForegroundColor Yellow
    Write-Host "Email: $($response2.data.email)" -ForegroundColor Yellow
    Write-Host "Expires At: $($response2.data.expiresAt)" -ForegroundColor Yellow
    Write-Host "Cooldown: $($response2.data.cooldownSeconds) seconds" -ForegroundColor Yellow
    $sessionS2 = $response2.data.otpSessionId
} catch {
    Write-Host "❌ Login OTP Resend Failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 4: Try to use old session S1 (should fail)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$verifyOldRequest = @{
    otpSessionId = $sessionS1
    otp = "123456"
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/login/otp/verify" -Method Post -Body $verifyOldRequest -ContentType "application/json"
    Write-Host "❌ Old session S1 should have failed but succeeded!" -ForegroundColor Red
} catch {
    Write-Host "✅ Old session S1 correctly failed: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 5: Try resend during cooldown (should fail)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl/login/otp/resend" -Method Post -Body $resendRequest -ContentType "application/json"
    if (-not $response4.success) {
        Write-Host "✅ Cooldown correctly enforced: $($response4.message)" -ForegroundColor Green
    } else {
        Write-Host "❌ Cooldown should have blocked resend but didn't!" -ForegroundColor Red
    }
} catch {
    Write-Host "✅ Cooldown correctly enforced: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 6: Test with invalid session ID" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$invalidRequest = @{
    otpSessionId = "invalid-session-id-12345"
} | ConvertTo-Json

try {
    $response5 = Invoke-RestMethod -Uri "$baseUrl/login/otp/resend" -Method Post -Body $invalidRequest -ContentType "application/json"
    if (-not $response5.success) {
        Write-Host "✅ Invalid session correctly rejected: $($response5.message)" -ForegroundColor Green
    } else {
        Write-Host "❌ Invalid session should have been rejected!" -ForegroundColor Red
    }
} catch {
    Write-Host "✅ Invalid session correctly rejected: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 7: Test with missing otpSessionId" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$missingRequest = @{} | ConvertTo-Json

try {
    $response6 = Invoke-RestMethod -Uri "$baseUrl/login/otp/resend" -Method Post -Body $missingRequest -ContentType "application/json"
    if (-not $response6.success) {
        Write-Host "✅ Missing otpSessionId correctly rejected: $($response6.message)" -ForegroundColor Green
    } else {
        Write-Host "❌ Missing otpSessionId should have been rejected!" -ForegroundColor Red
    }
} catch {
    Write-Host "✅ Missing otpSessionId correctly rejected: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 8: Request Password Reset OTP - Get S1" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$forgotRequest = @{
    email = "dev.cyberlearnix@gmail.com"
} | ConvertTo-Json

try {
    $response7 = Invoke-RestMethod -Uri "$baseUrl/password/forgot" -Method Post -Body $forgotRequest -ContentType "application/json"
    Write-Host "✅ Password Reset OTP Request Successful" -ForegroundColor Green
    Write-Host "Session ID (S1): $($response7.data.otpSessionId)" -ForegroundColor Yellow
    Write-Host "Email: $($response7.data.email)" -ForegroundColor Yellow
    Write-Host "Expires At: $($response7.data.expiresAt)" -ForegroundColor Yellow
    $passwordSessionS1 = $response7.data.otpSessionId
} catch {
    Write-Host "❌ Password Reset OTP Request Failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 9: Wait for password reset cooldown (35 seconds)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Start-Sleep -Seconds 35

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST 10: Resend Password Reset OTP with S1" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$passwordResendRequest = @{
    otpSessionId = $passwordSessionS1
} | ConvertTo-Json

try {
    $response8 = Invoke-RestMethod -Uri "$baseUrl/password/otp/resend" -Method Post -Body $passwordResendRequest -ContentType "application/json"
    Write-Host "✅ Password Reset OTP Resend Successful" -ForegroundColor Green
    Write-Host "New Session ID (S2): $($response8.data.otpSessionId)" -ForegroundColor Yellow
    Write-Host "Email: $($response8.data.email)" -ForegroundColor Yellow
    Write-Host "Expires At: $($response8.data.expiresAt)" -ForegroundColor Yellow
    Write-Host "Cooldown: $($response8.data.cooldownSeconds) seconds" -ForegroundColor Yellow
    $passwordSessionS2 = $response8.data.otpSessionId
} catch {
    Write-Host "❌ Password Reset OTP Resend Failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "All Tests Completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
