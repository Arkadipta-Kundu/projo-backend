package org.arkadipta.projobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.arkadipta.projobackend.enums.InviteStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status", nullable = false)
    private InviteStatus inviteStatus = InviteStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProjectMember(Project project, User user) {
        this.project = project;
        this.user = user;
        this.inviteStatus = InviteStatus.PENDING;
    }

    public ProjectMember(Project project, User user, InviteStatus inviteStatus) {
        this.project = project;
        this.user = user;
        this.inviteStatus = inviteStatus;
    }
}
