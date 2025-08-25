package org.arkadipta.projobackend.repository;

import org.arkadipta.projobackend.entity.Project;
import org.arkadipta.projobackend.entity.Task;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.enums.Priority;
import org.arkadipta.projobackend.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
        Page<Task> findByUser(User user, Pageable pageable);

        Page<Task> findByProject(Project project, Pageable pageable);

        Page<Task> findByUserAndProject(User user, Project project, Pageable pageable);

        Page<Task> findByUserAndStatus(User user, TaskStatus status, Pageable pageable);

        Page<Task> findByUserAndPriority(User user, Priority priority, Pageable pageable);

        Page<Task> findByProjectAndStatus(Project project, TaskStatus status, Pageable pageable);

        List<Task> findByUser(User user);

        List<Task> findByProject(Project project);

        List<Task> findByUserAndProject(User user, Project project);

        List<Task> findByUserAndStatus(User user, TaskStatus status);

        Optional<Task> findByIdAndUser(Long id, User user);

        @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate BETWEEN :startDate AND :endDate")
        List<Task> findByUserAndDueDateBetween(@Param("user") User user,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate < :today AND t.status != :completedStatus")
        List<Task> findOverdueTasksByUser(@Param("user") User user,
                        @Param("today") LocalDate today,
                        @Param("completedStatus") TaskStatus completedStatus);

        long countByUserAndStatus(User user, TaskStatus status);

        long countByProject(Project project);

        long countByProjectAndStatus(Project project, TaskStatus status);

        // Task Reminder Queries
        @Query("SELECT t FROM Task t WHERE t.reminderTime <= :now AND t.customReminderSent = false AND t.reminderEnabled = true AND t.isCompleted = false")
        List<Task> findTasksForCustomReminder(@Param("now") LocalDateTime now);

        @Query("SELECT t FROM Task t WHERE t.dueDate IN (:today, :tomorrow) AND t.deadlineReminderSent = false AND t.reminderEnabled = true AND t.isCompleted = false")
        List<Task> findTasksForDeadlineReminder(@Param("today") LocalDate today, @Param("tomorrow") LocalDate tomorrow);

        @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status != 'COMPLETED' AND t.status != 'OVERDUE' AND t.isCompleted = false")
        List<Task> findOverdueTasks(@Param("today") LocalDate today);
}
