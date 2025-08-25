package org.arkadipta.projobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.arkadipta.projobackend.enums.Priority;
import org.arkadipta.projobackend.enums.TaskStatus;

import java.time.LocalDate;

public class TaskRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate dueDate;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    // Constructors
    public TaskRequest() {
    }

    public TaskRequest(String title, String description, LocalDate startDate, LocalDate dueDate,
            Priority priority, TaskStatus status, Long projectId) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.projectId = projectId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
