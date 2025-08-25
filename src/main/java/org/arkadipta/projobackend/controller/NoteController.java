package org.arkadipta.projobackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.arkadipta.projobackend.dto.request.NoteRequest;
import org.arkadipta.projobackend.entity.Note;
import org.arkadipta.projobackend.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "http://localhost:5174",
        "http://127.0.0.1:5174" })
@Tag(name = "Note Management", description = "APIs for managing notes with privacy and collaboration features")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    @Operation(summary = "Get all accessible notes", description = "Retrieve all notes accessible to the current user with pagination, filtering, and sorting options")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<Note>> getAllNotes(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by project ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "Search query") @RequestParam(required = false) String search,
            Authentication authentication) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Note> notePage = noteService.getAllNotes(pageable, projectId, search);

            return ResponseEntity.ok(notePage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get note by ID", description = "Retrieve a specific note by ID if accessible to the current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Note> getNoteById(
            @Parameter(description = "Note ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Note note = noteService.getNoteById(id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new note", description = "Create a new note with privacy and collaboration settings")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Note> createNote(
            @Valid @RequestBody NoteRequest request,
            Authentication authentication) {
        try {
            Note note = noteService.createNote(request);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update note", description = "Update an existing note if the user has edit permissions")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No permission to edit this note"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Note> updateNote(
            @Parameter(description = "Note ID") @PathVariable Long id,
            @Valid @RequestBody NoteRequest request,
            Authentication authentication) {
        try {
            Note note = noteService.updateNote(id, request);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete note", description = "Delete a note if the user has delete permissions")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No permission to delete this note"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<org.arkadipta.projobackend.dto.response.ApiResponse<String>> deleteNote(
            @Parameter(description = "Note ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            noteService.deleteNote(id);
            return ResponseEntity
                    .ok(org.arkadipta.projobackend.dto.response.ApiResponse.success("Note deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(org.arkadipta.projobackend.dto.response.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get note statistics", description = "Get statistics about notes for the current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Map<String, Object>> getNoteStatistics(Authentication authentication) {
        try {
            Map<String, Object> statistics = noteService.getNoteStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
