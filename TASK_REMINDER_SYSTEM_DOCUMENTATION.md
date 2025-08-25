# Task Reminder System Documentation

## Overview

The Task Reminder System is a comprehensive email-based notification system that helps users stay on top of their tasks through automated reminders. The system supports both custom reminders and automatic deadline notifications.

## Features

### 1. Database Persistence Fix

- **Problem**: Data wasn't saving permanently due to `create-drop` DDL auto setting
- **Solution**: Changed to `update` mode for persistent data storage
- **Configuration**: `spring.jpa.hibernate.ddl-auto=update`

### 2. Custom Reminders

- Users can set personalized reminder times for any task
- Flexible scheduling with precise datetime selection
- Email notifications sent at specified times
- One-time custom reminders per task

### 3. Deadline Reminders

- Automatic reminders for tasks due today or tomorrow
- Runs daily at 9:00 AM via cron jobs
- Smart detection of upcoming deadlines
- Prevents duplicate reminder emails

### 4. Overdue Task Management

- Automatic detection of overdue tasks
- Status updates to "OVERDUE" for past-due items
- Daily check at 10:00 AM via cron jobs
- Immediate notification emails

### 5. Beautiful Email Templates

- Clean blue and white theme matching app design
- Responsive design for mobile and desktop
- Professional formatting with task details
- Productivity tips included in emails

## Technical Architecture

### Database Schema Updates

The `Task` entity has been enhanced with reminder-related fields:

```sql
-- New columns added to tasks table
reminder_time TIMESTAMP,
reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
deadline_reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
custom_reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
is_completed BOOLEAN NOT NULL DEFAULT FALSE
```

### Cron Job Schedule

1. **Custom Reminders**: Every 5 minutes (`fixedRate = 300000`)
2. **Deadline Reminders**: Daily at 9:00 AM (`0 0 9 * * ?`)
3. **Overdue Check**: Daily at 10:00 AM (`0 0 10 * * ?`)

## API Endpoints

### Set Custom Reminder

**POST** `/api/tasks/reminders/{taskId}/custom`

```json
{
  "reminderTime": "2025-08-26T14:30:00"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Custom reminder set successfully",
  "data": "Reminder set for 2025-08-26T14:30:00"
}
```

### Set Custom Reminder (Query Params)

**POST** `/api/tasks/reminders/{taskId}/set?reminderTime=2025-08-26T14:30:00`

### Disable Reminders

**DELETE** `/api/tasks/reminders/{taskId}`

**Response:**

```json
{
  "success": true,
  "message": "Reminders disabled successfully",
  "data": "Reminders disabled for task ID: 123"
}
```

### Admin Testing Endpoints

**POST** `/api/tasks/reminders/trigger/deadline` - Trigger deadline reminders manually
**POST** `/api/tasks/reminders/trigger/custom` - Trigger custom reminders manually
**POST** `/api/tasks/reminders/trigger/overdue` - Trigger overdue check manually

## Email Types

### 1. Custom Reminder Email

- **Subject**: ⏰ Task Reminder: [Task Title]
- **Content**: Personalized reminder with task details
- **Trigger**: User-defined custom time

### 2. Deadline Reminder Email

- **Subject**: 🚨 Task Due [TODAY/TOMORROW]: [Task Title]
- **Content**: Urgent notification for upcoming deadlines
- **Trigger**: Daily cron job at 9:00 AM

### 3. Overdue Notification Email

- **Subject**: ❗ OVERDUE Task: [Task Title]
- **Content**: Alert for past-due tasks
- **Trigger**: Daily cron job at 10:00 AM

## Configuration

### Application Properties

```properties
# Database Configuration - Fixed for persistence
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# Task Reminder Configuration
app.reminder.cron.default=0 0 9 * * ?
app.reminder.enabled=true

# Scheduling Configuration
spring.task.scheduling.pool.size=5
```

### Email Configuration

Uses existing SMTP configuration with beautiful HTML templates:

- Template Location: `src/main/resources/templates/email/task-reminder.html`
- Theme: Clean blue and white design
- Responsive: Mobile-friendly layout

## Service Components

### 1. TaskReminderService

- Core reminder logic and cron job handlers
- Database queries for reminder-eligible tasks
- Email sending coordination

### 2. TaskReminderEmailService

- HTML email template processing
- SMTP email delivery
- Error handling and logging

### 3. TaskReminderController

- REST API endpoints for reminder management
- Admin testing endpoints
- Request validation and response formatting

## Usage Examples

### Setting a Custom Reminder

```bash
curl -X POST "http://localhost:8080/api/tasks/reminders/123/custom" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "reminderTime": "2025-08-26T15:30:00"
  }'
```

### Disabling Reminders

```bash
curl -X DELETE "http://localhost:8080/api/tasks/reminders/123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Testing Deadline Reminders (Admin)

```bash
curl -X POST "http://localhost:8080/api/tasks/reminders/trigger/deadline" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## Scheduling Details

### Custom Reminder Logic

1. Runs every 5 minutes
2. Finds tasks with `reminderTime <= now`
3. Filters: `customReminderSent = false`, `reminderEnabled = true`, `isCompleted = false`
4. Sends email and marks `customReminderSent = true`

### Deadline Reminder Logic

1. Runs daily at 9:00 AM
2. Finds tasks due today or tomorrow
3. Filters: `deadlineReminderSent = false`, `reminderEnabled = true`, `isCompleted = false`
4. Sends email and marks `deadlineReminderSent = true`

### Overdue Task Logic

1. Runs daily at 10:00 AM
2. Finds tasks with `dueDate < today`
3. Filters: status not COMPLETED or OVERDUE, `isCompleted = false`
4. Updates status to OVERDUE and sends notification

## Security

- User-level access for reminder management
- Admin-level access for testing triggers
- JWT token validation on all endpoints
- Users can only manage their own task reminders

## Logging

All reminder activities are logged with:

- Success notifications
- Error handling with details
- Performance metrics
- Email delivery status

## Performance Considerations

- Efficient database queries with proper indexing
- Batch processing for multiple reminders
- Configurable thread pool for scheduling
- Error handling to prevent system disruption

## Troubleshooting

### Common Issues

1. **Reminders not sending**: Check email SMTP configuration
2. **Cron jobs not running**: Verify `@EnableScheduling` annotation
3. **Database persistence**: Ensure DDL auto is set to `update`
4. **Template errors**: Check Thymeleaf template syntax

### Monitoring

- Check application logs for reminder activity
- Monitor email delivery success rates
- Verify cron job execution times
- Track database performance for reminder queries

## Future Enhancements

- SMS reminder integration
- Reminder frequency configuration
- Custom reminder templates
- Reminder analytics dashboard
- Mobile push notifications
