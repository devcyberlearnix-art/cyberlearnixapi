# User Service Auth API Test Cases

Date: 2026-08-04

## Scope

This document contains tested request and response samples for:
- Registration
- Email verification
- Password login
- Login with OTP
- Forgot password OTP
- Verify password OTP
- Reset password

Base URL:
- http://localhost:8091

Primary test email:
- dev.cyberlearnix@gmail.com

---

## 1) Register - Validation Error (Wrong Mobile Field)

Endpoint:
- POST /api/v1/auth/register

Sample request:

    {
      "firstName": "Dev",
      "lastName": "Cyber",
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Start@123",
      "confirmPassword": "Start@123",
      "countryCode": "+91",
      "mobile": "9876543210"
    }

Sample response (400):

    {
      "message": "Mobile number must be 6-12 digits",
      "success": false,
      "timestamp": "2026-08-04T01:32:53.0896883"
    }

---

## 2) Register - Success

Endpoint:
- POST /api/v1/auth/register

Sample request:

    {
      "firstName": "Dev",
      "lastName": "Cyber",
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Start@123",
      "confirmPassword": "Start@123",
      "countryCode": "+91",
      "mobileNumber": "9876543210"
    }

Sample response (201):

    {
      "success": true,
      "message": "User registered successfully. OTP has been sent to email.",
      "data": {
        "id": "f8d1e6f3-ad7b-4b2a-b4e5-b171110fec85",
        "email": "dev.cyberlearnix@gmail.com",
        "status": "PENDING_VERIFICATION",
        "role": "STUDENT",
        "countryCode": "+91"
      },
      "timestamp": "2026-08-04T01:33:08.7085014"
    }

---

## 3) Register Duplicate - Mobile Already Registered

Endpoint:
- POST /api/v1/auth/register

Sample request:

    {
      "firstName": "Dev",
      "lastName": "Cyber",
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Start@123",
      "confirmPassword": "Start@123",
      "countryCode": "+91",
      "mobileNumber": "9876543210"
    }

Sample response (400):

    {
      "message": "Mobile number already registered",
      "success": false,
      "timestamp": "2026-08-04T01:33:46.5735354"
    }

---

## 4) Register Duplicate - Email Already Registered

Endpoint:
- POST /api/v1/auth/register

Sample request:

    {
      "firstName": "Dev",
      "lastName": "Cyber",
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Start@123",
      "confirmPassword": "Start@123",
      "countryCode": "+91",
      "mobileNumber": "9123456789"
    }

Sample response (400):

    {
      "message": "Email already registered",
      "success": false,
      "timestamp": "2026-08-04T01:33:57.4323184"
    }

---

## 5) Verify Email OTP - Wrong OTP

Endpoint:
- POST /api/v1/auth/verify-email

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "111111"
    }

Sample response (400):

    {
      "data": {
        "expiresInSeconds": 275,
        "remainingAttempts": 4
      },
      "success": false,
      "message": "Invalid OTP",
      "timestamp": "2026-08-04T01:33:29.1570436"
    }

---

## 6) Verify Email OTP - Success

Endpoint:
- POST /api/v1/auth/verify-email

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "829395"
    }

Sample response (200):

    {
      "success": true,
      "message": "OTP verified successfully",
      "data": {
        "email": "dev.cyberlearnix@gmail.com",
        "status": "ACTIVE",
        "role": "STUDENT"
      },
      "timestamp": "2026-08-04T01:33:39.3076962"
    }

---

## 7) Password Login - Wrong Password

Endpoint:
- POST /api/v1/auth/login

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Wrong@123"
    }

Sample response (401):

    {
      "message": "Invalid credentials",
      "success": false,
      "timestamp": "2026-08-04T01:34:03.2721825"
    }

---

## 8) Password Login - Success

Endpoint:
- POST /api/v1/auth/login

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Start@123"
    }

Sample response (200):

    {
      "success": true,
      "message": "Login successful",
      "user": {
        "email": "dev.cyberlearnix@gmail.com",
        "role": "STUDENT",
        "verified": true
      },
      "authentication": {
        "accessToken": "<jwt>",
        "accessTokenExpiresIn": "15 minutes",
        "refreshToken": "<jwt>",
        "refreshTokenExpiresIn": "30 days"
      },
      "sessionInfo": {
        "device": "Windows Desktop"
      },
      "timestamp": "2026-08-04T01:34:03.397737"
    }

---

## 9) Login OTP Request - Success

Endpoint:
- POST /api/v1/auth/login/otp/request

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com"
    }

Sample response (200):

    {
      "otpSessionId": "45c96cfa-2662-4b4e-bd81-e5c264686dfd",
      "success": true,
      "sessionStartedAt": "2026-08-04T01:35:20.9852812",
      "message": "If the email exists, a login OTP has been sent",
      "validForMinutes": 5,
      "otpType": "login",
      "expiresAt": "2026-08-04T01:40:16.2690196",
      "cooldownSeconds": 30,
      "timestamp": "2026-08-04T01:35:20.9852812"
    }

---

## 10) Login OTP Request - Cooldown

Endpoint:
- POST /api/v1/auth/login/otp/request

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com"
    }

Sample response (429):

    {
      "success": false,
      "message": "Please wait before requesting a new OTP",
      "cooldownSeconds": 18,
      "timestamp": "2026-08-04T01:34:56.3534892"
    }

---

## 11) Login OTP Verify - Missing Session ID

Endpoint:
- POST /api/v1/auth/login/otp/verify

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "123456"
    }

Sample response (400):

    {
      "message": "OTP session ID is required",
      "success": false,
      "timestamp": "2026-08-04T01:34:28.0075224"
    }

