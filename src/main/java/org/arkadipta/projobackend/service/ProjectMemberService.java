package org.arkadipta.projobackend.service;

import org.arkadipta.projobackend.dto.request.ProjectInviteRequest;
import org.arkadipta.projobackend.dto.response.ProjectMemberResponse;
import org.arkadipta.projobackend.entity.Project;
import org.arkadipta.projobackend.entity.ProjectMember;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.enums.InviteStatus;
import org.arkadipta.projobackend.repository.ProjectMemberRepository;
import org.arkadipta.projobackend.repository.ProjectRepository;
import org.arkadipta.projobackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectMemberService {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Caching(evict = {
            @CacheEvict(value = "projectMembers", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "userStats", allEntries = true)
    })
    public ProjectMemberResponse inviteUserToProject(ProjectInviteRequest request, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check if current user is project owner
        if (!project.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only project owner can invite members");
        }

        // Find user to invite by email
        User userToInvite = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with email " + request.getEmail() + " not found"));

        // Check if user is already a member
        Optional<ProjectMember> existingMember = projectMemberRepository
                .findByProjectIdAndUserId(project.getId(), userToInvite.getId());

        if (existingMember.isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }

        // Create new project member
        ProjectMember projectMember = new ProjectMember(project, userToInvite, InviteStatus.PENDING);
        projectMember = projectMemberRepository.save(projectMember);

        return convertToResponse(projectMember);
    }

    @Caching(evict = {
            @CacheEvict(value = "projectMembers", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "userStats", allEntries = true)
    })
    public ProjectMemberResponse respondToInvite(Long membershipId, InviteStatus response, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProjectMember projectMember = projectMemberRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        // Check if current user is the invited user
        if (!projectMember.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only respond to your own invitations");
        }

        // Check if invite is still pending
        if (projectMember.getInviteStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("This invitation has already been responded to");
        }

        projectMember.setInviteStatus(response);
        projectMember = projectMemberRepository.save(projectMember);

        return convertToResponse(projectMember);
    }

    @Cacheable(value = "projectMembers", key = "#projectId + '_members'")
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check if user has access to view project members
        if (!hasProjectAccess(projectId, currentUser.getId()) &&
                !project.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No access to view project members");
        }

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "projectMembers", key = "#username + '_memberships'")
    public List<ProjectMemberResponse> getUserMemberships(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId());
        return memberships.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "projectMembers", key = "#username + '_pending_invites'")
    public List<ProjectMemberResponse> getPendingInvites(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ProjectMember> pendingInvites = projectMemberRepository.findPendingInvitesForUser(user.getId());
        return pendingInvites.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "projectMembers", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "userStats", allEntries = true)
    })
    public void removeMemberFromProject(Long projectId, Long userId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check if current user is project owner
        if (!project.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only project owner can remove members");
        }

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));

        projectMemberRepository.delete(projectMember);
    }

    @Caching(evict = {
            @CacheEvict(value = "projectMembers", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "userStats", allEntries = true)
    })
    public void leaveProject(Long projectId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this project"));

        projectMemberRepository.delete(projectMember);
    }

    public boolean hasProjectAccess(Long projectId, Long userId) {
        return projectMemberRepository.hasProjectAccess(projectId, userId);
    }

    public boolean isProjectOwner(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        return project != null && project.getUser().getId().equals(userId);
    }

    @Cacheable(value = "collaborationStats", key = "#username")
    public Map<String, Object> getCollaborationStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long ownedProjects = projectRepository.countByUserId(user.getId());
        long memberOfProjects = projectMemberRepository.countByUserIdAndInviteStatus(user.getId(),
                InviteStatus.ACCEPTED);
        long pendingInvites = projectMemberRepository.countByUserIdAndInviteStatus(user.getId(), InviteStatus.PENDING);

        return Map.of(
                "ownedProjects", ownedProjects,
                "memberOfProjects", memberOfProjects,
                "pendingInvites", pendingInvites,
                "totalCollaborations", memberOfProjects);
    }

    private ProjectMemberResponse convertToResponse(ProjectMember projectMember) {
        return new ProjectMemberResponse(
                projectMember.getId(),
                projectMember.getProject().getId(),
                projectMember.getProject().getTitle(),
                projectMember.getUser().getId(),
                projectMember.getUser().getUsername(),
                projectMember.getUser().getEmail(),
                projectMember.getInviteStatus(),
                projectMember.getCreatedAt());
    }
}
