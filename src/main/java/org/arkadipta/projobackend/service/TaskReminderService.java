package org.arkadipta.projobackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.arkadipta.projobackend.entity.Task;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.repository.TaskRepository;
import org.arkadipta.projobackend.enums.TaskStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskReminderService {

    private final TaskRepository taskRepository;
    private final TaskReminderEmailService taskReminderEmailService;

    /**
     * Send custom reminders - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    @Transactional
    public void sendCustomReminders() {
        log.info("Checking for custom task reminders...");

        LocalDateTime now = LocalDateTime.now();

        // Find tasks with custom reminder time that should be sent now
        List<Task> tasksForCustomReminder = taskRepository.findTasksForCustomReminder(now);

        for (Task task : tasksForCustomReminder) {
            try {
                sendCustomReminderEmail(task);
                task.setCustomReminderSent(true);
                taskRepository.save(task);
                log.info("Custom reminder sent for task: {} to user: {}", task.getTitle(), task.getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to send custom reminder for task: {} to user: {}",
                        task.getTitle(), task.getUser().getEmail(), e);
            }
        }
    }

    /**
     * Send deadline reminders - runs daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendDeadlineReminders() {
        log.info("Checking for deadline task reminders...");

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // Find tasks with deadline today or tomorrow that haven't been reminded
        List<Task> tasksForDeadlineReminder = taskRepository.findTasksForDeadlineReminder(today, tomorrow);

        for (Task task : tasksForDeadlineReminder) {
            try {
                sendDeadlineReminderEmail(task);
                task.setDeadlineReminderSent(true);
                taskRepository.save(task);
                log.info("Deadline reminder sent for task: {} to user: {}", task.getTitle(), task.getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to send deadline reminder for task: {} to user: {}",
                        task.getTitle(), task.getUser().getEmail(), e);
            }
        }
    }

    /**
     * Check for overdue tasks - runs daily at 10 AM
     */
    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void checkOverdueTasks() {
        log.info("Checking for overdue tasks...");

        LocalDate today = LocalDate.now();

        // Find tasks that are overdue and not completed
        List<Task> overdueTasks = taskRepository.findOverdueTasks(today);

        for (Task task : overdueTasks) {
            if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.OVERDUE) {
                task.setStatus(TaskStatus.OVERDUE);
                taskRepository.save(task);
                log.info("Task marked as overdue: {}", task.getTitle());

                // Send overdue notification
                try {
                    sendOverdueReminderEmail(task);
                    log.info("Overdue notification sent for task: {} to user: {}", task.getTitle(),
                            task.getUser().getEmail());
                } catch (Exception e) {
                    log.error("Failed to send overdue notification for task: {} to user: {}",
                            task.getTitle(), task.getUser().getEmail(), e);
                }
            }
        }
    }

    /**
     * Send custom reminder email
     */
    private void sendCustomReminderEmail(Task task) {
        User user = task.getUser();
        String subject = "⏰ Task Reminder: " + task.getTitle();

        StringBuilder message = new StringBuilder();
        message.append("Hello ").append(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .append(",\n\n");
        message.append("This is a friendly reminder about your task:\n\n");
        message.append("📋 Task: ").append(task.getTitle()).append("\n");

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            message.append("📝 Description: ").append(task.getDescription()).append("\n");
        }

        if (task.getDueDate() != null) {
            message.append("📅 Due Date: ")
                    .append(task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
        }

        message.append("🎯 Priority: ").append(task.getPriority()).append("\n");
        message.append("📊 Status: ").append(task.getStatus()).append("\n\n");
        message.append("Don't forget to work on this task!\n\n");
        message.append("Best regards,\nProjo Team");

        taskReminderEmailService.sendTaskReminderEmail(user.getEmail(), subject, message.toString());
    }

    /**
     * Send deadline reminder email
     */
    private void sendDeadlineReminderEmail(Task task) {
        User user = task.getUser();
        LocalDate dueDate = task.getDueDate();
        LocalDate today = LocalDate.now();

        String timeFrame;
        if (dueDate.equals(today)) {
            timeFrame = "TODAY";
        } else if (dueDate.equals(today.plusDays(1))) {
            timeFrame = "TOMORROW";
        } else {
            timeFrame = "SOON";
        }

        String subject = "🚨 Task Due " + timeFrame + ": " + task.getTitle();

        StringBuilder message = new StringBuilder();
        message.append("Hello ").append(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .append(",\n\n");
        message.append("⚠️ Your task is due ").append(timeFrame.toLowerCase()).append("!\n\n");
        message.append("📋 Task: ").append(task.getTitle()).append("\n");

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            message.append("📝 Description: ").append(task.getDescription()).append("\n");
        }

        message.append("📅 Due Date: ").append(dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                .append("\n");
        message.append("🎯 Priority: ").append(task.getPriority()).append("\n");
        message.append("📊 Status: ").append(task.getStatus()).append("\n\n");

        if (timeFrame.equals("TODAY")) {
            message.append("⏰ This task is due today! Please complete it as soon as possible.\n\n");
        } else if (timeFrame.equals("TOMORROW")) {
            message.append("⏰ This task is due tomorrow! Please plan to complete it.\n\n");
        }

        message.append("Best regards,\nProjo Team");

        taskReminderEmailService.sendTaskReminderEmail(user.getEmail(), subject, message.toString());
    }

    /**
     * Send overdue reminder email
     */
    private void sendOverdueReminderEmail(Task task) {
        User user = task.getUser();
        String subject = "❗ OVERDUE Task: " + task.getTitle();

        StringBuilder message = new StringBuilder();
        message.append("Hello ").append(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .append(",\n\n");
        message.append("🔴 Your task is now OVERDUE!\n\n");
        message.append("📋 Task: ").append(task.getTitle()).append("\n");

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            message.append("📝 Description: ").append(task.getDescription()).append("\n");
        }

        message.append("📅 Due Date: ").append(task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                .append("\n");
        message.append("🎯 Priority: ").append(task.getPriority()).append("\n");
        message.append("📊 Status: OVERDUE\n\n");
        message.append("⚠️ This task is past its deadline. Please complete it immediately or update its status.\n\n");
        message.append("Best regards,\nProjo Team");

        taskReminderEmailService.sendTaskReminderEmail(user.getEmail(), subject, message.toString());
    }

    /**
     * Set custom reminder for a specific task
     */
    @Transactional
    public void setCustomReminder(Long taskId, LocalDateTime reminderTime) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setReminderTime(reminderTime);
        task.setCustomReminderSent(false);
        task.setReminderEnabled(true);
        taskRepository.save(task);

        log.info("Custom reminder set for task: {} at {}", task.getTitle(), reminderTime);
    }

    /**
     * Disable reminders for a specific task
     */
    @Transactional
    public void disableReminders(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setReminderEnabled(false);
        taskRepository.save(task);

        log.info("Reminders disabled for task: {}", task.getTitle());
    }
}
