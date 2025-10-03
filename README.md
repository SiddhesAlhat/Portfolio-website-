# 🔐 Auth Service with Email Verification (Spring Boot + JWT)

A complete **authentication & authorization service** built with **Spring Boot**, **Spring Security**, **JWT**, and **Email Verification**.  
Supports **user registration, email verification (link & OTP), login, and profile retrieval** with secure JWT-based authentication.

---

## 🚀 Features
- ✅ User **registration** with email verification
- ✅ **Email verification** via clickable link or 6-digit OTP code
- ✅ Secure **login** (only for verified users)
- ✅ JWT (HS256) based authentication & authorization
- ✅ Password hashing with **BCrypt**
- ✅ Role-based access control (default: `ROLE_USER`)
- ✅ Email sending with **JavaMailSender**
- ✅ H2 (in-memory) & MySQL database support
- ✅ Token expiry management (24h for links, 10min for codes)
- ✅ Resend verification options

---

## ⚙️ Tech Stack
- **Java 17+**
- **Spring Boot 3.3.3** (Web, Security, Data JPA, Mail)
- **JWT (jjwt 0.11.5)**
- **JavaMailSender** for email
- **H2 / MySQL** database
- **Maven**

---

## 📦 Getting Started

### 1️⃣ Prerequisites
- Install **Java 17+**
- Install **Maven**
- (Optional) MySQL if not using H2
- **Gmail account** with App Password (for email sending)

### 2️⃣ Email Configuration

#### Gmail Setup:
1. Go to your Google Account → Security
2. Enable **2-Step Verification**
3. Go to **App passwords** and generate a password for "Mail"
4. Copy the generated password

Edit `src/main/resources/application.properties`:

```properties
# Email Configuration (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Verification settings
app.verification.link-expiry-hours=24
app.verification.code-expiry-minutes=10

# JWT secret (Base64-encoded, 256-bit recommended)
app.jwt.secret=QmFzZTY0LXNlY3JldC1vbmx5LWZvcitERVYvdGVzdGluZw==
app.jwt.expiration-ms=3600000

# Database (H2 default or MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/authdb
spring.datasource.username=root
spring.datasource.password=root
```

### 3️⃣ Build & Run

```bash
# Clone the repository
cd newapp

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## 📧 Email Verification Flows

### Option 1: Verification Link Flow
1. **Register** → User provides email + password
2. **Email Sent** → System sends verification link to email
3. **Click Link** → User clicks: `http://localhost:8080/api/auth/verify?token=xxx`
4. **Verified** → Email marked as verified
5. **Login** → User can now login with JWT token

### Option 2: Verification Code (OTP) Flow
1. **Register** → User provides email + password
2. **Request Code** → User requests OTP code
3. **Email Sent** → System sends 6-digit code (expires in 10 min)
4. **Submit Code** → User submits code via API
5. **Verified** → Email marked as verified
6. **Login** → User can now login with JWT token

---

## 🔌 API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login (verified users only) | No |
| GET | `/api/auth/verify?token=xxx` | Verify email via link | No |
| POST | `/api/auth/verify-code` | Verify email via OTP code | No |
| POST | `/api/auth/resend-verification-link?email=xxx` | Resend verification link | No |
| POST | `/api/auth/resend-verification-code?email=xxx` | Resend verification code | No |

### User Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/user/profile` | Get user profile | Yes (JWT) |

---

## 📝 API Examples

### 1. Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePass123"
}
```

**Response:**
```json
"User registered successfully. Please check your email to verify your account."
```

### 2. Verify Email (Link)
```bash
GET /api/auth/verify?token=550e8400-e29b-41d4-a716-446655440000
```

**Response:**
```json
"Email verified successfully. You can now login."
```

### 3. Verify Email (Code)
```bash
POST /api/auth/verify-code
Content-Type: application/json

{
  "email": "john@example.com",
  "code": "123456"
}
```

**Response:**
```json
"Email verified successfully. You can now login."
```

### 4. Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "identifier": "johndoe",
  "password": "securePass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": 1696345200000,
  "username": "johndoe",
  "roles": ["ROLE_USER"]
}
```

**Error (if not verified):**
```json
"Please verify your email before logging in"
```

### 5. Access Protected Endpoint
```bash
GET /api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
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

## 📂 Project Structure

```
newapp/
├── src/main/java/com/example/auth/
│   ├── controller/
│   │   ├── AuthController.java          # Auth endpoints (register, login, verify)
│   │   └── UserController.java          # User profile endpoint
│   ├── dto/
│   │   ├── AuthResponse.java            # JWT response
│   │   ├── LoginRequest.java            # Login request
│   │   ├── RegisterRequest.java         # Registration request
│   │   └── VerifyCodeRequest.java       # OTP verification request
│   ├── entity/
│   │   ├── User.java                    # User entity (with verified field)
│   │   └── VerificationToken.java       # Verification token entity
│   ├── repository/
│   │   ├── UserRepository.java          # User database operations
│   │   └── VerificationTokenRepository.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java # JWT filter
│   │   ├── JwtUtil.java                 # JWT utilities
│   │   └── SecurityConfig.java          # Security configuration
│   ├── service/
│   │   ├── AuthService.java             # Auth logic + verification
│   │   ├── CustomUserDetailsService.java
│   │   └── EmailService.java            # Email sending service
│   └── AuthApplication.java             # Main application
├── src/main/resources/
│   └── application.properties           # Configuration
├── API_DOCUMENTATION.md                 # Detailed API docs
├── pom.xml                              # Maven dependencies
└── README.md                            # This file
```

---

## 🔒 Security Features

- ✅ **BCrypt Password Hashing** - Passwords stored securely
- ✅ **JWT Authentication** - Stateless token-based auth
- ✅ **Email Verification** - Only verified users can login
- ✅ **Token Expiry** - Links expire in 24h, codes in 10min
- ✅ **One-time Tokens** - Verification tokens can only be used once
- ✅ **Role-based Access** - Support for multiple roles

---

## 🧪 Testing

### Using cURL

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"pass123"}'

# 2. Check email for verification link or request code
curl -X POST "http://localhost:8080/api/auth/resend-verification-code?email=test@example.com"

# 3. Verify with code
curl -X POST http://localhost:8080/api/auth/verify-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","code":"123456"}'

# 4. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"test","password":"pass123"}'

# 5. Access protected endpoint (replace TOKEN)
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📚 Additional Documentation

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for complete API reference with:
- Detailed endpoint specifications
- Request/response examples
- Error handling
- Database schema
- Security notes

---

## 🛠️ Troubleshooting

### Email not sending?
- Check Gmail App Password is correct
- Ensure 2-Step Verification is enabled
- Check spam/junk folder
- Verify `spring.mail.*` properties

### "Please verify your email" error?
- User must verify email before login
- Check verification token hasn't expired
- Resend verification link/code if needed

### JWT token issues?
- Ensure token is passed in `Authorization: Bearer <token>` header
- Check token hasn't expired (1 hour default)
- Verify JWT secret is configured

---

## 📄 License

This project is open source and available under the MIT License.
