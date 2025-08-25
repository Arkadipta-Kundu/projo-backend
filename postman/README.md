# Projo Backend API - Postman Collection

This comprehensive Postman collection includes all API endpoints for the Projo Backend application with complete testing capabilities.

## 📋 Collection Features

### ✅ **Authentication & Email Verification**

- User Registration with Email Verification
- Email OTP Verification
- User Login with JWT Token
- Password Reset with OTP
- Change Password

### ✅ **Project Management**

- Create, Read, Update, Delete Projects
- Project Search and Filtering
- Pagination Support
- Caching Implementation

### ✅ **Task Management**

- Complete CRUD Operations
- Task Status Management (TO_DO, IN_PROGRESS, DONE)
- Priority Levels (LOW, MEDIUM, HIGH)
- Time Tracking Features
- Task Reminders and Notifications

### ✅ **Issues Management** 🆕

- Create, Read, Update, Delete Issues
- Issue Severity Management (LOW, MEDIUM, HIGH, CRITICAL)
- Issue Status Tracking (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
- Advanced Filtering and Search
- Issue Statistics and Analytics
- Comprehensive Caching

### ✅ **Notes Management** 🆕

- Privacy Controls (Public/Private Notes)
- Collaboration Features (Collaborative Editing)
- Personal Notes (Without Project Association)
- Project-Associated Notes
- Advanced Access Control
- Note Statistics
- Comprehensive Caching

### ✅ **Collaboration Management** 🆕

- Project Member Invitations
- Invite Status Management (PENDING/ACCEPTED/DECLINED)
- Member Access Control
- Project Ownership Verification
- Collaboration Statistics
- Member Management (Add/Remove/Leave)

### ✅ **Dashboard & Analytics**

- Dashboard Data with Caching
- Performance Metrics
- User Activity Statistics

## 🚀 Quick Start Guide

### 1. **Import Collection & Environment**

1. Import `Projo_Backend_API_Complete_With_Email_Verification.postman_collection.json`
2. Import `Projo_Backend_Environment.postman_environment.json`
3. Select the imported environment in Postman

### 2. **Configure Environment Variables**

Update these variables in your environment:

- `base_url`: Your API server URL (default: `http://localhost:8080`)
- `test_email`: Your actual email address for OTP verification
- `test_username`: Your preferred test username
- `test_password`: Your test password (must meet security requirements)

### 3. **Authentication Flow**

1. **Register User**: Creates a new account and sends verification email
2. **Verify Email**: Use OTP from your email to verify the account
3. **Login**: Authenticate and get JWT token (auto-saved to environment)

### 4. **Test Data Flow**

The collection automatically sets IDs for created resources:

- `project_id` - Set after creating a project
- `task_id` - Set after creating a task
- `issue_id` - Set after creating an issue
- `note_id` - Set after creating a note
- `membership_id` - Set after inviting a user

## 📁 Collection Structure

### 🔐 Authentication & Email Verification

- Register User (Sends Verification Email)
- Verify Email with OTP
- Login User (Auto-saves JWT Token)
- Forgot Password (Sends Reset OTP)
- Reset Password with OTP
- Change Password

### 📂 Project Management

- Create Project (Auto-saves Project ID)
- Get All Projects (Cached)
- Get Project by ID (Cached)
- Update Project
- Delete Project
- Search Projects

### ✅ Task Management

- Create Task (Auto-saves Task ID)
- Get All Tasks (Cached)
- Get Task by ID (Cached)
- Update Task
- Delete Task
- Filter Tasks (Status, Priority, Project)
- Search Tasks
- Task Time Tracking (Start/Stop/Log)
- Task Reminders (Custom/Deadline)

### 🐛 Issues Management 🆕

- Create Issue (Auto-saves Issue ID)
- Get All Issues (Cached)
- Get Issue by ID (Cached)
- Update Issue
- Delete Issue
- Filter Issues (Status, Severity, Project)
- Search Issues
- Get Issue Statistics (Cached)

### 📝 Notes Management 🆕

- Create Public Note (Auto-saves Note ID)
- Create Private Note
- Create Collaborative Note
- Create Personal Note (No Project)
- Get All Accessible Notes (Cached)
- Get Note by ID
- Update Note
- Delete Note
- Filter Notes by Project
- Search Notes
- Get Note Statistics (Cached)

### 🤝 Collaboration Management 🆕

- Invite User to Project (Auto-saves Membership ID)
- Respond to Invitation (Accept/Decline)
- Get Project Members (Cached)
- Get My Project Memberships (Cached)
- Get Pending Invitations (Cached)
- Remove Member from Project
- Leave Project
- Get Collaboration Statistics (Cached)

### 📈 Dashboard API

- Get Dashboard Data (Cached)

## 🔧 Advanced Features

### **Automatic Test Scripts**

- JWT tokens are automatically extracted and saved
- Resource IDs are automatically captured for subsequent requests
- Dynamic timestamps for unique test data

### **Caching Validation**

- Multiple requests to cached endpoints to verify cache performance
- Cache invalidation testing after create/update/delete operations

### **Error Handling**

- Comprehensive error response testing
- Authentication error validation
- Permission and access control testing

### **Environment Variables**

Pre-configured variables for easy testing:

```
base_url: http://localhost:8080
test_username: testuser123
test_email: your-email@gmail.com
test_password: TestPass123!
project_title: Sample Project
task_title: Sample Task
collaborator_email: collaborator@example.com
```

## 📋 Testing Workflow

### **Complete Application Test**

1. **Authentication**: Register → Verify → Login
2. **Project Setup**: Create Project
3. **Task Management**: Create → Update → Track Time
4. **Issue Tracking**: Create → Update → Resolve
5. **Notes Management**: Create Different Types → Collaborate
6. **Collaboration**: Invite Users → Manage Members
7. **Analytics**: View Dashboard → Check Statistics

### **Feature-Specific Testing**

Each API section can be tested independently once authentication is complete.

### **Cache Performance Testing**

1. Make initial request (cache miss)
2. Make subsequent requests (cache hit)
3. Perform update operation (cache invalidation)
4. Verify cache refresh

## 🛡️ Security Features

- JWT Bearer Token Authentication
- Role-based Access Control
- Email Verification Required
- Password Strength Requirements
- API Rate Limiting
- CORS Configuration

## 📊 Analytics & Statistics

The collection includes comprehensive statistics endpoints:

- **Issue Statistics**: Count by status, severity, and user
- **Note Statistics**: Privacy and collaboration metrics
- **Collaboration Statistics**: Project memberships and invitations
- **Dashboard Metrics**: Overall application analytics

## 🔄 Auto-Generated Data

The collection automatically generates:

- Unique usernames with timestamps
- Dynamic email addresses
- Timestamped content for notes and issues
- Future dates for deadlines and due dates

## 💡 Tips for Best Results

1. **Use Real Email**: Set `test_email` to your actual email for OTP verification
2. **Sequential Testing**: Follow the authentication flow before testing other features
3. **Check Logs**: Monitor application logs for detailed error information
4. **Cache Testing**: Make multiple requests to verify caching behavior
5. **Environment Switching**: Use different environments for different testing scenarios

## 🆕 Version 3.0.0 New Features

- **Complete Issues Management System** with statistics and caching
- **Advanced Notes Management** with privacy and collaboration controls
- **Full Collaboration System** with invitations and member management
- **Enhanced Caching Strategy** across all new endpoints
- **Comprehensive API Documentation** with Swagger integration
- **Advanced Security Controls** with role-based permissions

---

**Happy Testing! 🚀**

For more information about the API endpoints, check the Swagger documentation at `http://localhost:8080/swagger-ui.html` when the application is running.
