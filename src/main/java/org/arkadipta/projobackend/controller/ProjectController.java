package org.arkadipta.projobackend.controller;

import jakarta.validation.Valid;
import org.arkadipta.projobackend.dto.request.ProjectRequest;
import org.arkadipta.projobackend.dto.response.ApiResponse;
import org.arkadipta.projobackend.entity.Project;
import org.arkadipta.projobackend.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "http://localhost:5174",
        "http://127.0.0.1:5174" })
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Project> projectPage = projectService.getAllProjects(pageable);

            List<Map<String, Object>> projectList = projectPage.getContent().stream().map(project -> {
                Map<String, Object> projectMap = new HashMap<>();
                projectMap.put("id", project.getId());
                projectMap.put("title", project.getTitle());
                projectMap.put("description", project.getDescription());
                projectMap.put("deadline", project.getDeadline());
                projectMap.put("createdAt", project.getCreatedAt());
                projectMap.put("taskCount", projectService.getTaskCount(project.getId()));
                projectMap.put("completedTaskCount", projectService.getCompletedTaskCount(project.getId()));
                return projectMap;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("content", projectList);
            response.put("totalElements", projectPage.getTotalElements());
            response.put("totalPages", projectPage.getTotalPages());
            response.put("number", projectPage.getNumber());
            response.put("size", projectPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProjectById(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", project.getId());
            response.put("title", project.getTitle());
            response.put("description", project.getDescription());
            response.put("deadline", project.getDeadline());
            response.put("createdAt", project.getCreatedAt());
            response.put("tasks", project.getTasks().stream().map(task -> {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("title", task.getTitle());
                taskMap.put("status", task.getStatus());
                taskMap.put("priority", task.getPriority());
                taskMap.put("dueDate", task.getDueDate());
                return taskMap;
            }).toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request) {
        try {
            Project project = projectService.createProject(request);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        try {
            Project project = projectService.updateProject(id, request);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
