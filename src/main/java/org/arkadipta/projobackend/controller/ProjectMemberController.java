package org.arkadipta.projobackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.arkadipta.projobackend.dto.request.ProjectInviteRequest;
import org.arkadipta.projobackend.dto.response.ApiResponse;
import org.arkadipta.projobackend.dto.response.ProjectMemberResponse;
import org.arkadipta.projobackend.enums.InviteStatus;
import org.arkadipta.projobackend.service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaboration")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "http://localhost:5174",
        "http://127.0.0.1:5174" })
@Tag(name = "Collaboration Management", description = "APIs for managing project collaboration and member invitations")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {

    @Autowired
    private ProjectMemberService projectMemberService;

    @PostMapping("/invite")
    @Operation(summary = "Invite user to project", description = "Send an invitation to a user to join a project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to invite users to this project"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or user not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<ProjectMemberResponse> inviteUserToProject(
            @Valid @RequestBody ProjectInviteRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            ProjectMemberResponse response = projectMemberService.inviteUserToProject(request, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/invites/{membershipId}/respond")
    @Operation(summary = "Respond to project invitation", description = "Accept or decline a project invitation")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response recorded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to respond to this invitation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invitation not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<ProjectMemberResponse> respondToInvite(
            @Parameter(description = "Membership ID") @PathVariable Long membershipId,
            @Parameter(description = "Response to invitation") @RequestParam InviteStatus response,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            ProjectMemberResponse memberResponse = projectMemberService.respondToInvite(membershipId, response,
                    username);
            return ResponseEntity.ok(memberResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/projects/{projectId}/members")
    @Operation(summary = "Get project members", description = "Retrieve all members of a project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to view project members"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @Parameter(description = "Project ID") @PathVariable Long projectId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ProjectMemberResponse> members = projectMemberService.getProjectMembers(projectId, username);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-memberships")
    @Operation(summary = "Get user's project memberships", description = "Retrieve all projects the current user is a member of")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Memberships retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<ProjectMemberResponse>> getUserMemberships(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ProjectMemberResponse> memberships = projectMemberService.getUserMemberships(username);
            return ResponseEntity.ok(memberships);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending-invites")
    @Operation(summary = "Get pending invitations", description = "Retrieve all pending project invitations for the current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending invitations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<ProjectMemberResponse>> getPendingInvites(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ProjectMemberResponse> pendingInvites = projectMemberService.getPendingInvites(username);
            return ResponseEntity.ok(pendingInvites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/projects/{projectId}/members/{userId}")
    @Operation(summary = "Remove member from project", description = "Remove a member from a project (project owner only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to remove members from this project"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or member not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<ApiResponse<String>> removeMemberFromProject(
            @Parameter(description = "Project ID") @PathVariable Long projectId,
            @Parameter(description = "User ID") @PathVariable Long userId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            projectMemberService.removeMemberFromProject(projectId, userId, username);
            return ResponseEntity.ok(ApiResponse.success("Member removed from project successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/projects/{projectId}/leave")
    @Operation(summary = "Leave project", description = "Leave a project as a member")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Left project successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Not a member of this project"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<ApiResponse<String>> leaveProject(
            @Parameter(description = "Project ID") @PathVariable Long projectId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            projectMemberService.leaveProject(projectId, username);
            return ResponseEntity.ok(ApiResponse.success("Left project successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get collaboration statistics", description = "Get collaboration statistics for the current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Map<String, Object>> getCollaborationStatistics(Authentication authentication) {
        try {
            String username = authentication.getName();
            Map<String, Object> statistics = projectMemberService.getCollaborationStatistics(username);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