---

## 12) Login OTP Verify - Wrong OTP

Endpoint:
- POST /api/v1/auth/login/otp/verify

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "111111",
      "otpSessionId": "45c96cfa-2662-4b4e-bd81-e5c264686dfd"
    }

Sample response (400):

    {
      "message": "Invalid OTP",
      "success": false,
      "timestamp": "2026-08-04T01:35:42.7402219"
    }

---

## 13) Login OTP Verify - Success

Endpoint:
- POST /api/v1/auth/login/otp/verify

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "438239",
      "otpSessionId": "45c96cfa-2662-4b4e-bd81-e5c264686dfd"
    }

Sample response (200):

    {
      "success": true,
      "message": "Login successful",
      "user": {
        "email": "dev.cyberlearnix@gmail.com",
        "role": "STUDENT"
      },
      "authentication": {
        "accessToken": "<jwt>",
        "refreshToken": "<jwt>"
      },
      "timestamp": "2026-08-04T01:35:42.7771613"
    }

---

## 14) Forgot Password OTP Request - Success

Endpoint:
- POST /api/v1/auth/password/forgot

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com"
    }

Sample response (200):

    {
      "otpSessionId": "2f2e8484-53c1-4985-9cd1-cfbeb023fd56",
      "success": true,
      "sessionStartedAt": "2026-08-04T01:37:31.5425989",
      "message": "If the email exists, a password reset OTP has been sent",
      "validForMinutes": 5,
      "otpType": "password_reset",
      "expiresAt": "2026-08-04T01:42:26.8548021",
      "cooldownSeconds": 30,
      "timestamp": "2026-08-04T01:37:31.5425989"
    }

---

## 15) Forgot Password OTP Request - Cooldown

Endpoint:
- POST /api/v1/auth/password/forgot

Back-to-back responses:

    {
      "success": false,
      "message": "Please wait before requesting a new OTP",
      "cooldownSeconds": 17,
      "timestamp": "2026-08-04T01:37:09.8948944"
    }

    {
      "success": false,
      "message": "Please wait before requesting a new OTP",
      "cooldownSeconds": 17,
      "timestamp": "2026-08-04T01:37:09.9049483"
    }

---

## 16) Verify Password OTP - Missing Session ID

Endpoint:
- POST /api/v1/auth/password/verify-otp

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "123456"
    }

Sample response (400):

    {
      "success": false,
      "message": "otpSessionId is required",
      "timestamp": "2026-08-04T01:37:52.0285781"
    }

---

## 17) Verify Password OTP - Wrong OTP

Endpoint:
- POST /api/v1/auth/password/verify-otp

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "111111",
      "otpSessionId": "2f2e8484-53c1-4985-9cd1-cfbeb023fd56"
    }

Sample response (400):

    {
      "success": false,
      "message": "Invalid OTP",
      "remainingAttempts": 4,
      "timestamp": "2026-08-04T01:37:52.0545113"
    }

---

## 18) Verify Password OTP - Success

Endpoint:
- POST /api/v1/auth/password/verify-otp

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otp": "377342",
      "otpSessionId": "2f2e8484-53c1-4985-9cd1-cfbeb023fd56"
    }

Sample response (200):

    {
      "sessionValidatedAt": "2026-08-04T01:37:52.0824926",
      "otpSessionId": "2f2e8484-53c1-4985-9cd1-cfbeb023fd56",
      "success": true,
      "message": "OTP verified successfully",
      "timestamp": "2026-08-04T01:37:52.0824926"
    }

---

## 19) Reset Password - Unverified/Invalid Session

Endpoint:
- POST /api/v1/auth/password/reset

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otpSessionId": "00000000-0000-0000-0000-000000000000",
      "newPassword": "Reset@1234",
      "confirmPassword": "Reset@1234"
    }

Sample response (401):

    {
      "success": false,
      "message": "OTP session not verified or expired",
      "timestamp": "2026-08-04T01:38:14.4957796"
    }

---

## 20) Reset Password - Success

Endpoint:
- POST /api/v1/auth/password/reset

Sample request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "otpSessionId": "2f2e8484-53c1-4985-9cd1-cfbeb023fd56",
      "newPassword": "Reset@1234",
      "confirmPassword": "Reset@1234"
    }

Sample response (200):

    {
      "success": true,
      "message": "Password reset successfully",
      "timestamp": "2026-08-04T01:38:14.606023"
    }

---

## 21) Post-Reset Login Validation

Endpoint:
- POST /api/v1/auth/login

Old password request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Start@123"
    }

Old password response (401):

    {
      "message": "Invalid credentials",
      "success": false,
      "timestamp": "2026-08-04T01:38:22.3823516"
    }

New password request:

    {
      "email": "dev.cyberlearnix@gmail.com",
      "password": "Reset@1234"
    }

New password response (200):

    {
      "success": true,
      "message": "Login successful",
      "user": {
        "email": "dev.cyberlearnix@gmail.com",
        "role": "STUDENT"
      },
      "authentication": {
        "accessToken": "<jwt>",
        "refreshToken": "<jwt>"
      },
      "timestamp": "2026-08-04T01:38:22.5027589"
    }

---

## Notes

- Registration verification endpoint uses /api/v1/auth/verify-email.
- Unified OTP login endpoints use /api/v1/auth/login/otp/request and /api/v1/auth/login/otp/verify.
- Password reset OTP endpoints use /api/v1/auth/password/forgot, /api/v1/auth/password/verify-otp, and /api/v1/auth/password/reset.
- OTP session validation messages have minor wording differences across endpoints:
  - OTP session ID is required
  - otpSessionId is required
