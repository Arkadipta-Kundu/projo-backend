# Projo Backend API Testing Guide

## 🚀 Quick Start Authentication Flow

### Prerequisites

1. Import the Postman collection: `Projo_Backend_API_Complete_With_Email_Verification.postman_collection.json`
2. Import the environment: `Projo_Backend_Environment.postman_environment.json`
3. Make sure your Spring Boot application is running on `http://localhost:8080`

### Step-by-Step Authentication Testing

#### 1. **Register a New User**

- **Endpoint**: `POST /api/auth/register`
- **Action**: Creates a new user and sends email verification OTP
- **Required**: Update `test_email` environment variable with your actual email
- **Response**: Success message confirming registration

#### 2. **Check Your Email**

- Look for verification email with 6-digit OTP
- Note: The OTP expires in 10 minutes

#### 3. **Verify Email with OTP**

- **Endpoint**: `POST /api/auth/verify-email`
- **Action**: Verifies your email with the received OTP
- **Required**: Enter the 6-digit OTP from email

#### 4. **Login to Get Authentication Token**

- **Endpoint**: `POST /api/auth/login`
- **Action**: Authenticates user and returns JWT token
- **Auto-Action**: Token is automatically saved to `auth_token` environment variable
- **Required**: Email must be verified first

#### 5. **Test Protected Endpoints**

Now you can test any protected endpoint using the automatically set `auth_token`:

- ✅ Projects API
- ✅ Tasks API
- ✅ Task Reminders API
- ✅ Dashboard API
- ✅ Caching API

#### 6. **Logout (Optional)**

- **Endpoint**: `POST /api/auth/logout`
- **Action**: Clears the auth token from environment variables

## 🔐 Authentication Headers

All protected endpoints require the `Authorization` header:

```
Authorization: Bearer {{auth_token}}
```

This is automatically handled by the Postman collection after successful login.

## 📧 Email Verification Features

### Resend Verification OTP

If you didn't receive the email or OTP expired:

- **Endpoint**: `POST /api/auth/resend-verification-otp`

### Forgot Password Flow

1. **Request Reset**: `POST /api/auth/forgot-password`
2. **Check Email**: Receive password reset OTP
3. **Reset Password**: `POST /api/auth/reset-password`

## ⏰ Task Reminder Features

### Set Custom Reminders

- **Endpoint**: `POST /api/tasks/reminders/{taskId}/custom`
- **Body**: `{"reminderTime": "2025-08-26T15:30:00"}`

### Admin Testing (Manual Triggers)

- Trigger deadline reminders: `POST /api/tasks/reminders/trigger/deadline`
- Trigger custom reminders: `POST /api/tasks/reminders/trigger/custom`
- Check overdue tasks: `POST /api/tasks/reminders/trigger/overdue`

## 🛠️ Environment Variables

| Variable        | Description              | Auto-Set                  |
| --------------- | ------------------------ | ------------------------- |
| `auth_token`    | JWT authentication token | ✅ After login            |
| `project_id`    | Created project ID       | ✅ After creating project |
| `task_id`       | Created task ID          | ✅ After creating task    |
| `test_email`    | Your email for testing   | ❌ Set manually           |
| `test_username` | Test username            | ❌ Pre-configured         |
| `test_password` | Test password            | ❌ Pre-configured         |

## 🔄 Testing Flow Example

1. **Complete Authentication Flow**:

   ```
   Register → Verify Email → Login → Get Token
   ```

2. **Create and Manage Projects**:

   ```
   Create Project → Get Projects → Update Project → Delete Project
   ```

3. **Create and Manage Tasks**:

   ```
   Create Task → Get Tasks → Set Reminder → Update Task → Complete Task
   ```

4. **Test Caching**:
   ```
   Get Cached Data → Clear Cache → Get Fresh Data
   ```

## 🚨 Common Issues & Solutions

### 1. **"Authentication required" Error**

- **Cause**: No auth token or expired token
- **Solution**: Run the login request first to get a fresh token

### 2. **"Email not verified" Error**

- **Cause**: Trying to login without email verification
- **Solution**: Check email and verify with OTP first

### 3. **"User not found" Error**

- **Cause**: Username/email doesn't exist
- **Solution**: Register the user first

### 4. **Email Not Received**

- **Cause**: Check spam folder or wrong email
- **Solution**: Use the resend OTP endpoint or check email configuration

## 📊 Expected Responses

### Successful Login Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "testuser123",
      "fullName": "Test User",
      "email": "your-email@gmail.com",
      "role": "USER",
      "emailVerified": true,
      "createdAt": "2025-08-25T10:30:00"
    }
  }
}
```

### Successful API Call Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    /* Response data */
  }
}
```

## 🎯 Pro Tips

1. **Auto Token Management**: The collection automatically saves and uses JWT tokens
2. **Environment Switching**: Create different environments for dev/staging/prod
3. **Batch Testing**: Use Postman Runner to test multiple endpoints in sequence
4. **Monitoring**: Set up Postman monitors for continuous API testing
5. **Documentation**: Use Postman's documentation feature to share API docs

Happy Testing! 🚀
