package org.arkadipta.projobackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_time_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTimeTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration; // in seconds

    public TaskTimeTracking(Task task, LocalDateTime startTime) {
        this.task = task;
        this.startTime = startTime;
    }

    public TaskTimeTracking(Task task, LocalDateTime startTime, LocalDateTime endTime, Integer duration) {
        this.task = task;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }
}
