# Projo Backend API - Comprehensive Postman Testing Guide

## Table of Contents

1. [Setup & Configuration](#setup--configuration)
2. [Authentication Endpoints](#authentication-endpoints)
3. [Project Management Endpoints](#project-management-endpoints)
4. [Task Management Endpoints](#task-management-endpoints)
5. [Note Management Endpoints](#note-management-endpoints)
6. [Issue Management Endpoints](#issue-management-endpoints)
7. [Dashboard Endpoints](#dashboard-endpoints)
8. [Cache Management Endpoints](#cache-management-endpoints)
9. [Testing Redis Caching](#testing-redis-caching)
10. [Error Handling](#error-handling)
11. [Environment Variables](#environment-variables)

## Setup & Configuration

### Base URL

```
http://localhost:8080
```

### Required Headers

For all authenticated endpoints, include:

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

### Environment Variables Setup

Create a Postman environment with these variables:

- `base_url`: `http://localhost:8080`
- `jwt_token`: (will be set automatically after login)
- `user_id`: (will be set automatically after login)
- `project_id`: (will be set manually for testing)
- `task_id`: (will be set manually for testing)

## Authentication Endpoints

### 1. User Registration

**POST** `{{base_url}}/api/auth/register`

**Headers:**

```
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": "User registered successfully"
}
```

**Test Script:**

```javascript
pm.test("Registration successful", function () {
  pm.response.to.have.status(200);
  const response = pm.response.json();
  pm.expect(response.success).to.be.true;
});
```

### 2. User Login

**POST** `{{base_url}}/api/auth/login`

**Headers:**

```
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "username": "testuser",
  "password": "password123",
  "rememberMe": false
}
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "username": "testuser",
    "role": "USER",
    "createdAt": "2025-08-25T12:30:00"
  }
}
```

**Test Script:**

```javascript
pm.test("Login successful", function () {
  pm.response.to.have.status(200);
  const response = pm.response.json();
  pm.expect(response.success).to.be.true;
  pm.expect(response.token).to.not.be.empty;

  // Set token for subsequent requests
  pm.environment.set("jwt_token", response.token);
  pm.environment.set("user_id", response.user.id);
});
```

### 3. Get Current User Profile

**GET** `{{base_url}}/api/auth/profile`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

**Expected Response (200 OK):**

```json
{
  "id": 1,
  "username": "testuser",
  "role": "USER",
  "createdAt": "2025-08-25T12:30:00"
}
```

## Project Management Endpoints

### 1. Create Project

**POST** `{{base_url}}/api/projects`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "title": "Sample Project",
  "description": "This is a test project for demonstration",
  "deadline": "2025-12-31"
}
```

**Expected Response (200 OK):**

```json
{
  "id": 1,
  "title": "Sample Project",
  "description": "This is a test project for demonstration",
  "deadline": "2025-12-31",
  "createdAt": "2025-08-25T12:30:00",
  "user": {
    "id": 1,
    "username": "testuser"
  }
}
```

**Test Script:**

```javascript
pm.test("Project created successfully", function () {
  pm.response.to.have.status(200);
  const response = pm.response.json();
  pm.expect(response.id).to.not.be.undefined;
  pm.environment.set("project_id", response.id);
});
```

### 2. Get All Projects (Paginated)

**GET** `{{base_url}}/api/projects?page=0&size=10&sortBy=createdAt&sortDir=desc`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

**Expected Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "title": "Sample Project",
      "description": "This is a test project for demonstration",
      "deadline": "2025-12-31",
      "createdAt": "2025-08-25T12:30:00",
      "taskCount": 0,
      "completedTaskCount": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### 3. Get Project by ID

**GET** `{{base_url}}/api/projects/{{project_id}}`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

### 4. Update Project

**PUT** `{{base_url}}/api/projects/{{project_id}}`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "title": "Updated Project Title",
  "description": "Updated project description",
  "deadline": "2025-12-31"
}
```

### 5. Delete Project

**DELETE** `{{base_url}}/api/projects/{{project_id}}`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

## Task Management Endpoints

### 1. Create Task

**POST** `{{base_url}}/api/tasks`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "title": "Sample Task",
  "description": "This is a test task",
  "startDate": "2025-08-25",
  "dueDate": "2025-08-30",
  "priority": "HIGH",
  "status": "TODO",
  "projectId": {{project_id}}
}
```

**Test Script:**

```javascript
pm.test("Task created successfully", function () {
  pm.response.to.have.status(200);
  const response = pm.response.json();
  pm.expect(response.id).to.not.be.undefined;
  pm.environment.set("task_id", response.id);
});
```

### 2. Get All Tasks (with filters)

**GET** `{{base_url}}/api/tasks?page=0&size=10&sortBy=createdAt&sortDir=desc&projectId={{project_id}}&status=TODO&priority=HIGH`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

### 3. Get Tasks for Kanban Board

**GET** `{{base_url}}/api/tasks/kanban?projectId={{project_id}}`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

**Expected Response (200 OK):**

```json
{
  "TODO": [
    {
      "id": 1,
      "title": "Sample Task",
      "description": "This is a test task",
      "priority": "HIGH",
      "dueDate": "2025-08-30"
    }
  ],
  "IN_PROGRESS": [],
  "DONE": []
}
```

### 4. Get Tasks for Gantt Chart

**GET** `{{base_url}}/api/tasks/gantt?projectId={{project_id}}`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

### 5. Get Tasks for Calendar

**GET** `{{base_url}}/api/tasks/calendar?start=2025-08-01&end=2025-08-31`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

### 6. Update Task Status

**PATCH** `{{base_url}}/api/tasks/{{task_id}}/status`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "status": "IN_PROGRESS"
}
```

### 7. Task Timer Endpoints

#### Start Timer

**POST** `{{base_url}}/api/tasks/{{task_id}}/timer/start`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

#### Stop Timer

**POST** `{{base_url}}/api/tasks/{{task_id}}/timer/stop`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

#### Get Timer Status

**GET** `{{base_url}}/api/tasks/{{task_id}}/timer/status`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

#### Get Timer History

**GET** `{{base_url}}/api/tasks/{{task_id}}/timer/history`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

#### Reset Timer

**DELETE** `{{base_url}}/api/tasks/{{task_id}}/timer`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

## Note Management Endpoints

### 1. Create Note

**POST** `{{base_url}}/api/notes`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "content": "This is a sample note for the project",
  "projectId": {{project_id}}
}
```

### 2. Get All Notes

**GET** `{{base_url}}/api/notes?page=0&size=10&sortBy=createdAt&sortDir=desc&projectId={{project_id}}&search=sample`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

## Issue Management Endpoints

### 1. Create Issue

**POST** `{{base_url}}/api/issues`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "title": "Sample Issue",
  "description": "This is a test issue",
  "severity": "HIGH",
  "status": "OPEN",
  "projectId": {{project_id}}
}
```

### 2. Get All Issues

**GET** `{{base_url}}/api/issues?page=0&size=10&sortBy=createdAt&sortDir=desc&projectId={{project_id}}&status=OPEN&severity=HIGH`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

### 3. Convert Issue to Task

**POST** `{{base_url}}/api/issues/{{issue_id}}/convert-to-task`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "startDate": "2025-08-25",
  "dueDate": "2025-08-30",
  "priority": "HIGH"
}
```

## Dashboard Endpoints

### 1. Get Dashboard Stats

**GET** `{{base_url}}/api/dashboard/stats`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

**Expected Response (200 OK):**

```json
{
  "totalProjects": 5,
  "totalTasks": 15,
  "completedTasks": 8,
  "pendingTasks": 7,
  "totalIssues": 3,
  "openIssues": 2,
  "resolvedIssues": 1,
  "totalNotes": 10,
  "upcomingDeadlines": 3,
  "overdueTasks": 1
}
```

### 2. Get Upcoming Tasks

**GET** `{{base_url}}/api/dashboard/upcoming-tasks?days=7`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

### 3. Get Recent Activity

**GET** `{{base_url}}/api/dashboard/recent-activity?limit=10`

**Headers:**

```
Authorization: Bearer {{jwt_token}}
```

## Cache Management Endpoints

### 1. Check Redis Health

**GET** `{{base_url}}/api/cache/health`

**Expected Response (200 OK):**

```json
{
  "status": "UP",
  "redis": "Connected",
  "ping": "PONG",
  "cacheManager": "Available",
  "cacheNames": ["projects", "tasks_kanban", "tasks_gantt", "dashboard_stats"]
}
```

### 2. Get Cache Statistics

**GET** `{{base_url}}/api/cache/stats`

**Expected Response (200 OK):**

```json
{
  "projects": {
    "size": 5,
    "hitCount": 25,
    "missCount": 5,
    "hitRatio": 0.83
  },
  "tasks_kanban": {
    "size": 3,
    "hitCount": 15,
    "missCount": 3,
    "hitRatio": 0.83
  }
}
```

### 3. Clear All Caches

**DELETE** `{{base_url}}/api/cache/clear`

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "All caches cleared successfully",
  "clearedCaches": [
    "projects",
    "tasks_kanban",
    "tasks_gantt",
    "dashboard_stats"
  ]
}
```

### 4. Clear Specific Cache

**DELETE** `{{base_url}}/api/cache/clear/projects`

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Cache 'projects' cleared successfully"
}
```

## Testing Redis Caching

### Test Cache Hit/Miss Scenarios

#### 1. Test Project Caching

1. **First Request (Cache Miss):**

   - `GET {{base_url}}/api/projects/{{project_id}}`
   - Check response time (should be slower)
   - Verify cache stats show miss

2. **Second Request (Cache Hit):**

   - `GET {{base_url}}/api/projects/{{project_id}}`
   - Check response time (should be faster)
   - Verify cache stats show hit

3. **Update Project (Cache Eviction):**

   - `PUT {{base_url}}/api/projects/{{project_id}}`
   - Verify cache is evicted

4. **Next Request (Cache Miss Again):**
   - `GET {{base_url}}/api/projects/{{project_id}}`
   - Check response time (should be slower again)

#### 2. Test Dashboard Caching

```javascript
// Pre-request script to measure response time
pm.globals.set("startTime", Date.now());

// Test script to check cache performance
pm.test("Response time reasonable for cached data", function () {
  const endTime = Date.now();
  const startTime = pm.globals.get("startTime");
  const responseTime = endTime - startTime;

  console.log("Response time: " + responseTime + "ms");
  pm.expect(pm.response.responseTime).to.be.below(500);
});
```

### Cache Invalidation Testing

#### Create Test Sequence:

1. Get dashboard stats (cache miss)
2. Create new project
3. Get dashboard stats again (should be cache miss due to eviction)
4. Get dashboard stats third time (cache hit)

## Error Handling

### Common Error Responses

#### 400 Bad Request

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "deadline": "Deadline must be in the future"
  }
}
```

#### 401 Unauthorized

```json
{
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

#### 403 Forbidden

```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

#### 404 Not Found

```json
{
  "success": false,
  "message": "Project not found"
}
```

#### 500 Internal Server Error

```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Environment Variables

### Development Environment

```json
{
  "name": "Development",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8080"
    },
    {
      "key": "jwt_token",
      "value": ""
    },
    {
      "key": "user_id",
      "value": ""
    },
    {
      "key": "project_id",
      "value": "1"
    },
    {
      "key": "task_id",
      "value": "1"
    }
  ]
}
```

### Production Environment

```json
{
  "name": "Production",
  "values": [
    {
      "key": "base_url",
      "value": "https://your-production-url.com"
    },
    {
      "key": "jwt_token",
      "value": ""
    }
  ]
}
```

## Test Collection Runner Scripts

### Global Pre-request Script

```javascript
// Auto-refresh token if expired
const token = pm.environment.get("jwt_token");
if (token) {
  // Check if token is expired (implement your logic)
  // If expired, make login request to refresh
}

// Add timestamp to requests
pm.globals.set("requestTime", new Date().toISOString());
```

### Global Test Script

```javascript
// Common tests for all endpoints
pm.test("Response status is valid", function () {
  pm.expect(pm.response.code).to.be.oneOf([
    200, 201, 204, 400, 401, 403, 404, 500,
  ]);
});

pm.test("Response time is acceptable", function () {
  pm.expect(pm.response.responseTime).to.be.below(5000);
});

pm.test("Response has correct content type", function () {
  if (pm.response.code === 200) {
    pm.expect(pm.response.headers.get("Content-Type")).to.include(
      "application/json"
    );
  }
});
```

## Performance Testing Scripts

### Cache Performance Test

```javascript
pm.test("Cache improves performance", function () {
  const responseTime = pm.response.responseTime;
  const previousTime = pm.globals.get("previousResponseTime");

  if (previousTime) {
    console.log(`Previous: ${previousTime}ms, Current: ${responseTime}ms`);
    // Second request should be faster (cached)
    if (pm.globals.get("isCacheTest")) {
      pm.expect(responseTime).to.be.below(previousTime * 0.8);
    }
  }

  pm.globals.set("previousResponseTime", responseTime);
});
```

## Complete Test Workflow

### 1. Authentication Flow

1. Register new user
2. Login with credentials
3. Get user profile
4. Test protected endpoints

### 2. CRUD Operations Flow

1. Create project
2. Read project
3. Update project
4. Create tasks for project
5. Read tasks
6. Update task status
7. Delete task
8. Delete project

### 3. Cache Testing Flow

1. Check cache health
2. Clear all caches
3. Make requests to trigger cache misses
4. Make same requests to test cache hits
5. Update data to test cache eviction
6. Verify cache statistics

This comprehensive guide provides everything needed to thoroughly test the Projo Backend API with Postman, including caching behavior and performance validation.
