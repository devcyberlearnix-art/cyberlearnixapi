@echo off
setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8087/api/v1/admin

echo ========================================
echo TEST: Resend Login OTP with valid session ID
echo ========================================
curl -X POST "%BASE_URL%/login/otp/resend" -H "Content-Type: application/json" -d "{\"otpSessionId\":\"16ef30ad-7a32-4da7-bddd-fc9638f990b7\"}"
echo.

echo ========================================
echo TEST: Resend Password Reset OTP with valid session ID
echo ========================================
curl -X POST "%BASE_URL%/password/otp/resend" -H "Content-Type: application/json" -d "{\"otpSessionId\":\"6249a72c-ee8b-4897-b8e9-de293dc17c06\"}"
echo.
