package org.arkadipta.projobackend.repository;

import org.arkadipta.projobackend.entity.Note;
import org.arkadipta.projobackend.entity.Project;
import org.arkadipta.projobackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
        Page<Note> findByUser(User user, Pageable pageable);

        Page<Note> findByProject(Project project, Pageable pageable);

        Page<Note> findByUserAndProject(User user, Project project, Pageable pageable);

        List<Note> findByUser(User user);

        List<Note> findByProject(Project project);

        Optional<Note> findByIdAndUser(Long id, User user);

        @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
                        "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<Note> findByUserAndContentContaining(@Param("user") User user,
                        @Param("search") String search,
                        Pageable pageable);

        // Enhanced queries for privacy and collaboration features
        @Query("""
                        SELECT DISTINCT n FROM Note n
                        LEFT JOIN n.project p
                        LEFT JOIN ProjectMember pm ON pm.project = p AND pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED'
                        WHERE (n.user.id = :userId)
                           OR (n.isPublic = true AND p IS NOT NULL AND p.user.id = :userId)
                           OR (n.isPublic = true AND p IS NOT NULL AND pm.id IS NOT NULL)
                        ORDER BY n.createdAt DESC
                        """)
        List<Note> findAllAccessibleNotes(@Param("userId") Long userId);

        @Query("""
                        SELECT DISTINCT n FROM Note n
                        LEFT JOIN n.project p
                        LEFT JOIN ProjectMember pm ON pm.project = p AND pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED'
                        WHERE n.project.id = :projectId
                           AND ((n.user.id = :userId)
                                OR (p.user.id = :userId AND n.isPublic = true)
                                OR (pm.id IS NOT NULL AND n.isPublic = true))
                        ORDER BY n.createdAt DESC
                        """)
        List<Note> findNotesByProject(@Param("projectId") Long projectId, @Param("userId") Long userId);

        @Query("""
                        SELECT n FROM Note n
                        LEFT JOIN n.project p
                        LEFT JOIN ProjectMember pm ON pm.project = p AND pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED'
                        WHERE n.id = :noteId
                           AND ((n.user.id = :userId)
                                OR (n.isCollaborative = true AND p IS NOT NULL AND p.user.id = :userId)
                                OR (n.isCollaborative = true AND p IS NOT NULL AND pm.id IS NOT NULL))
                        """)
        Optional<Note> findEditableNote(@Param("noteId") Long noteId, @Param("userId") Long userId);

        // Privacy-based queries
        List<Note> findByUserAndIsPublic(User user, Boolean isPublic);

        List<Note> findByUserAndIsCollaborative(User user, Boolean isCollaborative);

        @Query("SELECT COUNT(n) FROM Note n WHERE n.user = :user AND n.isPublic = :isPublic")
        long countByUserAndIsPublic(@Param("user") User user, @Param("isPublic") Boolean isPublic);

        long countByUser(User user);

        long countByUserAndIsCollaborative(User user, Boolean isCollaborative);
}
