@echo off
setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8087/api/v1/admin

echo ========================================
echo TEST 1: Request Login OTP - Get S1
echo ========================================
curl -X POST "%BASE_URL%/login/otp/request" -H "Content-Type: application/json" -d "{\"email\":\"dev.cyberlearnix@gmail.com\"}" -o login_response.json
type login_response.json
echo.

echo ========================================
echo Wait 35 seconds for cooldown
echo ========================================
ping 127.0.0.1 -n 36 > nul

echo ========================================
echo TEST 2: Extract session ID and test resend
echo ========================================
powershell -Command "$json = Get-Content login_response.json | ConvertFrom-Json; $sessionId = $json.data.otpSessionId; Write-Host \"Session ID: $sessionId\"; Invoke-RestMethod -Uri '%BASE_URL%/login/otp/resend' -Method Post -Body (@{otpSessionId=$sessionId} | ConvertTo-Json) -ContentType 'application/json' | ConvertTo-Json"
echo.

echo ========================================
echo TEST 3: Try resend during cooldown (should fail)
echo ========================================
curl -X POST "%BASE_URL%/login/otp/resend" -H "Content-Type: application/json" -d "{\"otpSessionId\":\"e3b201aa-723f-4cba-b6e4-0f434a2c14b9\"}"
echo.

echo ========================================
echo TEST 4: Test with invalid session ID
echo ========================================
curl -X POST "%BASE_URL%/login/otp/resend" -H "Content-Type: application/json" -d "{\"otpSessionId\":\"invalid-session-id\"}"
echo.

echo ========================================
echo TEST 5: Test with missing otpSessionId
echo ========================================
curl -X POST "%BASE_URL%/login/otp/resend" -H "Content-Type: application/json" -d "{}"
echo.

echo ========================================
echo TEST 6: Request Password Reset OTP - Get S1
echo ========================================
curl -X POST "%BASE_URL%/password/forgot" -H "Content-Type: application/json" -d "{\"email\":\"dev.cyberlearnix@gmail.com\"}" -o password_response.json
type password_response.json
echo.

echo ========================================
echo Wait 35 seconds for cooldown
echo ========================================
ping 127.0.0.1 -n 36 > nul

echo ========================================
echo TEST 7: Extract session ID and test password resend
echo ========================================
powershell -Command "$json = Get-Content password_response.json | ConvertFrom-Json; $sessionId = $json.data.otpSessionId; Write-Host \"Session ID: $sessionId\"; Invoke-RestMethod -Uri '%BASE_URL%/password/otp/resend' -Method Post -Body (@{otpSessionId=$sessionId} | ConvertTo-Json) -ContentType 'application/json' | ConvertTo-Json"
echo.

echo ========================================
echo All Tests Completed!
echo ========================================
