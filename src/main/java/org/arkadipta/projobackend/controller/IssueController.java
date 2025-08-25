package org.arkadipta.projobackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.arkadipta.projobackend.dto.request.IssueRequest;
import org.arkadipta.projobackend.dto.response.ApiResponse;
import org.arkadipta.projobackend.entity.Issue;
import org.arkadipta.projobackend.entity.Task;
import org.arkadipta.projobackend.enums.IssueStatus;
import org.arkadipta.projobackend.enums.Severity;
import org.arkadipta.projobackend.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "http://localhost:5174",
        "http://127.0.0.1:5174" })
@Tag(name = "Issues", description = "Issue management APIs for tracking and resolving project issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Operation(summary = "Get all issues", description = "Retrieve all issues with filtering and pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Issues retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Severity severity) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Issue> issuePage = issueService.getAllIssues(pageable, projectId, status, severity);

            List<Map<String, Object>> issueList = issuePage.getContent().stream().map(issue -> {
                Map<String, Object> issueMap = new HashMap<>();
                issueMap.put("id", issue.getId());
                issueMap.put("title", issue.getTitle());
                issueMap.put("description", issue.getDescription());
                issueMap.put("severity", issue.getSeverity());
                issueMap.put("status", issue.getStatus());
                issueMap.put("projectId", issue.getProject().getId());
                issueMap.put("projectTitle", issue.getProject().getTitle());
                issueMap.put("createdAt", issue.getCreatedAt());
                return issueMap;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("content", issueList);
            response.put("totalElements", issuePage.getTotalElements());
            response.put("totalPages", issuePage.getTotalPages());
            response.put("number", issuePage.getNumber());
            response.put("size", issuePage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get issue by ID", description = "Retrieve a specific issue by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable Long id) {
        try {
            Issue issue = issueService.getIssueById(id);
            return ResponseEntity.ok(issue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Create new issue", description = "Create a new issue in a project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Issue created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<Issue> createIssue(@Valid @RequestBody IssueRequest request) {
        try {
            Issue issue = issueService.createIssue(request);
            return ResponseEntity.ok(issue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get issue statistics", description = "Retrieve statistics about issues including counts by status and severity")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getIssueStatistics() {
        try {
            Map<String, Object> stats = issueService.getIssueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update issue", description = "Update an existing issue")
    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long id, @Valid @RequestBody IssueRequest request) {
        try {
            Issue issue = issueService.updateIssue(id, request);
            return ResponseEntity.ok(issue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/convert-to-task")
    public ResponseEntity<Map<String, Object>> convertIssueToTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> conversionData) {
        try {
            Task task = issueService.convertIssueToTask(id, conversionData);

            Map<String, Object> response = new HashMap<>();
            response.put("taskId", task.getId());
            response.put("message", "Issue converted to task successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteIssue(@PathVariable Long id) {
        try {
            issueService.deleteIssue(id);
            return ResponseEntity.ok(ApiResponse.success("Issue deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
