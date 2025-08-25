package org.arkadipta.projobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.arkadipta.projobackend.enums.Priority;
import org.arkadipta.projobackend.enums.TaskStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate startDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TO_DO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    // Task Reminder Fields
    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Column(name = "reminder_enabled", nullable = false)
    private Boolean reminderEnabled = true;

    @Column(name = "deadline_reminder_sent", nullable = false)
    private Boolean deadlineReminderSent = false;

    @Column(name = "custom_reminder_sent", nullable = false)
    private Boolean customReminderSent = false;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskTimeTracking> timeTrackings = new ArrayList<>();

    public Task(String title, String description, LocalDate startDate, LocalDate dueDate,
            Priority priority, TaskStatus status, Project project, User user) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.project = project;
        this.user = user;
        this.reminderEnabled = true;
        this.deadlineReminderSent = false;
        this.customReminderSent = false;
        this.isCompleted = false;
    }
}
