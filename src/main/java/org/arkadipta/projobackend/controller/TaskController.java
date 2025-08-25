package org.arkadipta.projobackend.controller;

import jakarta.validation.Valid;
import org.arkadipta.projobackend.dto.request.TaskRequest;
import org.arkadipta.projobackend.dto.response.ApiResponse;
import org.arkadipta.projobackend.entity.Task;
import org.arkadipta.projobackend.entity.TaskTimeTracking;
import org.arkadipta.projobackend.enums.Priority;
import org.arkadipta.projobackend.enums.TaskStatus;
import org.arkadipta.projobackend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "http://localhost:5174",
        "http://127.0.0.1:5174" })
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Task> taskPage = taskService.getAllTasks(pageable, projectId, status, priority);

            List<Map<String, Object>> taskList = taskPage.getContent().stream().map(task -> {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("title", task.getTitle());
                taskMap.put("description", task.getDescription());
                taskMap.put("startDate", task.getStartDate());
                taskMap.put("dueDate", task.getDueDate());
                taskMap.put("priority", task.getPriority());
                taskMap.put("status", task.getStatus());
                taskMap.put("projectId", task.getProject().getId());
                taskMap.put("projectTitle", task.getProject().getTitle());
                taskMap.put("createdAt", task.getCreatedAt());
                taskMap.put("isTimerRunning", taskService.isTimerRunning(task.getId()));
                taskMap.put("totalTimeSpent", taskService.getTotalTimeSpent(task.getId()));
                return taskMap;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("content", taskList);
            response.put("totalElements", taskPage.getTotalElements());
            response.put("totalPages", taskPage.getTotalPages());
            response.put("number", taskPage.getNumber());
            response.put("size", taskPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/kanban")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getTasksForKanban(
            @RequestParam(required = false) Long projectId) {

        try {
            List<Task> tasks = taskService.getTasksForKanban(projectId);

            Map<String, List<Map<String, Object>>> kanbanBoard = tasks.stream()
                    .collect(Collectors.groupingBy(
                            task -> task.getStatus().toString(),
                            Collectors.mapping(task -> {
                                Map<String, Object> taskMap = new HashMap<>();
                                taskMap.put("id", task.getId());
                                taskMap.put("title", task.getTitle());
                                taskMap.put("priority", task.getPriority());
                                taskMap.put("dueDate", task.getDueDate());
                                taskMap.put("projectTitle", task.getProject().getTitle());
                                return taskMap;
                            }, Collectors.toList())));

            // Ensure all status columns exist
            for (TaskStatus status : TaskStatus.values()) {
                kanbanBoard.putIfAbsent(status.toString(), List.of());
            }

            return ResponseEntity.ok(kanbanBoard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/gantt")
    public ResponseEntity<List<Map<String, Object>>> getTasksForGantt(
            @RequestParam(required = false) Long projectId) {

        try {
            List<Task> tasks = taskService.getTasksForGantt(projectId);

            List<Map<String, Object>> ganttTasks = tasks.stream().map(task -> {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("title", task.getTitle());
                taskMap.put("startDate", task.getStartDate());
                taskMap.put("endDate", task.getDueDate());

                // Calculate progress based on status
                double progress = switch (task.getStatus()) {
                    case TO_DO -> 0.0;
                    case IN_PROGRESS -> 0.5;
                    case DONE, COMPLETED -> 1.0;
                    case OVERDUE -> 0.2; // Slightly progressed but overdue
                    case CANCELLED -> 0.0;
                };
                taskMap.put("progress", progress);

                taskMap.put("projectId", task.getProject().getId());
                taskMap.put("projectTitle", task.getProject().getTitle());
                taskMap.put("priority", task.getPriority());

                return taskMap;
            }).toList();

            return ResponseEntity.ok(ganttTasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<Map<String, Object>>> getTasksForCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        try {
            List<Task> tasks = taskService.getTasksForCalendar(start, end);

            List<Map<String, Object>> calendarEvents = tasks.stream().map(task -> {
                Map<String, Object> event = new HashMap<>();
                event.put("id", task.getId());
                event.put("title", task.getTitle());
                event.put("start", task.getDueDate().toString());

                String backgroundColor = switch (task.getPriority()) {
                    case HIGH -> "#ef4444";
                    case MEDIUM -> "#f59e0b";
                    case LOW -> "#10b981";
                };
                event.put("backgroundColor", backgroundColor);

                String borderColor = switch (task.getPriority()) {
                    case HIGH -> "#dc2626";
                    case MEDIUM -> "#d97706";
                    case LOW -> "#059669";
                };
                event.put("borderColor", borderColor);

                event.put("url", "/tasks/" + task.getId());

                Map<String, Object> extendedProps = new HashMap<>();
                extendedProps.put("priority", task.getPriority());
                extendedProps.put("status", task.getStatus());
                extendedProps.put("projectTitle", task.getProject().getTitle());
                event.put("extendedProps", extendedProps);

                return event;
            }).toList();

            return ResponseEntity.ok(calendarEvents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest request) {
        try {
            Task task = taskService.createTask(request);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        try {
            Task task = taskService.updateTask(id, request);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            TaskStatus status = TaskStatus.valueOf(request.get("status"));
            Task task = taskService.updateTaskStatus(id, status);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Timer endpoints
    @PostMapping("/{taskId}/timer/start")
    public ResponseEntity<Map<String, Object>> startTimer(@PathVariable Long taskId) {
        try {
            taskService.startTimer(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("startTime", LocalDateTime.now());
            response.put("message", "Timer started");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{taskId}/timer/stop")
    public ResponseEntity<Map<String, Object>> stopTimer(@PathVariable Long taskId) {
        try {
            taskService.stopTimer(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("endTime", LocalDateTime.now());
            response.put("message", "Timer stopped");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{taskId}/timer/status")
    public ResponseEntity<Map<String, Object>> getTimerStatus(@PathVariable Long taskId) {
        try {
            boolean running = taskService.isTimerRunning(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("running", running);

            if (running) {
                List<TaskTimeTracking> history = taskService.getTimeHistory(taskId);
                if (!history.isEmpty()) {
                    TaskTimeTracking activeTimer = history.get(0);
                    response.put("startTime", activeTimer.getStartTime());

                    long currentDuration = java.time.Duration.between(
                            activeTimer.getStartTime(),
                            LocalDateTime.now()).getSeconds();
                    response.put("currentDuration", currentDuration);
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{taskId}/timer/history")
    public ResponseEntity<List<TaskTimeTracking>> getTimeHistory(@PathVariable Long taskId) {
        try {
            List<TaskTimeTracking> history = taskService.getTimeHistory(taskId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{taskId}/timer")
    public ResponseEntity<ApiResponse<String>> resetTimer(@PathVariable Long taskId) {
        try {
            taskService.resetTimer(taskId);
            return ResponseEntity.ok(ApiResponse.success("Timer history cleared"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
