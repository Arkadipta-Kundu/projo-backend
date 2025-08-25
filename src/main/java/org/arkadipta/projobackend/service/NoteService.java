package org.arkadipta.projobackend.service;

import org.arkadipta.projobackend.dto.request.NoteRequest;
import org.arkadipta.projobackend.entity.ActivityLog;
import org.arkadipta.projobackend.entity.Note;
import org.arkadipta.projobackend.entity.Project;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.repository.ActivityLogRepository;
import org.arkadipta.projobackend.repository.NoteRepository;
import org.arkadipta.projobackend.repository.ProjectRepository;
import org.arkadipta.projobackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Cacheable(value = "notes", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_accessible'")
    public List<Note> getAllAccessibleNotes() {
        User user = getCurrentUser();
        return noteRepository.findAllAccessibleNotes(user.getId());
    }

    @Cacheable(value = "notes", key = "#projectId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public List<Note> getNotesByProject(Long projectId) {
        User user = getCurrentUser();
        return noteRepository.findNotesByProject(projectId, user.getId());
    }

    public Page<Note> getAllNotes(Pageable pageable, Long projectId, String search) {
        User user = getCurrentUser();

        if (projectId != null) {
            Project project = projectRepository.findByIdAndUser(projectId, user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            return noteRepository.findByProject(project, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            return noteRepository.findByUserAndContentContaining(user, search, pageable);
        } else {
            return noteRepository.findByUser(user, pageable);
        }
    }

    @Cacheable(value = "note", key = "#id")
    public Note getNoteById(Long id) {
        User user = getCurrentUser();
        return noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));
    }

    @Caching(evict = {
            @CacheEvict(value = "notes", allEntries = true),
            @CacheEvict(value = "note", key = "#result.id")
    })
    public Note createNote(NoteRequest request) {
        User user = getCurrentUser();
        Project project = null;

        if (request.getProjectId() != null) {
            project = projectRepository.findByIdAndUser(request.getProjectId(), user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
        }

        Note note = new Note();
        note.setContent(request.getContent());
        note.setProject(project);
        note.setUser(user);

        // Set privacy and collaboration settings if provided
        if (request.getIsPrivate() != null) {
            note.setIsPublic(!request.getIsPrivate());
        }
        if (request.getIsCollaborative() != null) {
            note.setIsCollaborative(request.getIsCollaborative());
        }

        Note savedNote = noteRepository.save(note);

        // Log activity
        String projectName = project != null ? project.getTitle() : "Personal Notes";
        ActivityLog log = new ActivityLog(user, "Created note in " + projectName);
        activityLogRepository.save(log);

        return savedNote;
    }

    @Caching(evict = {
            @CacheEvict(value = "notes", allEntries = true),
            @CacheEvict(value = "note", key = "#id")
    })
    public Note updateNote(Long id, NoteRequest request) {
        User user = getCurrentUser();
        Note note = noteRepository.findEditableNote(id, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Cannot edit this note"));

        note.setContent(request.getContent());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndUser(request.getProjectId(), user)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            note.setProject(project);
        }

        // Only note owner can change privacy/collaboration settings
        if (note.getUser().getId().equals(user.getId())) {
            if (request.getIsPrivate() != null) {
                note.setIsPublic(!request.getIsPrivate());
            }
            if (request.getIsCollaborative() != null) {
                note.setIsCollaborative(request.getIsCollaborative());
            }
        }

        Note updatedNote = noteRepository.save(note);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Updated note");
        activityLogRepository.save(log);

        return updatedNote;
    }

    @Caching(evict = {
            @CacheEvict(value = "notes", allEntries = true),
            @CacheEvict(value = "note", key = "#id")
    })
    public void deleteNote(Long id) {
        User user = getCurrentUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));

        // Check if user can delete: owner or admin
        if (!note.getUser().getId().equals(user.getId()) && !"admin".equals(user.getRole())) {
            throw new AccessDeniedException("Cannot delete this note");
        }

        noteRepository.delete(note);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Deleted note");
        activityLogRepository.save(log);
    }

    @Cacheable(value = "note-stats", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public Map<String, Object> getNoteStatistics() {
        User user = getCurrentUser();

        Map<String, Object> stats = new HashMap<>();

        // Total notes
        long totalNotes = noteRepository.countByUser(user);
        stats.put("totalNotes", totalNotes);

        // Notes by privacy
        long publicNotes = noteRepository.countByUserAndIsPublic(user, true);
        long privateNotes = noteRepository.countByUserAndIsPublic(user, false);

        Map<String, Long> privacyStats = new HashMap<>();
        privacyStats.put("public", publicNotes);
        privacyStats.put("private", privateNotes);
        stats.put("byPrivacy", privacyStats);

        // Collaborative notes
        long collaborativeNotes = noteRepository.countByUserAndIsCollaborative(user, true);
        stats.put("collaborativeNotes", collaborativeNotes);

        return stats;
    }
}
