# Projo Backend - Spring Boot Project Management API

A comprehensive project management system backend built with Spring Boot, providing REST APIs for managing projects, tasks, notes, issues, and time tracking.

## Features

- **User Authentication & Authorization** with JWT tokens
- **Project Management** - Create, update, delete, and organize projects
- **Task Management** - Full CRUD operations with Kanban, Gantt, and Calendar views
- **Time Tracking** - Start/stop timers for tasks with duration tracking
- **Issue Management** - Track bugs and issues with conversion to tasks
- **Notes Management** - Add contextual notes to projects
- **Dashboard** - Real-time statistics and recent activity
- **Activity Logging** - Track all user actions
- **RESTful APIs** - Complete REST API following OpenAPI standards

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.5**
- **Spring Security 6** with JWT authentication
- **Spring Data JPA** with Hibernate
- **MySQL 8** database
- **Maven** for dependency management
- **Bean Validation** for request validation

## Database Setup

1. Install MySQL 8.0+
2. Create a database named `projo_db` (or update `application.properties`)
3. Update database credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/projo_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Installation & Running

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd projo-backend
   ```

2. **Configure database** (see Database Setup above)

3. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

   Or on Windows:

   ```bash
   mvnw.cmd spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Health Check: `http://localhost:8080/actuator/health`

## API Documentation

### Authentication Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/validate` - Token validation
- `GET /api/auth/me` - Get current user

### Project Management

- `GET /api/projects` - Get all projects (paginated)
- `GET /api/projects/{id}` - Get project by ID
- `POST /api/projects` - Create new project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Task Management

- `GET /api/tasks` - Get all tasks (with filtering)
- `GET /api/tasks/kanban` - Get tasks for Kanban board
- `GET /api/tasks/gantt` - Get tasks for Gantt chart
- `GET /api/tasks/calendar` - Get tasks for calendar view
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Update task status
- `DELETE /api/tasks/{id}` - Delete task

### Time Tracking

- `POST /api/tasks/{taskId}/timer/start` - Start timer
- `POST /api/tasks/{taskId}/timer/stop` - Stop timer
- `GET /api/tasks/{taskId}/timer/status` - Get timer status
- `GET /api/tasks/{taskId}/timer/history` - Get time history
- `DELETE /api/tasks/{taskId}/timer` - Reset timer

### Issue Management

- `GET /api/issues` - Get all issues (with filtering)
- `POST /api/issues` - Create new issue
- `PUT /api/issues/{id}` - Update issue
- `POST /api/issues/{id}/convert-to-task` - Convert issue to task
- `DELETE /api/issues/{id}` - Delete issue

### Notes Management

- `GET /api/notes` - Get all notes (with search)
- `POST /api/notes` - Create new note
- `PUT /api/notes/{id}` - Update note
- `DELETE /api/notes/{id}` - Delete note

### Dashboard

- `GET /api/dashboard/stats` - Get dashboard statistics
- `GET /api/dashboard/upcoming-tasks` - Get upcoming tasks
- `GET /api/dashboard/recent-activity` - Get recent activity

## Request/Response Examples

### User Registration

```json
POST /api/auth/register
{
  "username": "johndoe",
  "password": "password123",
  "confirmPassword": "password123"
}
```

### User Login

```json
POST /api/auth/login
{
  "username": "johndoe",
  "password": "password123",
  "rememberMe": false
}

Response:
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "johndoe",
    "role": "user",
    "createdAt": "2025-01-01T00:00:00"
  }
}
```

### Create Project

```json
POST /api/projects
Authorization: Bearer <token>
{
  "title": "My New Project",
  "description": "Project description",
  "deadline": "2025-12-31"
}
```

### Create Task

```json
POST /api/tasks
Authorization: Bearer <token>
{
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication",
  "startDate": "2025-01-01",
  "dueDate": "2025-01-15",
  "priority": "HIGH",
  "status": "TO_DO",
  "projectId": 1
}
```

## Security

- JWT token-based authentication
- Password encryption using BCrypt
- Role-based access control
- CORS configuration for frontend integration
- Input validation on all endpoints

## Database Schema

The application uses the following main entities:

- **User** - User accounts and authentication
- **Project** - Project information and metadata
- **Task** - Individual tasks within projects
- **Note** - Text notes associated with projects
- **Issue** - Bug reports and issues
- **TaskTimeTracking** - Time tracking for tasks
- **ActivityLog** - User activity and audit trail

## Configuration

Key configuration properties in `application.properties`:

```properties
# JWT Configuration
jwt.secret=ProjoSecretKeyForJWTTokenGenerationThatShouldBeLongEnoughAndSecure2024
jwt.expiration=86400000

# Server Configuration
server.port=8080

# Database Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Error Handling

The application includes comprehensive error handling:

- Global exception handler for consistent error responses
- Validation error messages
- Authentication and authorization errors
- Custom business logic exceptions

## Development

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Building

```bash
./mvnw clean compile
```

### Testing

```bash
./mvnw test
```

### Packaging

```bash
./mvnw clean package
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
# projo-backend
