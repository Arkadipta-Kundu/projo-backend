# API Testing Guide - Projo Backend

This guide will help you test the complete Projo Backend API using Postman with proper authentication flow.

## Prerequisites

1. **Import the Postman Collection**: `Projo_Backend_API_Collection.postman_collection.json`
2. **Import the Environment**: `Projo_Backend_Environment.postman_environment.json`
3. **Ensure the Spring Boot application is running** on `http://localhost:8080`

## Quick Start - Authentication Flow

### Step 1: Register a New User

- **Endpoint**: `POST {{base_url}}/api/auth/register`
- **Request Body**:

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "fullName": "Test User"
}
```

### Step 2: Login to Get JWT Token

- **Endpoint**: `POST {{base_url}}/api/auth/login`
- **Request Body**:

```json
{
  "username": "testuser",
  "password": "password123",
  "rememberMe": false
}
```

- **Auto-Token Capture**: The collection automatically captures the JWT token from the response and sets it in the `auth_token` environment variable
- **Token Usage**: All subsequent requests will automatically use this token in the Authorization header

### Step 3: Test Protected Endpoints

Now you can test any protected endpoint! The token will be automatically included.

## Environment Variables

The environment file includes these pre-configured variables:

- `base_url`: http://localhost:8080
- `auth_token`: (automatically set after login)
- `user_id`: (automatically set after login)
- `project_id`: 1 (default for testing)
- `task_id`: 1 (default for testing)
- `note_id`: 1 (default for testing)
- `issue_id`: 1 (default for testing)

## Testing Workflow

### 1. Authentication

- Register → Login → Test other endpoints
- Use Logout to clear the token when done

### 2. Project Management

- Create Project → Get Projects → Update Project → Delete Project

### 3. Task Management

- Create Task → Get Tasks → Update Task Status → Delete Task
- Test Kanban/Gantt/Calendar views

### 4. Task Reminders (NEW!)

- Set Custom Reminder → Query Tasks with Reminders → Disable Reminder
- Use admin triggers to test email sending

### 5. Time Tracking

- Start Timer → Check Status → Stop Timer → View History

### 6. Notes & Issues

- Create Note → Update Note → Delete Note
- Create Issue → Convert to Task → Delete Issue

## Task Reminder System Testing

The collection includes a dedicated "Task Reminders" section with these endpoints:

### Set Custom Reminder

```http
POST {{base_url}}/api/task-reminders/{{task_id}}/set-reminder
Authorization: Bearer {{auth_token}}
Content-Type: application/json

{
  "reminderTime": "2025-01-15T10:30:00"
}
```

### Query Tasks with Reminders

```http
GET {{base_url}}/api/task-reminders/tasks-with-reminders?reminderTime=2025-01-15T10:30:00
Authorization: Bearer {{auth_token}}
```

### Disable Reminder

```http
DELETE {{base_url}}/api/task-reminders/{{task_id}}/disable-reminder
Authorization: Bearer {{auth_token}}
```

### Admin Testing Triggers

```http
POST {{base_url}}/api/task-reminders/admin/trigger-custom-reminders
POST {{base_url}}/api/task-reminders/admin/trigger-deadline-reminders
POST {{base_url}}/api/task-reminders/admin/trigger-overdue-check
```

## Authentication Details

### Token Management

- **Login**: Automatically captures JWT token and sets `auth_token` variable
- **Logout**: Automatically clears the `auth_token` variable
- **Token Format**: `Bearer <jwt_token>`
- **Token Expiration**: 24 hours (86400000 ms)

### Headers

All protected endpoints automatically include:

```
Authorization: Bearer {{auth_token}}
Content-Type: application/json
```

## Error Handling

Common error responses:

### 401 Unauthorized

```json
{
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

### 403 Forbidden

```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

### 400 Bad Request

```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "details": ["Field 'title' is required"]
}
```

## Tips for Testing

1. **Always start with Register → Login** to get a valid token
2. **Check the Console** in Postman to see the auto-token capture scripts working
3. **Use the Environment Variables** instead of hardcoding IDs
4. **Test the Logout** endpoint to see token clearing in action
5. **Create sample data** (projects, tasks) before testing complex features
6. **Use the Task Reminder endpoints** to test the cron job email system

## Database Data Persistence

The application now uses `hibernate.ddl-auto=update` which means:

- ✅ **Data persists** between application restarts
- ✅ **Database tables are preserved**
- ✅ **Your test data remains available**

## Cron Job Testing

The task reminder system runs these cron jobs:

- **Custom Reminders**: Every 5 minutes
- **Deadline Reminders**: Daily at 9:00 AM
- **Overdue Check**: Daily at 10:00 AM

You can manually trigger these using the admin endpoints for immediate testing.

## Support

If you encounter any issues:

1. Check that the Spring Boot application is running
2. Verify the environment variables are set correctly
3. Ensure you're logged in (have a valid `auth_token`)
4. Check the Postman console for any script errors
5. Review the application logs for any server-side errors

Happy Testing! 🚀
