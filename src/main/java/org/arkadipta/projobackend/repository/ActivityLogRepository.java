package org.arkadipta.projobackend.repository;

import org.arkadipta.projobackend.entity.ActivityLog;
import org.arkadipta.projobackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByUser(User user, Pageable pageable);

    List<ActivityLog> findByUserOrderByTimestampDesc(User user);

    @Query("SELECT a FROM ActivityLog a WHERE a.user = :user ORDER BY a.timestamp DESC")
    List<ActivityLog> findRecentActivitiesByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT a FROM ActivityLog a WHERE a.user = :user AND a.timestamp BETWEEN :startDate AND :endDate")
    Page<ActivityLog> findByUserAndTimestampBetween(@Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
