# Projo to Spring Boot API Documentation

## Project Overview

Projo is a self-hosted project management system for solo developers and small teams. This document outlines the APIs needed to convert the PHP application to Spring Boot.

## Table of Contents

1. [Database Schema](#database-schema)
2. [Authentication APIs](#authentication-apis)
3. [User Management APIs](#user-management-apis)
4. [Project Management APIs](#project-management-apis)
5. [Task Management APIs](#task-management-apis)
6. [Notes Management APIs](#notes-management-apis)
7. [Issues Management APIs](#issues-management-apis)
8. [Timer Management APIs](#timer-management-apis)
9. [Dashboard APIs](#dashboard-apis)
10. [Import/Export APIs](#importexport-apis)
11. [Settings APIs](#settings-apis)
12. [Activity Logging APIs](#activity-logging-apis)

---

## Database Schema

### Entities Required:

```java
// User Entity
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    private String rememberToken;
    private String role; // admin, user
    private LocalDateTime createdAt;
}

// Project Entity
@Entity
public class Project {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private LocalDate deadline;
    @ManyToOne
    private User user;
    private LocalDateTime createdAt;
}

// Task Entity
@Entity
public class Task {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    private Priority priority; // LOW, MEDIUM, HIGH
    @Enumerated(EnumType.STRING)
    private TaskStatus status; // TO_DO, IN_PROGRESS, DONE
    @ManyToOne
    private Project project;
    @ManyToOne
    private User user;
    private LocalDateTime createdAt;
}

// Note Entity
@Entity
public class Note {
    @Id @GeneratedValue
    private Long id;
    @Lob
    private String content;
    @ManyToOne
    private Project project;
    @ManyToOne
    private User user;
    private LocalDateTime createdAt;
}

// Issue Entity
@Entity
public class Issue {
    @Id @GeneratedValue
    private Long id;
    private String title;
    @Lob
    private String description;
    @Enumerated(EnumType.STRING)
    private Severity severity; // LOW, MEDIUM, HIGH
    @Enumerated(EnumType.STRING)
    private IssueStatus status; // OPEN, RESOLVED
    @ManyToOne
    private Project project;
    @ManyToOne
    private User user;
    private LocalDateTime createdAt;
}

// TaskTimeTracking Entity
@Entity
public class TaskTimeTracking {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Task task;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration; // in seconds
}

// ActivityLog Entity
@Entity
public class ActivityLog {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String action;
    private LocalDateTime timestamp;
}
```

---

## Authentication APIs

### 1. User Login

- **Endpoint**: `POST /api/auth/login`
- **Request Body**:

```json
{
  "username": "string",
  "password": "string",
  "rememberMe": "boolean"
}
```

- **Response**:

```json
{
  "success": true,
  "token": "jwt_token",
  "user": {
    "id": 1,
    "username": "user123",
    "role": "user"
  }
}
```

### 2. User Registration

- **Endpoint**: `POST /api/auth/register`
- **Request Body**:

```json
{
  "username": "string",
  "password": "string",
  "confirmPassword": "string"
}
```

- **Response**:

```json
{
  "success": true,
  "message": "User registered successfully"
}
```

### 3. User Logout

- **Endpoint**: `POST /api/auth/logout`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

### 4. Token Validation

- **Endpoint**: `GET /api/auth/validate`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "valid": true,
  "user": {
    "id": 1,
    "username": "user123",
    "role": "user"
  }
}
```

---

## User Management APIs

### 1. Get Current User Profile

- **Endpoint**: `GET /api/users/profile`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "id": 1,
  "username": "user123",
  "role": "user",
  "createdAt": "2025-01-01T00:00:00"
}
```

### 2. Update Password

- **Endpoint**: `PUT /api/users/password`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "currentPassword": "string",
  "newPassword": "string",
  "confirmPassword": "string"
}
```

- **Response**:

```json
{
  "success": true,
  "message": "Password updated successfully"
}
```

---

## Project Management APIs

### 1. Get All Projects

- **Endpoint**: `GET /api/projects`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**: `?page=0&size=10&sort=deadline,asc`
- **Response**:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Project Alpha",
      "description": "Description here",
      "deadline": "2025-12-31",
      "createdAt": "2025-01-01T00:00:00",
      "taskCount": 5,
      "completedTaskCount": 2
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### 2. Get Project by ID

- **Endpoint**: `GET /api/projects/{id}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "id": 1,
  "title": "Project Alpha",
  "description": "Description here",
  "deadline": "2025-12-31",
  "createdAt": "2025-01-01T00:00:00",
  "tasks": [
    {
      "id": 1,
      "title": "Task 1",
      "status": "TO_DO",
      "priority": "HIGH",
      "dueDate": "2025-02-15"
    }
  ]
}
```

### 3. Create Project

- **Endpoint**: `POST /api/projects`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "deadline": "2025-12-31"
}
```

- **Response**:

```json
{
  "id": 1,
  "title": "Project Alpha",
  "description": "Description here",
  "deadline": "2025-12-31",
  "createdAt": "2025-01-01T00:00:00"
}
```

### 4. Update Project

- **Endpoint**: `PUT /api/projects/{id}`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "deadline": "2025-12-31"
}
```

### 5. Delete Project

- **Endpoint**: `DELETE /api/projects/{id}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "success": true,
  "message": "Project deleted successfully"
}
```

---

## Task Management APIs

### 1. Get All Tasks

- **Endpoint**: `GET /api/tasks`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `?projectId=1` (optional)
  - `?status=TO_DO` (optional)
  - `?priority=HIGH` (optional)
  - `?page=0&size=10&sort=dueDate,asc`
- **Response**:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Task 1",
      "description": "Task description",
      "startDate": "2025-01-01",
      "dueDate": "2025-02-15",
      "priority": "HIGH",
      "status": "TO_DO",
      "projectId": 1,
      "projectTitle": "Project Alpha",
      "createdAt": "2025-01-01T00:00:00",
      "isTimerRunning": false,
      "totalTimeSpent": 3600
    }
  ],
  "totalElements": 20,
  "totalPages": 2,
  "number": 0,
  "size": 10
}
```

### 2. Get Tasks for Kanban Board

- **Endpoint**: `GET /api/tasks/kanban`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**: `?projectId=1` (optional)
- **Response**:

```json
{
  "TODO": [
    {
      "id": 1,
      "title": "Task 1",
      "priority": "HIGH",
      "dueDate": "2025-02-15",
      "projectTitle": "Project Alpha"
    }
  ],
  "IN_PROGRESS": [],
  "DONE": []
}
```

### 3. Get Tasks for Gantt Chart

- **Endpoint**: `GET /api/tasks/gantt`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**: `?projectId=1` (optional)
- **Response**:

```json
[
  {
    "id": 1,
    "title": "Task 1",
    "startDate": "2025-01-01",
    "endDate": "2025-02-15",
    "progress": 0.3,
    "projectId": 1,
    "projectTitle": "Project Alpha",
    "priority": "HIGH"
  }
]
```

### 4. Get Tasks for Calendar

- **Endpoint**: `GET /api/tasks/calendar`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**: `?start=2025-01-01&end=2025-12-31`
- **Response**:

```json
[
  {
    "id": 1,
    "title": "Task 1",
    "start": "2025-02-15",
    "backgroundColor": "#ef4444",
    "borderColor": "#dc2626",
    "url": "/tasks/1",
    "extendedProps": {
      "priority": "HIGH",
      "status": "TO_DO",
      "projectTitle": "Project Alpha"
    }
  }
]
```

### 5. Create Task

- **Endpoint**: `POST /api/tasks`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "startDate": "2025-01-01",
  "dueDate": "2025-02-15",
  "priority": "HIGH",
  "status": "TO_DO",
  "projectId": 1
}
```

### 6. Update Task

- **Endpoint**: `PUT /api/tasks/{id}`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "startDate": "2025-01-01",
  "dueDate": "2025-02-15",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "projectId": 1
}
```

### 7. Update Task Status (for Kanban)

- **Endpoint**: `PATCH /api/tasks/{id}/status`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "status": "IN_PROGRESS"
}
```

### 8. Delete Task

- **Endpoint**: `DELETE /api/tasks/{id}`
- **Headers**: `Authorization: Bearer {token}`

---

## Notes Management APIs

### 1. Get All Notes

- **Endpoint**: `GET /api/notes`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `?projectId=1` (optional)
  - `?search=keyword` (optional)
  - `?page=0&size=10&sort=createdAt,desc`
- **Response**:

```json
{
  "content": [
    {
      "id": 1,
      "content": "Note content here",
      "projectId": 1,
      "projectTitle": "Project Alpha",
      "createdAt": "2025-01-01T00:00:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### 2. Create Note

- **Endpoint**: `POST /api/notes`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "content": "string",
  "projectId": 1
}
```

### 3. Update Note

- **Endpoint**: `PUT /api/notes/{id}`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "content": "string",
  "projectId": 1
}
```

### 4. Delete Note

- **Endpoint**: `DELETE /api/notes/{id}`
- **Headers**: `Authorization: Bearer {token}`

---

## Issues Management APIs

### 1. Get All Issues

- **Endpoint**: `GET /api/issues`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `?projectId=1` (optional)
  - `?status=OPEN` (optional)
  - `?severity=HIGH` (optional)
  - `?page=0&size=10&sort=createdAt,desc`
- **Response**:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Bug in login",
      "description": "Issue description",
      "severity": "HIGH",
      "status": "OPEN",
      "projectId": 1,
      "projectTitle": "Project Alpha",
      "createdAt": "2025-01-01T00:00:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### 2. Create Issue

- **Endpoint**: `POST /api/issues`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "severity": "HIGH",
  "status": "OPEN",
  "projectId": 1
}
```

### 3. Update Issue

- **Endpoint**: `PUT /api/issues/{id}`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "severity": "HIGH",
  "status": "RESOLVED",
  "projectId": 1
}
```

### 4. Convert Issue to Task

- **Endpoint**: `POST /api/issues/{id}/convert-to-task`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "dueDate": "2025-02-15",
  "priority": "HIGH"
}
```

- **Response**:

```json
{
  "taskId": 10,
  "message": "Issue converted to task successfully"
}
```

### 5. Delete Issue

- **Endpoint**: `DELETE /api/issues/{id}`
- **Headers**: `Authorization: Bearer {token}`

---

## Timer Management APIs

### 1. Start Task Timer

- **Endpoint**: `POST /api/tasks/{taskId}/timer/start`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "success": true,
  "startTime": "2025-01-01T10:00:00",
  "message": "Timer started"
}
```

### 2. Stop Task Timer

- **Endpoint**: `POST /api/tasks/{taskId}/timer/stop`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "success": true,
  "endTime": "2025-01-01T12:00:00",
  "duration": 7200,
  "message": "Timer stopped"
}
```

### 3. Get Timer Status

- **Endpoint**: `GET /api/tasks/{taskId}/timer/status`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "running": true,
  "startTime": "2025-01-01T10:00:00",
  "currentDuration": 3600
}
```

### 4. Get Task Time History

- **Endpoint**: `GET /api/tasks/{taskId}/timer/history`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
[
  {
    "id": 1,
    "startTime": "2025-01-01T10:00:00",
    "endTime": "2025-01-01T12:00:00",
    "duration": 7200
  }
]
```

### 5. Reset Task Timer

- **Endpoint**: `DELETE /api/tasks/{taskId}/timer`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "success": true,
  "message": "Timer history cleared"
}
```

---

## Dashboard APIs

### 1. Get Dashboard Statistics

- **Endpoint**: `GET /api/dashboard/stats`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "totalProjects": 5,
  "totalTasks": 25,
  "pendingTasks": 10,
  "completedTasks": 12,
  "overdueTasks": 3,
  "totalIssues": 8,
  "openIssues": 5,
  "resolvedIssues": 3
}
```

### 2. Get Upcoming Tasks

- **Endpoint**: `GET /api/dashboard/upcoming-tasks`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**: `?days=7` (default: next 7 days)
- **Response**:

```json
[
  {
    "id": 1,
    "title": "Task 1",
    "dueDate": "2025-02-15",
    "priority": "HIGH",
    "projectTitle": "Project Alpha",
    "status": "TO_DO"
  }
]
```

### 3. Get Recent Activity

- **Endpoint**: `GET /api/dashboard/recent-activity`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**: `?limit=10`
- **Response**:

```json
[
  {
    "id": 1,
    "action": "Created task 'Fix login bug'",
    "timestamp": "2025-01-01T10:00:00",
    "user": "user123"
  }
]
```

---

## Import/Export APIs

### 1. Export Data

- **Endpoint**: `GET /api/export/{type}`
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**: `type` (tasks, projects, notes, issues)
- **Query Parameters**: `?format=csv` or `?format=json`
- **Response**: File download

### 2. Import Data

- **Endpoint**: `POST /api/import/{type}`
- **Headers**:
  - `Authorization: Bearer {token}`
  - `Content-Type: multipart/form-data`
- **Path Parameters**: `type` (tasks, projects, notes, issues)
- **Request Body**: Form data with file upload
- **Response**:

```json
{
  "success": true,
  "imported": 10,
  "failed": 2,
  "errors": [
    {
      "row": 3,
      "error": "Invalid date format"
    }
  ]
}
```

### 3. Database Backup

- **Endpoint**: `GET /api/backup/database`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: SQL file download

### 4. Reset Application Data

- **Endpoint**: `DELETE /api/reset/all`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "confirmationText": "RESET_ALL_DATA"
}
```

- **Response**:

```json
{
  "success": true,
  "message": "All data has been reset"
}
```

---

## Settings APIs

### 1. Get User Settings

- **Endpoint**: `GET /api/settings`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "theme": "light",
  "timezone": "UTC",
  "dateFormat": "yyyy-MM-dd",
  "notifications": {
    "email": true,
    "browser": true,
    "taskReminders": true
  }
}
```

### 2. Update User Settings

- **Endpoint**: `PUT /api/settings`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "theme": "dark",
  "timezone": "America/New_York",
  "dateFormat": "dd/MM/yyyy",
  "notifications": {
    "email": false,
    "browser": true,
    "taskReminders": true
  }
}
```

---

## Activity Logging APIs

### 1. Get Activity Logs

- **Endpoint**: `GET /api/activity-logs`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `?page=0&size=20&sort=timestamp,desc`
  - `?startDate=2025-01-01`
  - `?endDate=2025-12-31`
- **Response**:

```json
{
  "content": [
    {
      "id": 1,
      "user": "user123",
      "action": "Created project 'New Website'",
      "timestamp": "2025-01-01T10:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

---

## Technical Implementation Notes

### Security Considerations

1. Use JWT tokens for authentication
2. Implement role-based access control (RBAC)
3. Validate user ownership of resources
4. Use HTTPS for all endpoints
5. Implement rate limiting
6. Sanitize all input data

### Spring Boot Dependencies Required

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-csv</artifactId>
    </dependency>
</dependencies>
```

### Additional Features to Implement

1. **WebSocket Support**: For real-time updates (timer status, task updates)
2. **Caching**: Redis for session management and frequently accessed data
3. **File Upload**: For importing CSV/JSON files
4. **Email Notifications**: For task reminders and deadlines
5. **API Documentation**: Swagger/OpenAPI integration
6. **Logging**: Comprehensive application logging with Logback
7. **Health Checks**: Spring Boot Actuator endpoints
8. **Data Validation**: Bean Validation with custom validators

### Error Handling

Implement global exception handling with consistent error response format:

```json
{
  "error": true,
  "message": "Resource not found",
  "code": "RESOURCE_NOT_FOUND",
  "timestamp": "2025-01-01T10:00:00",
  "path": "/api/tasks/999"
}
```

This comprehensive API documentation covers all the functionality present in the original PHP Projo application and provides a solid foundation for the Spring Boot conversion.
