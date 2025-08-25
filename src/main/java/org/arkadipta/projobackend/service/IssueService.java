package org.arkadipta.projobackend.service;

import org.arkadipta.projobackend.dto.request.IssueRequest;
import org.arkadipta.projobackend.entity.*;
import org.arkadipta.projobackend.enums.IssueStatus;
import org.arkadipta.projobackend.enums.Priority;
import org.arkadipta.projobackend.enums.Severity;
import org.arkadipta.projobackend.enums.TaskStatus;
import org.arkadipta.projobackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Cacheable(value = "issues", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #projectId + '_' + #status + '_' + #severity")
    public Page<Issue> getAllIssues(Pageable pageable, Long projectId, IssueStatus status, Severity severity) {
        User user = getCurrentUser();

        if (projectId != null && status != null) {
            Project project = projectRepository.findByIdAndUser(projectId, user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            return issueRepository.findByProjectAndStatus(project, status, pageable);
        } else if (projectId != null) {
            Project project = projectRepository.findByIdAndUser(projectId, user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            return issueRepository.findByProject(project, pageable);
        } else if (status != null) {
            return issueRepository.findByUserAndStatus(user, status, pageable);
        } else if (severity != null) {
            return issueRepository.findByUserAndSeverity(user, severity, pageable);
        } else {
            return issueRepository.findByUser(user, pageable);
        }
    }

    @Cacheable(value = "issue", key = "#id")
    public Issue getIssueById(Long id) {
        User user = getCurrentUser();
        return issueRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
    }

    @Caching(evict = {
            @CacheEvict(value = "issues", allEntries = true),
            @CacheEvict(value = "issue", key = "#result.id")
    })
    public Issue createIssue(IssueRequest request) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUser(request.getProjectId(), user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setSeverity(request.getSeverity());
        issue.setStatus(request.getStatus());
        issue.setProject(project);
        issue.setUser(user);

        Issue savedIssue = issueRepository.save(issue);

        // Log activity
        ActivityLog log = new ActivityLog(user,
                "Created issue '" + issue.getTitle() + "' in project '" + project.getTitle() + "'");
        activityLogRepository.save(log);

        return savedIssue;
    }

    @Caching(evict = {
            @CacheEvict(value = "issues", allEntries = true),
            @CacheEvict(value = "issue", key = "#id")
    })
    public Issue updateIssue(Long id, IssueRequest request) {
        User user = getCurrentUser();
        Issue issue = issueRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        Project project = projectRepository.findByIdAndUser(request.getProjectId(), user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setSeverity(request.getSeverity());
        issue.setStatus(request.getStatus());
        issue.setProject(project);

        Issue updatedIssue = issueRepository.save(issue);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Updated issue '" + issue.getTitle() + "'");
        activityLogRepository.save(log);

        return updatedIssue;
    }

    @Caching(evict = {
            @CacheEvict(value = "issues", allEntries = true),
            @CacheEvict(value = "issue", key = "#id")
    })
    public void deleteIssue(Long id) {
        User user = getCurrentUser();
        Issue issue = issueRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        String issueTitle = issue.getTitle();
        issueRepository.delete(issue);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Deleted issue '" + issueTitle + "'");
        activityLogRepository.save(log);
    }

    public Task convertIssueToTask(Long issueId, Map<String, Object> conversionData) {
        User user = getCurrentUser();
        Issue issue = issueRepository.findByIdAndUser(issueId, user)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        // Create task from issue
        Task task = new Task();
        task.setTitle(issue.getTitle());
        task.setDescription(issue.getDescription());
        task.setProject(issue.getProject());
        task.setUser(user);
        task.setStatus(TaskStatus.TO_DO);

        // Set due date if provided
        if (conversionData.containsKey("dueDate")) {
            LocalDate dueDate = LocalDate.parse(conversionData.get("dueDate").toString());
            task.setDueDate(dueDate);
        }

        // Set priority based on severity or provided priority
        if (conversionData.containsKey("priority")) {
            Priority priority = Priority.valueOf(conversionData.get("priority").toString());
            task.setPriority(priority);
        } else {
            // Convert severity to priority
            Priority priority = switch (issue.getSeverity()) {
                case LOW -> Priority.LOW;
                case MEDIUM -> Priority.MEDIUM;
                case HIGH -> Priority.HIGH;
            };
            task.setPriority(priority);
        }

        Task savedTask = taskRepository.save(task);

        // Mark issue as resolved
        issue.setStatus(IssueStatus.RESOLVED);
        issueRepository.save(issue);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Converted issue '" + issue.getTitle() + "' to task");
        activityLogRepository.save(log);

        return savedTask;
    }

    @Cacheable(value = "issue-stats", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public Map<String, Object> getIssueStatistics() {
        User user = getCurrentUser();

        Map<String, Object> stats = new HashMap<>();

        // Total issues
        long totalIssues = issueRepository.countByUser(user);
        stats.put("totalIssues", totalIssues);

        // Issues by status
        long openIssues = issueRepository.countByUserAndStatus(user, IssueStatus.OPEN);
        long resolvedIssues = issueRepository.countByUserAndStatus(user, IssueStatus.RESOLVED);

        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("open", openIssues);
        statusStats.put("resolved", resolvedIssues);
        stats.put("byStatus", statusStats);

        // Issues by severity
        Map<String, Long> severityStats = new HashMap<>();
        for (Severity severity : Severity.values()) {
            long count = issueRepository.countByUserAndSeverity(user, severity);
            severityStats.put(severity.name().toLowerCase(), count);
        }
        stats.put("bySeverity", severityStats);

        // Recent issues (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentIssues = issueRepository.countByUserAndCreatedAtAfter(user, weekAgo);
        stats.put("recentIssues", recentIssues);

        return stats;
    }
}
