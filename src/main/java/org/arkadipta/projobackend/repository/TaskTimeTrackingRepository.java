package org.arkadipta.projobackend.repository;

import org.arkadipta.projobackend.entity.Task;
import org.arkadipta.projobackend.entity.TaskTimeTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskTimeTrackingRepository extends JpaRepository<TaskTimeTracking, Long> {
    List<TaskTimeTracking> findByTask(Task task);

    List<TaskTimeTracking> findByTaskOrderByStartTimeDesc(Task task);

    @Query("SELECT t FROM TaskTimeTracking t WHERE t.task = :task AND t.endTime IS NULL")
    Optional<TaskTimeTracking> findActiveTimerByTask(@Param("task") Task task);

    @Query("SELECT SUM(t.duration) FROM TaskTimeTracking t WHERE t.task = :task AND t.duration IS NOT NULL")
    Long getTotalTimeSpentByTask(@Param("task") Task task);

    void deleteByTask(Task task);
}
