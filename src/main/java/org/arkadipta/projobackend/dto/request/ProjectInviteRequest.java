package org.arkadipta.projobackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

public class ProjectInviteRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @Email(message = "Valid email is required")
    @NotNull(message = "Email is required")
    private String email;

    private String message; // Optional invitation message

    // Constructors
    public ProjectInviteRequest() {
    }

    public ProjectInviteRequest(Long projectId, String email) {
        this.projectId = projectId;
        this.email = email;
    }

    public ProjectInviteRequest(Long projectId, String email, String message) {
        this.projectId = projectId;
        this.email = email;
        this.message = message;
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
