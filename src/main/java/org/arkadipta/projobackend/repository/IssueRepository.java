package org.arkadipta.projobackend.repository;

import org.arkadipta.projobackend.entity.Issue;
import org.arkadipta.projobackend.entity.Project;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.enums.IssueStatus;
import org.arkadipta.projobackend.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    Page<Issue> findByUser(User user, Pageable pageable);

    Page<Issue> findByProject(Project project, Pageable pageable);

    Page<Issue> findByUserAndProject(User user, Project project, Pageable pageable);

    Page<Issue> findByUserAndStatus(User user, IssueStatus status, Pageable pageable);

    Page<Issue> findByUserAndSeverity(User user, Severity severity, Pageable pageable);

    Page<Issue> findByProjectAndStatus(Project project, IssueStatus status, Pageable pageable);

    List<Issue> findByUser(User user);

    List<Issue> findByProject(Project project);

    List<Issue> findByUserAndStatus(User user, IssueStatus status);

    Optional<Issue> findByIdAndUser(Long id, User user);

    long countByUserAndStatus(User user, IssueStatus status);

    long countByProject(Project project);

    long countByUser(User user);

    long countByUserAndSeverity(User user, Severity severity);

    long countByUserAndCreatedAtAfter(User user, LocalDateTime date);
}
