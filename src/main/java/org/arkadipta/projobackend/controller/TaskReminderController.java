package org.arkadipta.projobackend.controller;

import lombok.RequiredArgsConstructor;
import org.arkadipta.projobackend.dto.request.TaskReminderRequest;
import org.arkadipta.projobackend.dto.response.ApiResponse;
import org.arkadipta.projobackend.service.TaskReminderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tasks/reminders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TaskReminderController {

    private final TaskReminderService taskReminderService;

    /**
     * Set custom reminder for a task
     */
    @PostMapping("/{taskId}/custom")
    public ResponseEntity<ApiResponse<String>> setCustomReminder(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskReminderRequest request) {

        taskReminderService.setCustomReminder(taskId, request.getReminderTime());

        return ResponseEntity.ok(
                ApiResponse.success("Custom reminder set successfully",
                        "Reminder set for " + request.getReminderTime()));
    }

    /**
     * Set custom reminder for a task (using query parameters)
     */
    @PostMapping("/{taskId}/set")
    public ResponseEntity<ApiResponse<String>> setCustomReminderWithParams(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reminderTime) {

        taskReminderService.setCustomReminder(taskId, reminderTime);

        return ResponseEntity.ok(
                ApiResponse.success("Custom reminder set successfully",
                        "Reminder set for " + reminderTime));
    }

    /**
     * Disable reminders for a task
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<String>> disableReminders(@PathVariable Long taskId) {
        taskReminderService.disableReminders(taskId);

        return ResponseEntity.ok(
                ApiResponse.success("Reminders disabled successfully",
                        "Reminders disabled for task ID: " + taskId));
    }

    /**
     * Manual trigger for deadline reminders (for testing)
     */
    @PostMapping("/trigger/deadline")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> triggerDeadlineReminders() {
        taskReminderService.sendDeadlineReminders();

        return ResponseEntity.ok(
                ApiResponse.success("Deadline reminders triggered successfully",
                        "Check logs for details"));
    }

    /**
     * Manual trigger for custom reminders (for testing)
     */
    @PostMapping("/trigger/custom")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> triggerCustomReminders() {
        taskReminderService.sendCustomReminders();

        return ResponseEntity.ok(
                ApiResponse.success("Custom reminders triggered successfully",
                        "Check logs for details"));
    }

    /**
     * Manual trigger for overdue check (for testing)
     */
    @PostMapping("/trigger/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> triggerOverdueCheck() {
        taskReminderService.checkOverdueTasks();

        return ResponseEntity.ok(
                ApiResponse.success("Overdue tasks check triggered successfully",
                        "Check logs for details"));
    }
}
