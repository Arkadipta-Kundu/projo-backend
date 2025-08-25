# Email Verification & Password Reset System Documentation

## Overview

This document describes the email verification and password reset functionality implemented in the Projo Backend application. The system uses OTP (One-Time Password) based verification for both email verification and password reset operations.

## Features

1. **Email Verification During Registration**

   - Users must verify their email address after registration
   - OTP is sent to the provided email address
   - Users cannot login until email is verified

2. **Password Reset with Email OTP**

   - Users can request password reset via email
   - OTP is sent to the registered email address
   - Password can be reset after OTP verification

3. **OTP Management**
   - 6-digit numeric OTP
   - 10-minute expiry time (configurable)
   - Secure random generation
   - Resend functionality available

## Architecture

### Components

1. **EmailService** - Handles email sending and OTP generation
2. **AuthService** - Updated with email verification and password reset logic
3. **EmailController** - REST endpoints for email operations
4. **User Entity** - Extended with email verification fields
5. **Request DTOs** - For email verification and password reset requests

### Database Schema Changes

The `User` entity has been extended with the following fields:

```sql
-- New columns added to users table
username VARCHAR(50) NOT NULL UNIQUE,
full_name VARCHAR(100) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
email_verified BOOLEAN NOT NULL DEFAULT FALSE,
verification_otp VARCHAR(6),
otp_expiry_time TIMESTAMP,
reset_password_otp VARCHAR(6),
reset_password_otp_expiry TIMESTAMP
```

## API Endpoints

### Registration

```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "confirmPassword": "password123"
}
```

**Response:**

- Success: User registered, verification email sent (uses fullName in email)
- Error: Username/email already exists, passwords don't match

### Email Verification

```http
POST /api/auth/verify-email
Content-Type: application/json

{
    "email": "john@example.com",
    "otp": "123456"
}
```

**Response:**

- Success: Email verified successfully
- Error: Invalid/expired OTP, email already verified

### Resend Verification OTP

```http
POST /api/auth/resend-verification-otp
Content-Type: application/json

{
    "email": "john@example.com"
}
```

**Response:**

- Success: New OTP sent to email
- Error: Email already verified, user not found

### Forgot Password

```http
POST /api/auth/forgot-password
Content-Type: application/json

{
    "email": "john@example.com"
}
```

**Response:**

- Success: Password reset OTP sent to email
- Error: User not found with email

### Reset Password

```http
POST /api/auth/reset-password
Content-Type: application/json

{
    "email": "john@example.com",
    "otp": "123456",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
}
```

**Response:**

- Success: Password reset successfully
- Error: Invalid/expired OTP, passwords don't match

### Login (Updated)

```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "john_doe",
    "password": "password123"
}
```

**Response:**

- Success: Login successful with JWT token
- Error: Email not verified, invalid credentials

## Configuration

### Application Properties

Add the following properties to `application.properties`:

```properties
# Email Configuration (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${EMAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com

# OTP Configuration
app.otp.expiry.minutes=10
app.email.from=${EMAIL_USERNAME:your-email@gmail.com}
```

### Environment Variables

Set these environment variables for email configuration:

```bash
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password  # Use App Password for Gmail
```

## Gmail Setup

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password**:
   - Go to Google Account settings
   - Security → App passwords
   - Generate a new app password for "Mail"
   - Use this password as `EMAIL_PASSWORD`

## Email Templates

### Beautiful HTML Email Design

The system now uses modern, responsive HTML email templates with the following features:

- **Professional Design**: Modern gradient backgrounds and clean typography
- **Projo Logo Integration**: Your `icon.ico` logo embedded in every email
- **Mobile Responsive**: Optimized for all devices and email clients
- **Security Focused**: Clear warnings and instructions
- **Color Coded**: Different themes for verification vs password reset

### Verification Email Template

```
Subject: 🔐 Verify Your Email - Welcome to Projo!

Features:
- Purple gradient header with Projo logo
- Large, prominent 6-digit OTP code
- Step-by-step verification instructions
- Security tips and warnings
- Professional footer with branding
```

### Password Reset Email Template

```
Subject: 🔒 Password Reset Request - Projo

Features:
- Red gradient header for urgency
- Security alert section
- Large, prominent reset code
- Clear reset instructions
- Security warnings about code sharing
```

### Technical Implementation

- **Thymeleaf Templates**: HTML templates with dynamic content (`/templates/email/`)
- **Inline Attachments**: Logo embedded as `cid:logo`
- **MIME Messages**: Rich HTML email support
- **UTF-8 Encoding**: Full unicode support for emojis and special characters

## Security Features

1. **OTP Expiry**: All OTPs expire after 10 minutes
2. **Secure Generation**: Uses `SecureRandom` for OTP generation
3. **Email Validation**: Email format validation using `@Email` annotation
4. **Login Protection**: Users cannot login without email verification
5. **OTP Cleanup**: Used OTPs are cleared from database after verification

## Error Handling

Common error scenarios and responses:

- **Email Already Exists**: `"Email already exists"`
- **Invalid OTP**: `"Invalid or expired OTP"`
- **Email Already Verified**: `"Email is already verified"`
- **User Not Found**: `"User not found with this email"`
- **Email Not Verified**: `"Please verify your email address before logging in"`
- **Passwords Don't Match**: `"Passwords do not match"`

## Testing Flow

### Registration & Verification Flow

1. Register a new user with email
2. Check email for verification OTP
3. Verify email using the OTP
4. Login with credentials

### Password Reset Flow

1. Request password reset with email
2. Check email for reset OTP
3. Reset password using OTP and new password
4. Login with new password

## Dependencies

The following dependencies were added to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## Troubleshooting

### Email Not Sending

1. Check Gmail app password is correct
2. Verify SMTP settings in application.properties
3. Ensure 2FA is enabled on Gmail account
4. Check application logs for email service errors

### OTP Issues

1. Verify OTP hasn't expired (10 minutes)
2. Check email for correct OTP
3. Ensure case-sensitive input
4. Try resending OTP if needed

### Database Issues

1. Ensure email column is added to users table
2. Check unique constraint on email field
3. Verify boolean fields are handled correctly

## Future Enhancements

1. **HTML Email Templates**: Rich HTML email templates
2. **Rate Limiting**: Limit OTP requests per email/IP
3. **Email Templates Configuration**: Configurable email content
4. **Multiple Email Providers**: Support for other email services
5. **Email Verification Reminders**: Periodic reminders for unverified users
6. **Audit Logging**: Detailed logs for email verification attempts
