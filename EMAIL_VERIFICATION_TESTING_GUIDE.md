# Email Verification System Testing Guide

## Prerequisites

Before testing the email verification system, ensure you have:

1. **Email Configuration Setup**:

   - Gmail account with 2FA enabled
   - App password generated for Gmail
   - Environment variables set (`EMAIL_USERNAME`, `EMAIL_PASSWORD`)

2. **Application Running**:

   - Spring Boot application started
   - Redis server connected
   - PostgreSQL database connected

3. **Postman Setup**:
   - Import the updated collection: `Projo_Backend_API_Complete_With_Email_Verification.postman_collection.json`
   - Import the environment: `Projo_Backend_Environment_Email_Verification.postman_environment.json`

## Testing Workflow

### 1. Email Verification During Registration

#### Step 1: Register a New User

```http
POST /api/auth/register
{
    "username": "testuser123",
    "fullName": "Test User",
    "email": "your-email@gmail.com",
    "password": "password123",
    "confirmPassword": "password123"
}
```

**Expected Result**:

- ✅ HTTP 200 OK
- ✅ Response: `{"success": true, "message": "User registered successfully", "data": null}`
- ✅ Verification email sent to the provided email address

#### Step 2: Check Your Email

- Open your email inbox
- Look for email with subject: "Projo - Email Verification"
- Note the 6-digit OTP (e.g., 123456)

#### Step 3: Verify Email with OTP

```http
POST /api/auth/verify-email
{
    "email": "your-email@gmail.com",
    "otp": "123456"
}
```

**Expected Result**:

- ✅ HTTP 200 OK
- ✅ Response: `{"success": true, "message": "Email verified successfully", "data": null}`

#### Step 4: Login After Verification

```http
POST /api/auth/login
{
    "username": "testuser123",
    "password": "password123"
}
```

**Expected Result**:

- ✅ HTTP 200 OK
- ✅ JWT token returned
- ✅ User can now access protected endpoints

### 2. Login Before Email Verification (Error Case)

#### Test Scenario: Try to login without verifying email

```http
POST /api/auth/login
{
    "username": "unverified_user",
    "password": "password123"
}
```

**Expected Result**:

- ❌ HTTP 400 Bad Request
- ❌ Response: `{"success": false, "message": "Please verify your email address before logging in", "data": null}`

### 3. OTP Resend Functionality

#### Step 1: Request OTP Resend

```http
POST /api/auth/resend-verification-otp
{
    "email": "your-email@gmail.com"
}
```

**Expected Result**:

- ✅ HTTP 200 OK
- ✅ New OTP sent to email
- ✅ Previous OTP becomes invalid

#### Step 2: Verify with New OTP

Use the new OTP received in email to verify the account.

### 4. Password Reset with Email OTP

#### Step 1: Request Password Reset

```http
POST /api/auth/forgot-password
{
    "email": "your-email@gmail.com"
}
```

**Expected Result**:

- ✅ HTTP 200 OK
- ✅ Response: `{"success": true, "message": "Password reset OTP sent to your email", "data": null}`
- ✅ Reset email sent with subject: "Projo - Password Reset"

#### Step 2: Check Reset Email

- Look for email with reset OTP
- Note the 6-digit reset code

#### Step 3: Reset Password with OTP

```http
POST /api/auth/reset-password
{
    "email": "your-email@gmail.com",
    "otp": "654321",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
}
```

**Expected Result**:

- ✅ HTTP 200 OK
- ✅ Response: `{"success": true, "message": "Password reset successfully", "data": null}`

#### Step 4: Login with New Password

```http
POST /api/auth/login
{
    "username": "testuser123",
    "password": "newpassword123"
}
```

**Expected Result**:

- ✅ HTTP 200 OK
- ✅ Successful login with new password

## Error Testing Scenarios

### 1. Invalid OTP Tests

#### Test 1: Wrong OTP

```http
POST /api/auth/verify-email
{
    "email": "your-email@gmail.com",
    "otp": "999999"
}
```

**Expected**: `{"success": false, "message": "Invalid or expired OTP", "data": null}`

#### Test 2: Expired OTP

- Wait for 11 minutes after receiving OTP
- Try to verify with the expired OTP
  **Expected**: `{"success": false, "message": "Invalid or expired OTP", "data": null}`

### 2. Duplicate Email Tests

#### Test: Register with Existing Email

```http
POST /api/auth/register
{
    "username": "newuser",
    "email": "existing-email@gmail.com",
    "password": "password123",
    "confirmPassword": "password123"
}
```

**Expected**: `{"success": false, "message": "Email already exists", "data": null}`

### 3. Already Verified Email Tests

#### Test: Verify Already Verified Email

```http
POST /api/auth/verify-email
{
    "email": "verified-email@gmail.com",
    "otp": "123456"
}
```

**Expected**: `{"success": false, "message": "Email is already verified", "data": null}`

