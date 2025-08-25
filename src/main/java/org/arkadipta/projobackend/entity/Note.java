package org.arkadipta.projobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "is_collaborative", nullable = false)
    private Boolean isCollaborative = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Note(String content, Project project, User user) {
        this.content = content;
        this.project = project;
        this.user = user;
        this.isPublic = true;
        this.isCollaborative = false;
    }

    public Note(String content, Project project, User user, Boolean isPublic, Boolean isCollaborative) {
        this.content = content;
        this.project = project;
        this.user = user;
        this.isPublic = isPublic;
        this.isCollaborative = isCollaborative;
    }
}
