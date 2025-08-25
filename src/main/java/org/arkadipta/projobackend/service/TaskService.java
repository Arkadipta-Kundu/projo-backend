package org.arkadipta.projobackend.service;

import org.arkadipta.projobackend.dto.request.TaskRequest;
import org.arkadipta.projobackend.entity.*;
import org.arkadipta.projobackend.enums.Priority;
import org.arkadipta.projobackend.enums.TaskStatus;
import org.arkadipta.projobackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskTimeTrackingRepository timeTrackingRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private CacheService cacheService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Page<Task> getAllTasks(Pageable pageable, Long projectId, TaskStatus status, Priority priority) {
        User user = getCurrentUser();

        if (projectId != null && status != null) {
            Project project = projectRepository.findByIdAndUser(projectId, user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            return taskRepository.findByProjectAndStatus(project, status, pageable);
        } else if (projectId != null) {
            Project project = projectRepository.findByIdAndUser(projectId, user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            return taskRepository.findByProject(project, pageable);
        } else if (status != null) {
            return taskRepository.findByUserAndStatus(user, status, pageable);
        } else if (priority != null) {
            return taskRepository.findByUserAndPriority(user, priority, pageable);
        } else {
            return taskRepository.findByUser(user, pageable);
        }
    }

    @Cacheable(value = "tasks_kanban", key = "#projectId ?: 'all'")
    public List<Task> getTasksForKanban(Long projectId) {
        User user = getCurrentUser();

        if (projectId != null) {
            Project project = projectRepository.findByIdAndUser(projectId, user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            return taskRepository.findByProject(project);
        } else {
            return taskRepository.findByUser(user);
        }
    }

    @Cacheable(value = "tasks_gantt", key = "#projectId ?: 'all'")
    public List<Task> getTasksForGantt(Long projectId) {
        return getTasksForKanban(projectId); // Same logic for now
    }

    @Cacheable(value = "tasks_calendar", key = "#start + '_' + #end")
    public List<Task> getTasksForCalendar(LocalDate start, LocalDate end) {
        User user = getCurrentUser();
        return taskRepository.findByUserAndDueDateBetween(user, start, end);
    }

    @Cacheable(value = "task", key = "#id")
    public Task getTaskById(Long id) {
        User user = getCurrentUser();
        return taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @CacheEvict(value = { "tasks_kanban", "tasks_gantt", "tasks_calendar", "task" }, allEntries = true)
    public Task createTask(TaskRequest request) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUser(request.getProjectId(), user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setProject(project);
        task.setUser(user);

        Task savedTask = taskRepository.save(task);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Created task '" + task.getTitle() + "'");
        activityLogRepository.save(log);

        // Clear dashboard caches since task counts have changed
        cacheService.evictDashboardCaches();

        return savedTask;
    }

    @CacheEvict(value = { "tasks_kanban", "tasks_gantt", "tasks_calendar", "task" }, key = "#id")
    public Task updateTask(Long id, TaskRequest request) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = projectRepository.findByIdAndUser(request.getProjectId(), user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setProject(project);

        Task updatedTask = taskRepository.save(task);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Updated task '" + task.getTitle() + "'");
        activityLogRepository.save(log);

        // Clear dashboard caches since task counts might have changed
        cacheService.evictDashboardCaches();

        return updatedTask;
    }

    public Task updateTaskStatus(Long id, TaskStatus status) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        // Log activity
        ActivityLog log = new ActivityLog(user,
                "Changed task '" + task.getTitle() + "' status from " + oldStatus + " to " + status);
        activityLogRepository.save(log);

        return updatedTask;
    }

    public void deleteTask(Long id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        String taskTitle = task.getTitle();
        taskRepository.delete(task);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Deleted task '" + taskTitle + "'");
        activityLogRepository.save(log);
    }

    // Timer methods
    public void startTimer(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Check if there's already an active timer
        Optional<TaskTimeTracking> activeTimer = timeTrackingRepository.findActiveTimerByTask(task);
        if (activeTimer.isPresent()) {
            throw new RuntimeException("Timer is already running for this task");
        }

        TaskTimeTracking timeTracking = new TaskTimeTracking(task, LocalDateTime.now());
        timeTrackingRepository.save(timeTracking);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Started timer for task '" + task.getTitle() + "'");
        activityLogRepository.save(log);
    }

    public void stopTimer(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskTimeTracking activeTimer = timeTrackingRepository.findActiveTimerByTask(task)
                .orElseThrow(() -> new RuntimeException("No active timer found for this task"));

        LocalDateTime endTime = LocalDateTime.now();
        activeTimer.setEndTime(endTime);

        // Calculate duration in seconds
        long duration = java.time.Duration.between(activeTimer.getStartTime(), endTime).getSeconds();
        activeTimer.setDuration((int) duration);

        timeTrackingRepository.save(activeTimer);

        // Log activity
        ActivityLog log = new ActivityLog(user,
                "Stopped timer for task '" + task.getTitle() + "' (Duration: " + duration + "s)");
        activityLogRepository.save(log);
    }

    public boolean isTimerRunning(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        return timeTrackingRepository.findActiveTimerByTask(task).isPresent();
    }

    public List<TaskTimeTracking> getTimeHistory(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        return timeTrackingRepository.findByTaskOrderByStartTimeDesc(task);
    }

    public void resetTimer(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        timeTrackingRepository.deleteByTask(task);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Reset timer for task '" + task.getTitle() + "'");
        activityLogRepository.save(log);
    }

    public Long getTotalTimeSpent(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Long totalTime = timeTrackingRepository.getTotalTimeSpentByTask(task);
        return totalTime != null ? totalTime : 0L;
    }
}
