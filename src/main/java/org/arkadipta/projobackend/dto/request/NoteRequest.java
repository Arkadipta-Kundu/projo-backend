package org.arkadipta.projobackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class NoteRequest {
    @NotBlank(message = "Content is required")
    private String content;

    private Long projectId; // Made optional for personal notes

    private Boolean isPrivate; // If true, isPublic will be false

    private Boolean isCollaborative = false;

    // Constructors
    public NoteRequest() {
    }

    public NoteRequest(String content, Long projectId) {
        this.content = content;
        this.projectId = projectId;
    }

    public NoteRequest(String content, Long projectId, Boolean isPrivate, Boolean isCollaborative) {
        this.content = content;
        this.projectId = projectId;
        this.isPrivate = isPrivate;
        this.isCollaborative = isCollaborative;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Boolean getIsCollaborative() {
        return isCollaborative;
    }

    public void setIsCollaborative(Boolean isCollaborative) {
        this.isCollaborative = isCollaborative;
    }
}
