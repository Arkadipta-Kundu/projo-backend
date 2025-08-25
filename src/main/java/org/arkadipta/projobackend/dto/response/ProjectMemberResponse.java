package org.arkadipta.projobackend.dto.response;

import org.arkadipta.projobackend.enums.InviteStatus;

import java.time.LocalDateTime;

public class ProjectMemberResponse {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long userId;
    private String username;
    private String email;
    private InviteStatus inviteStatus;
    private LocalDateTime createdAt;

    // Constructors
    public ProjectMemberResponse() {
    }

    public ProjectMemberResponse(Long id, Long projectId, String projectTitle, Long userId,
            String username, String email, InviteStatus inviteStatus, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.inviteStatus = inviteStatus;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public InviteStatus getInviteStatus() {
        return inviteStatus;
    }

    public void setInviteStatus(InviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