### 4. Non-existent User Tests

#### Test: Reset Password for Non-existent Email

```http
POST /api/auth/forgot-password
{
    "email": "nonexistent@gmail.com"
}
```

**Expected**: `{"success": false, "message": "User not found with this email", "data": null}`

## Postman Testing Instructions

### Setting Up Environment

1. **Import Collection**: Import `Projo_Backend_API_Complete_With_Email_Verification.postman_collection.json`
2. **Import Environment**: Import `Projo_Backend_Environment_Email_Verification.postman_environment.json`
3. **Update Test Email**: Change `test_email` variable to your actual email address

### Automated Testing Flow

Use this sequence for automated testing:

1. **🔐 Authentication & Email Verification** → **Register User (Sends Verification Email)**
2. Check your email for OTP
3. **🔐 Authentication & Email Verification** → **Verify Email with OTP**
4. **🔐 Authentication & Email Verification** → **Login User (Requires Email Verification)**
5. **📊 Projects API** → **Create Project** (to test that authenticated user can create projects)

### Manual Variables Update

For testing, update these environment variables:

```
test_email = your-actual-email@gmail.com
test_username = your-test-username
```

## Database Verification

### Check User Email Status

Connect to your PostgreSQL database and run:

```sql
-- Check user email verification status
SELECT
    id,
    username,
    email,
    email_verified,
    verification_otp,
    otp_expiry_time,
    reset_password_otp,
    reset_password_otp_expiry,
    created_at
FROM users
WHERE email = 'your-email@gmail.com';
```

### Expected Data Flow

1. **After Registration**:

   - `email_verified = false`
   - `verification_otp = '123456'` (6-digit code)
   - `otp_expiry_time = timestamp + 10 minutes`

2. **After Email Verification**:

   - `email_verified = true`
   - `verification_otp = null`
   - `otp_expiry_time = null`

3. **After Password Reset Request**:

   - `reset_password_otp = '654321'` (6-digit code)
   - `reset_password_otp_expiry = timestamp + 10 minutes`

4. **After Password Reset**:
   - `reset_password_otp = null`
   - `reset_password_otp_expiry = null`
   - `password = hashed_new_password`

## Performance Testing

### Cache Behavior with Email Verification

1. **Create multiple users** and verify their emails
2. **Test login performance** for verified vs unverified users
3. **Monitor cache statistics** during user authentication

### Load Testing Considerations

- **OTP Generation**: Test with multiple concurrent OTP requests
- **Email Sending**: Monitor email service performance under load
- **Database Updates**: Test concurrent email verification attempts

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. Emails Not Sending

```bash
# Check application logs
tail -f logs/spring.log | grep -i email

# Common causes:
# - Incorrect Gmail app password
# - SMTP settings wrong
# - Firewall blocking SMTP
```

#### 2. OTP Validation Failures

```sql
-- Check OTP and expiry in database
SELECT verification_otp, otp_expiry_time, NOW()
FROM users WHERE email = 'test@example.com';
```

#### 3. Login Still Failing After Verification

```sql
-- Verify email_verified status
SELECT email_verified FROM users WHERE email = 'test@example.com';
```

#### 4. Database Schema Issues

```sql
-- Check if new columns exist
DESCRIBE users;

-- If columns missing, may need to update DDL setting
-- spring.jpa.hibernate.ddl-auto=create-drop
```

## Security Testing

### OTP Security Tests

1. **Rate Limiting**: Test multiple OTP requests in short time
2. **Brute Force**: Test multiple wrong OTP attempts
3. **Time Window**: Verify OTP expires exactly after 10 minutes
4. **Code Reuse**: Ensure used OTPs cannot be reused

### Email Security Tests

1. **Email Validation**: Test with invalid email formats
2. **SQL Injection**: Test email fields with SQL injection attempts
3. **XSS**: Test email content for XSS vulnerabilities

## Success Criteria Checklist

### Registration Flow

- [ ] User can register with valid email
- [ ] Verification email is sent immediately
- [ ] OTP is 6 digits and numeric
- [ ] User cannot login before verification
- [ ] Duplicate emails are rejected

### Email Verification Flow

- [ ] Valid OTP verifies email successfully
- [ ] Invalid OTP shows appropriate error
- [ ] Expired OTP shows appropriate error
- [ ] Already verified email shows appropriate error
- [ ] OTP can be resent successfully

### Password Reset Flow

- [ ] Reset OTP is sent to registered email
- [ ] Valid OTP allows password reset
- [ ] New password works for login
- [ ] Reset OTP expires after 10 minutes
- [ ] Non-existent email shows appropriate error

### System Integration

- [ ] Cache system works with email verification
- [ ] Database stores email verification data correctly
- [ ] JWT tokens include email verification status
- [ ] All existing APIs work after email integration

This comprehensive testing guide ensures that your email verification system is thoroughly tested and working correctly.
