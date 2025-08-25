package org.arkadipta.projobackend.service;

import org.arkadipta.projobackend.entity.ActivityLog;
import org.arkadipta.projobackend.entity.Task;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.enums.IssueStatus;
import org.arkadipta.projobackend.enums.TaskStatus;
import org.arkadipta.projobackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Cacheable(value = "dashboard_stats", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public Map<String, Object> getDashboardStats() {
        User user = getCurrentUser();

        long totalProjects = projectRepository.findByUser(user).size();
        long totalTasks = taskRepository.countByUserAndStatus(user, TaskStatus.TO_DO) +
                taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS) +
                taskRepository.countByUserAndStatus(user, TaskStatus.DONE);

        long pendingTasks = taskRepository.countByUserAndStatus(user, TaskStatus.TO_DO);
        long inProgressTasks = taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByUserAndStatus(user, TaskStatus.DONE);

        List<Task> overdueTasks = taskRepository.findOverdueTasksByUser(user, LocalDate.now(), TaskStatus.DONE);

        long totalIssues = issueRepository.countByUserAndStatus(user, IssueStatus.OPEN) +
                issueRepository.countByUserAndStatus(user, IssueStatus.RESOLVED);
        long openIssues = issueRepository.countByUserAndStatus(user, IssueStatus.OPEN);
        long resolvedIssues = issueRepository.countByUserAndStatus(user, IssueStatus.RESOLVED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProjects", totalProjects);
        stats.put("totalTasks", totalTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("overdueTasks", overdueTasks.size());
        stats.put("totalIssues", totalIssues);
        stats.put("openIssues", openIssues);
        stats.put("resolvedIssues", resolvedIssues);

        return stats;
    }

    @Cacheable(value = "upcoming_tasks", key = "#days")
    public List<Map<String, Object>> getUpcomingTasks(int days) {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<Task> upcomingTasks = taskRepository.findByUserAndDueDateBetween(user, today, endDate);

        return upcomingTasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("id", task.getId());
                    taskMap.put("title", task.getTitle());
                    taskMap.put("dueDate", task.getDueDate());
                    taskMap.put("priority", task.getPriority());
                    taskMap.put("projectTitle", task.getProject().getTitle());
                    taskMap.put("status", task.getStatus());
                    return taskMap;
                })
                .toList();
    }

    @Cacheable(value = "recent_activity", key = "#limit")
    public List<Map<String, Object>> getRecentActivity(int limit) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(0, limit);

        List<ActivityLog> recentActivities = activityLogRepository.findRecentActivitiesByUser(user, pageable);

        return recentActivities.stream().map(activity -> {
            Map<String, Object> activityMap = new HashMap<>();
            activityMap.put("id", activity.getId());
            activityMap.put("action", activity.getAction());
            activityMap.put("timestamp", activity.getTimestamp());
            activityMap.put("user", activity.getUser().getUsername());
            return activityMap;
        }).toList();
    }
}
