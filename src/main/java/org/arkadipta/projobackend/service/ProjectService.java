package org.arkadipta.projobackend.service;

import org.arkadipta.projobackend.dto.request.ProjectRequest;
import org.arkadipta.projobackend.entity.ActivityLog;
import org.arkadipta.projobackend.entity.Project;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.repository.ActivityLogRepository;
import org.arkadipta.projobackend.repository.ProjectRepository;
import org.arkadipta.projobackend.repository.TaskRepository;
import org.arkadipta.projobackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {

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

    @Cacheable(value = "projects", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Project> getAllProjects(Pageable pageable) {
        User user = getCurrentUser();
        return projectRepository.findByUser(user, pageable);
    }

    @Cacheable(value = "projects", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_all'")
    public List<Project> getAllProjects() {
        User user = getCurrentUser();
        return projectRepository.findByUser(user);
    }

    @Cacheable(value = "project", key = "#id")
    public Project getProjectById(Long id) {
        User user = getCurrentUser();
        return projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    @CacheEvict(value = { "projects", "project" }, allEntries = true)
    public Project createProject(ProjectRequest request) {
        User user = getCurrentUser();

        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setDeadline(request.getDeadline());
        project.setUser(user);

        Project savedProject = projectRepository.save(project);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Created project '" + project.getTitle() + "'");
        activityLogRepository.save(log);

        return savedProject;
    }

    @CacheEvict(value = { "projects", "project" }, key = "#id")
    public Project updateProject(Long id, ProjectRequest request) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setDeadline(request.getDeadline());

        Project updatedProject = projectRepository.save(project);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Updated project '" + project.getTitle() + "'");
        activityLogRepository.save(log);

        return updatedProject;
    }

    @CacheEvict(value = { "projects", "project" }, key = "#id")
    public void deleteProject(Long id) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        String projectTitle = project.getTitle();
        projectRepository.delete(project);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Deleted project '" + projectTitle + "'");
        activityLogRepository.save(log);
    }

    @Cacheable(value = "project_task_count", key = "#projectId")
    public long getTaskCount(Long projectId) {
        Project project = getProjectById(projectId);
        return taskRepository.countByProject(project);
    }

    @Cacheable(value = "project_completed_task_count", key = "#projectId")
    public long getCompletedTaskCount(Long projectId) {
        Project project = getProjectById(projectId);
        return taskRepository.countByProjectAndStatus(project, org.arkadipta.projobackend.enums.TaskStatus.DONE);
    }
}
