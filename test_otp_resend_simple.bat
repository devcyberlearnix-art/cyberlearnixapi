@echo off
setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8087/api/v1/admin

echo ========================================
echo TEST 1: Request Login OTP - Get S1
echo ========================================
curl -X POST "%BASE_URL%/login/otp/request" -H "Content-Type: application/json" -d "{\"email\":\"dev.cyberlearnix@gmail.com\"}"
echo.

echo ========================================
echo TEST 2: Wait for cooldown (35 seconds)
echo ========================================
timeout /t 35 /nobreak

echo ========================================
echo TEST 3: Resend Login OTP with S1
echo ========================================
curl -X POST "%BASE_URL%/login/otp/resend" -H "Content-Type: application/json" -d "{\"otpSessionId\":\"test-session-id\"}"
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
curl -X POST "%BASE_URL%/password/forgot" -H "Content-Type: application/json" -d "{\"email\":\"dev.cyberlearnix@gmail.com\"}"
echo.

echo ========================================
echo TEST 7: Wait for password reset cooldown (35 seconds)
echo ========================================
timeout /t 35 /nobreak

echo ========================================
echo TEST 8: Resend Password Reset OTP with S1
echo ========================================
curl -X POST "%BASE_URL%/password/otp/resend" -H "Content-Type: application/json" -d "{\"otpSessionId\":\"test-session-id\"}"
echo.

echo ========================================
echo All Tests Completed!
echo ========================================
