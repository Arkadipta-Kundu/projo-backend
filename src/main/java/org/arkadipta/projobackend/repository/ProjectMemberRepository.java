package org.arkadipta.projobackend.repository;

import org.arkadipta.projobackend.entity.ProjectMember;
import org.arkadipta.projobackend.enums.InviteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // Find all members of a project
    List<ProjectMember> findByProjectId(Long projectId);

    // Find all members of a project with specific invite status
    List<ProjectMember> findByProjectIdAndInviteStatus(Long projectId, InviteStatus inviteStatus);

    // Find all projects a user is a member of
    List<ProjectMember> findByUserId(Long userId);

    // Find all projects a user is a member of with specific invite status
    List<ProjectMember> findByUserIdAndInviteStatus(Long userId, InviteStatus inviteStatus);

    // Check if user is a member of a project
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    // Check if user is an accepted member of a project
    Optional<ProjectMember> findByProjectIdAndUserIdAndInviteStatus(Long projectId, Long userId,
            InviteStatus inviteStatus);

    // Count members in a project
    long countByProjectId(Long projectId);

    // Count accepted members in a project
    long countByProjectIdAndInviteStatus(Long projectId, InviteStatus inviteStatus);

    // Count projects a user is a member of
    long countByUserId(Long userId);

    // Count projects a user is an accepted member of
    long countByUserIdAndInviteStatus(Long userId, InviteStatus inviteStatus);

    // Find pending invites for a user
    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.project WHERE pm.user.id = :userId AND pm.inviteStatus = 'PENDING'")
    List<ProjectMember> findPendingInvitesForUser(@Param("userId") Long userId);

    // Find pending invites for a project
    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user WHERE pm.project.id = :projectId AND pm.inviteStatus = 'PENDING'")
    List<ProjectMember> findPendingInvitesForProject(@Param("projectId") Long projectId);

    // Find all collaboration data for user statistics
    @Query("""
            SELECT new map(
                COUNT(DISTINCT pm.project.id) as totalProjects,
                COUNT(DISTINCT CASE WHEN pm.inviteStatus = 'ACCEPTED' THEN pm.project.id END) as acceptedProjects,
                COUNT(DISTINCT CASE WHEN pm.inviteStatus = 'PENDING' THEN pm.project.id END) as pendingInvites
            )
            FROM ProjectMember pm
            WHERE pm.user.id = :userId
            """)
    List<Object> getCollaborationStatistics(@Param("userId") Long userId);

    // Check if user has access to project
    @Query("""
            SELECT COUNT(pm) > 0
            FROM ProjectMember pm
            WHERE pm.project.id = :projectId
            AND pm.user.id = :userId
            AND pm.inviteStatus = 'ACCEPTED'
            """)
    boolean hasProjectAccess(@Param("projectId") Long projectId, @Param("userId") Long userId);

    // Get user's accepted project memberships with pagination
    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.project WHERE pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED'")
    Page<ProjectMember> findAcceptedMembershipsByUser(@Param("userId") Long userId, Pageable pageable);
}
