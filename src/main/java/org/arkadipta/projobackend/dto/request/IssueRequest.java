package org.arkadipta.projobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.arkadipta.projobackend.enums.IssueStatus;
import org.arkadipta.projobackend.enums.Severity;

public class IssueRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Severity is required")
    private Severity severity;

    @NotNull(message = "Status is required")
    private IssueStatus status;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    // Constructors
    public IssueRequest() {
    }

    public IssueRequest(String title, String description, Severity severity, IssueStatus status, Long projectId) {
        this.title = title;
        this.description = description;
        this.severity = severity;
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

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
