# Authentication Flow: Login vs Register

## Overview

This document explains how to handle the authentication flow for both login and registration scenarios, including user existence checks and proper error handling.

## Authentication Flow Architecture

### 1. **Login Flow**
```
Client Request → API Gateway → User Service → Database
     ↓              ↓              ↓           ↓
User exists?    Validate        Check         Return
     ↓          credentials      password      user data
Generate JWT    ← Success ←    ← Valid ←      ← Found
```

### 2. **Registration Flow**
```
Client Request → API Gateway → User Service → Database
     ↓              ↓              ↓           ↓
User exists?    Check user      Check         Return
     ↓          existence        database      exists/not
Create user     ← Not exists ←  ← Available ←  ← Available
     ↓              ↓              ↓           ↓
Generate JWT    Create user     Save user     Return
                                               user data
```

## API Endpoints

### 1. Login Endpoint
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123",
  "deviceId": "device-uuid",
  "deviceName": "iPhone 12"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer",
  "sessionId": "session-uuid-123",
  "user": {
    "userId": "user-123",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**Error Responses:**
- **404 Not Found**: User doesn't exist
- **401 Unauthorized**: Invalid credentials
- **500 Internal Server Error**: Service unavailable

### 2. Register Endpoint
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "confirmPassword": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "deviceId": "device-uuid",
  "deviceName": "iPhone 12",
  "acceptTerms": true,
  "acceptPrivacyPolicy": true,
  "subscribeToNewsletter": false
}
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "Registration successful",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer",
  "sessionId": "session-uuid-123",
  "user": {
    "userId": "user-123",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**Error Responses:**
- **409 Conflict**: User already exists
- **400 Bad Request**: Validation errors
- **500 Internal Server Error**: Registration failed

### 3. Check User Existence
```http
GET /api/auth/check-user?username=john_doe&email=john@example.com
```

**Response (200):**
```json
{
  "exists": true,
  "username": "john_doe",
  "email": "john@example.com"
}
```

## Frontend Implementation Strategy

### 1. **Login/Register Decision Flow**

```javascript
// Step 1: Check if user exists when they enter username/email
const checkUserExists = async (username, email) => {
  try {
    const response = await fetch(`/api/auth/check-user?username=${username}&email=${email}`);
    const data = await response.json();
    return data.exists;
  } catch (error) {
    console.error('Error checking user existence:', error);
    return false;
  }
};

// Step 2: Show appropriate form based on user existence
const handleUsernameInput = async (username, email) => {
  const userExists = await checkUserExists(username, email);
  
  if (userExists) {
    // Show login form
    showLoginForm();
  } else {
    // Show registration form
    showRegisterForm();
  }
};
```

### 2. **Login Implementation**

```javascript
const login = async (credentials) => {
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    const data = await response.json();

    if (response.ok) {
      // Store tokens
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
      
      // Redirect to dashboard
      window.location.href = '/dashboard';
    } else {
      // Handle specific errors
      if (response.status === 404) {
        // User not found - show register option
        showRegisterSuggestion();
      } else if (response.status === 401) {
        // Invalid credentials
        showError('Invalid username or password');
      }
    }
  } catch (error) {
    showError('Login failed. Please try again.');
  }
};
```

### 3. **Registration Implementation**

```javascript
const register = async (registrationData) => {
  // Validate form data
  if (!validateRegistrationForm(registrationData)) {
    return;
  }

  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(registrationData),
    });

    const data = await response.json();

    if (response.ok) {
      // Store tokens (user is automatically logged in)
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
      
      // Redirect to welcome page or dashboard
      window.location.href = '/welcome';
    } else {
      // Handle specific errors
      if (response.status === 409) {
        // User already exists
        showError('User already exists. Please login instead.');
        showLoginForm();
      } else {
        // Other registration errors
        showError(data.message || 'Registration failed');
      }
    }
  } catch (error) {
    showError('Registration failed. Please try again.');
  }
};
```

## Backend User Service Requirements

### 1. **User Service Endpoints**

The User Service needs to implement these endpoints:

```java
// Check user existence
@GetMapping("/api/consumer/users/exists")
public ResponseEntity<Map<String, Boolean>> checkUserExists(
    @RequestParam String username, 
    @RequestParam String email) {
    // Implementation
}

// Login validation
@PostMapping("/api/consumer/auth/login")
public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
    // Implementation
}

// User registration
@PostMapping("/api/consumer/users/register")
public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
    // Implementation
}
```

### 2. **Database Schema**

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_email CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);
```

## Error Handling Strategy

### 1. **Common Error Scenarios**

| Scenario | HTTP Status | Response | Frontend Action |
|----------|-------------|----------|----------------|
| User not found (login) | 404 | `{"success": false, "message": "User not found. Please register first."}` | Show register form |
| Invalid credentials | 401 | `{"success": false, "message": "Invalid credentials"}` | Show error, allow retry |
| User already exists | 409 | `{"success": false, "message": "User already exists"}` | Show login form |
| Validation errors | 400 | `{"success": false, "message": "Validation error details"}` | Show field errors |
| Service unavailable | 500 | `{"success": false, "message": "Service unavailable"}` | Show retry option |

### 2. **Validation Rules**

**Registration Validation:**
- Username: 3-50 characters, alphanumeric + underscore
- Email: Valid email format
- Password: Minimum 8 characters, must contain uppercase, lowercase, number
- Confirm Password: Must match password
- Terms acceptance: Required
- Privacy policy acceptance: Required

**Login Validation:**
- Username/Email: Required, not empty
- Password: Required, not empty

## Security Considerations

### 1. **Rate Limiting**
```yaml
# application.yml
security:
  rate-limit:
    login-attempts: 5
    login-window-minutes: 15
    registration-attempts: 3
    registration-window-minutes: 60
```

### 2. **Password Security**
- Use bcrypt for password hashing
- Minimum 8 characters
- Complexity requirements
- Password history (prevent reuse)

### 3. **Account Security**
- Email verification for new accounts
- Account lockout after failed attempts
- Password reset functionality
- Two-factor authentication (optional)

## Testing Strategy

### 1. **Unit Tests**
```java
@Test
void testLoginSuccess() {
    // Test successful login
}

@Test
void testLoginUserNotFound() {
    // Test user not found scenario
}

@Test
void testRegisterSuccess() {
    // Test successful registration
}

@Test
void testRegisterUserExists() {
    // Test user already exists scenario
}
```

### 2. **Integration Tests**
```java
@Test
void testCompleteAuthenticationFlow() {
    // Test end-to-end authentication flow
}
```

This comprehensive authentication flow provides a smooth user experience while maintaining security and proper error handling.
