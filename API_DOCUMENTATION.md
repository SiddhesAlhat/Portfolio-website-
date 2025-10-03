# Email Verification API Documentation

## Overview
This authentication service supports email verification with two methods:
1. **Verification Link** - User clicks a link sent to their email
2. **Verification Code (OTP)** - User enters a 6-digit code sent to their email

---

## API Endpoints

### 1. User Registration
**POST** `/api/auth/register`

Registers a new user and automatically sends a verification link to their email.

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
"User registered successfully. Please check your email to verify your account."
```

**Response (400 Bad Request):**
```json
"Username is already taken"
```
or
```json
"Email is already in use"
```

---

### 2. Email Verification via Link
**GET** `/api/auth/verify?token={token}`

Verifies user email using the token from the verification link.

**Query Parameters:**
- `token` (string, required) - The verification token from the email link

**Example:**
```
GET http://localhost:8080/api/auth/verify?token=550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
"Email verified successfully. You can now login."
```

**Response (400 Bad Request):**
```json
"Invalid verification token"
```
or
```json
"Verification token has expired"
```
or
```json
"Verification token already used"
```

---

### 3. Email Verification via Code (OTP)
**POST** `/api/auth/verify-code`

Verifies user email using a 6-digit verification code.

**Request Body:**
```json
{
  "email": "john@example.com",
  "code": "123456"
}
```

**Response (200 OK):**
```json
"Email verified successfully. You can now login."
```

**Response (400 Bad Request):**
```json
"Invalid verification code"
```
or
```json
"Verification code has expired"
```

---

### 4. Resend Verification Link
**POST** `/api/auth/resend-verification-link?email={email}`

Sends a new verification link to the user's email.

**Query Parameters:**
- `email` (string, required) - User's email address

**Example:**
```
POST http://localhost:8080/api/auth/resend-verification-link?email=john@example.com
```

**Response (200 OK):**
```json
"Verification link sent to your email"
```

---

### 5. Resend Verification Code
**POST** `/api/auth/resend-verification-code?email={email}`

Sends a new 6-digit verification code to the user's email.

**Query Parameters:**
- `email` (string, required) - User's email address

**Example:**
```
POST http://localhost:8080/api/auth/resend-verification-code?email=john@example.com
```

**Response (200 OK):**
```json
"Verification code sent to your email"
```

---

### 6. User Login
**POST** `/api/auth/login`

Authenticates a user and returns a JWT token. **User must be verified to login.**

**Request Body:**
```json
{
  "identifier": "johndoe",
  "password": "securePassword123"
}
```
*Note: `identifier` can be either username or email*

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": 1696345200000,
  "username": "johndoe",
  "roles": ["ROLE_USER"]
}
```

**Response (400 Bad Request):**
```json
"Please verify your email before logging in"
```

**Response (401 Unauthorized):**
```json
"Invalid credentials"
```

---

### 7. Get User Profile (Protected)
**GET** `/api/user/profile`

Returns the authenticated user's profile information.

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"],
  "enabled": true,
  "createdAt": "2024-10-03T10:30:00Z"
}
```

---

## Email Configuration

### Gmail Setup (application.properties)
```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Verification settings
app.verification.link-expiry-hours=24
app.verification.code-expiry-minutes=10
```

### Getting Gmail App Password
1. Go to Google Account settings
2. Enable 2-Step Verification
3. Go to Security → App passwords
4. Generate a new app password for "Mail"
5. Use this password in `spring.mail.password`

---

## Verification Flow Examples

### Flow 1: Verification Link
1. User registers → `POST /api/auth/register`
2. System sends email with link: `http://localhost:8080/api/auth/verify?token=xxx`
3. User clicks link → Email verified
4. User logs in → `POST /api/auth/login`

### Flow 2: Verification Code (OTP)
1. User registers → `POST /api/auth/register`
2. User requests OTP → `POST /api/auth/resend-verification-code?email=user@example.com`
3. System sends 6-digit code via email
4. User submits code → `POST /api/auth/verify-code` with `{"email": "user@example.com", "code": "123456"}`
5. Email verified
6. User logs in → `POST /api/auth/login`

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Verification Tokens Table
```sql
CREATE TABLE verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    verification_code VARCHAR(6),
    expiry_date TIMESTAMP NOT NULL,
    type VARCHAR(10) NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Security Notes

1. **Password Hashing**: All passwords are hashed using BCrypt
2. **JWT Tokens**: Tokens expire in 1 hour (configurable)
3. **Verification Links**: Expire in 24 hours (configurable)
4. **Verification Codes**: Expire in 10 minutes (configurable)
5. **One-time Use**: Verification tokens can only be used once
6. **Email Validation**: Email format is validated on registration

---

## Testing with cURL

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Verify Email (Link)
```bash
curl -X GET "http://localhost:8080/api/auth/verify?token=YOUR_TOKEN_HERE"
```

### Verify Email (Code)
```bash
curl -X POST http://localhost:8080/api/auth/verify-code \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "123456"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser",
    "password": "password123"
  }'
```

### Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Error Handling

All endpoints return appropriate HTTP status codes:
- **200 OK** - Success
- **400 Bad Request** - Validation error or business logic error
- **401 Unauthorized** - Authentication failed
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

Error responses include descriptive messages to help debug issues.
