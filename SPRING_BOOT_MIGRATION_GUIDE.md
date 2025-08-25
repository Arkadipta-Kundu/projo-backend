# Spring Boot Migration Guide: Notes & Issues APIs with Collaboration

## Overview

This document provides a comprehensive guide for migrating the existing PHP-based Notes and Issues APIs to Spring Boot. The current system includes collaborative features, private/public notes, project-based organization, and role-based access control.

## Current System Analysis

### Database Schema

#### Notes Table

```sql
CREATE TABLE notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    project_id INT DEFAULT NULL,
    user_id INT NOT NULL,
    is_public TINYINT(1) DEFAULT 1,
    is_collaborative TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### Issues Table

```sql
CREATE TABLE issues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity ENUM('Low', 'Medium', 'High') NOT NULL,
    status ENUM('Open', 'Resolved') DEFAULT 'Open',
    project_id INT DEFAULT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### Supporting Tables

```sql
-- Project members for collaboration
CREATE TABLE project_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    invite_status ENUM('pending', 'accepted') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member (project_id, user_id)
);

-- Projects table
CREATE TABLE projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Spring Boot Implementation

### 1. Project Setup

#### Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>

    <!-- JWT for authentication -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
    </dependency>
</dependencies>
```

### 2. Entity Classes

#### User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Note> notes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Issue> issues = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Project> ownedProjects = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProjectMember> projectMemberships = new HashSet<>();

    // constructors, getters, setters
}

public enum Role {
    USER, ADMIN
}
```

#### Project Entity

```java
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Note> notes = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Issue> issues = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProjectMember> members = new HashSet<>();

    // constructors, getters, setters
}
```

#### ProjectMember Entity

```java
@Entity
@Table(name = "project_members")
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status", nullable = false)
    private InviteStatus inviteStatus = InviteStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // constructors, getters, setters
}

public enum InviteStatus {
    PENDING, ACCEPTED
}
```

#### Note Entity

```java
@Entity
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "is_collaborative", nullable = false)
    private Boolean isCollaborative = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // constructors, getters, setters
}
```

#### Issue Entity

```java
@Entity
@Table(name = "issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // constructors, getters, setters
}

public enum Severity {
    LOW, MEDIUM, HIGH
}

public enum Status {
    OPEN, RESOLVED
}
```

### 3. Repository Interfaces

#### NoteRepository

```java
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("""
        SELECT DISTINCT n FROM Note n
        LEFT JOIN n.project p
        LEFT JOIN p.members pm
        WHERE n.user.id = :userId
           OR (p.user.id = :userId AND (n.isPublic = true OR n.isPublic IS NULL))
           OR (pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED' AND (n.isPublic = true OR n.isPublic IS NULL))
        ORDER BY n.id DESC
        """)
    List<Note> findAllAccessibleNotes(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT n FROM Note n
        LEFT JOIN n.project p
        LEFT JOIN p.members pm
        WHERE n.project.id = :projectId
           AND (n.user.id = :userId
                OR p.user.id = :userId
                OR (pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED'))
           AND (n.isPublic = true OR n.isPublic IS NULL)
        ORDER BY n.id DESC
        """)
    List<Note> findNotesByProject(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Query("""
        SELECT n FROM Note n
        LEFT JOIN n.project p
        WHERE n.id = :noteId
           AND (n.user.id = :userId
                OR p.user.id = :userId
                OR (n.isCollaborative = true AND n.project IS NOT NULL
                    AND EXISTS (SELECT 1 FROM ProjectMember pm WHERE pm.project.id = p.id
                               AND pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED')))
        """)
    Optional<Note> findEditableNote(@Param("noteId") Long noteId, @Param("userId") Long userId);
}
```

#### IssueRepository

```java
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    @Query("""
        SELECT DISTINCT i FROM Issue i
        LEFT JOIN i.project p
        LEFT JOIN p.members pm
        WHERE i.user.id = :userId
           OR p.user.id = :userId
           OR (pm.user.id = :userId AND pm.inviteStatus = 'ACCEPTED')
        ORDER BY i.id DESC
        """)
    List<Issue> findAllAccessibleIssues(@Param("userId") Long userId);

    List<Issue> findAllByOrderByIdDesc();

    List<Issue> findByProjectIdAndStatusAndSeverityOrderByIdDesc(
        Long projectId, Status status, Severity severity);

    List<Issue> findByProjectIdOrderByIdDesc(Long projectId);

    List<Issue> findByStatusOrderByIdDesc(Status status);

    List<Issue> findBySeverityOrderByIdDesc(Severity severity);
}
```

### 4. Service Classes

#### NoteService

```java
@Service
@Transactional
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ProjectService projectService;

    public List<NoteDTO> getAllNotes(Long userId) {
        List<Note> notes = noteRepository.findAllAccessibleNotes(userId);
        return notes.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByProject(Long projectId, Long userId) {
        List<Note> notes = noteRepository.findNotesByProject(projectId, userId);
        return notes.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }

    public NoteDTO createNote(CreateNoteRequest request, Long userId) {
        User user = userService.findById(userId);
        Project project = null;

        if (request.getProjectId() != null) {
            project = projectService.findById(request.getProjectId());
            // Verify user has access to project
            if (!projectService.hasProjectAccess(project.getId(), userId)) {
                throw new AccessDeniedException("No access to project");
            }
        }

        Note note = new Note();
        note.setContent(request.getContent());
        note.setProject(project);
        note.setUser(user);
        note.setIsPublic(!request.getIsPrivate());
        note.setIsCollaborative(request.getIsCollaborative());

        Note savedNote = noteRepository.save(note);
        return convertToDTO(savedNote);
    }

    public NoteDTO updateNote(Long noteId, UpdateNoteRequest request, Long userId) {
        Note note = noteRepository.findEditableNote(noteId, userId)
            .orElseThrow(() -> new AccessDeniedException("Cannot edit this note"));

        note.setContent(request.getContent());

        if (request.getProjectId() != null) {
            Project project = projectService.findById(request.getProjectId());
            if (!projectService.hasProjectAccess(project.getId(), userId)) {
                throw new AccessDeniedException("No access to project");
            }
            note.setProject(project);
        }

        // Only note owner can change privacy/collaboration settings
        if (note.getUser().getId().equals(userId)) {
            if (request.getIsPrivate() != null) {
                note.setIsPublic(!request.getIsPrivate());
            }
            if (request.getIsCollaborative() != null) {
                note.setIsCollaborative(request.getIsCollaborative());
            }
        }

        Note savedNote = noteRepository.save(note);
        return convertToDTO(savedNote);
    }

    public void deleteNote(Long noteId, Long userId, Role userRole) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("Note not found"));

        boolean canDelete = false;

        // User can delete their own notes
        if (note.getUser().getId().equals(userId)) {
            canDelete = true;
        }
        // Admin can delete any note
        else if (userRole == Role.ADMIN) {
            canDelete = true;
        }
        // Project creator can delete any note in their project
        else if (note.getProject() != null &&
                 note.getProject().getUser().getId().equals(userId)) {
            canDelete = true;
        }

        if (!canDelete) {
            throw new AccessDeniedException("Cannot delete this note");
        }

        noteRepository.delete(note);
    }

    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setContent(note.getContent());
        dto.setIsPublic(note.getIsPublic());
        dto.setIsCollaborative(note.getIsCollaborative());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setCreatedBy(note.getUser().getUsername());

        if (note.getProject() != null) {
            dto.setProjectId(note.getProject().getId());
            dto.setProjectTitle(note.getProject().getTitle());
        }

        return dto;
    }
}
```

#### IssueService

```java
@Service
@Transactional
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    public List<IssueDTO> getAllIssues(Long userId, Role userRole) {
        List<Issue> issues;

        if (userRole == Role.ADMIN) {
            issues = issueRepository.findAllByOrderByIdDesc();
        } else {
            issues = issueRepository.findAllAccessibleIssues(userId);
        }

        return issues.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
    }

    public List<IssueDTO> getFilteredIssues(Long userId, Role userRole,
                                          Long projectId, Status status, Severity severity) {
        List<Issue> allIssues = getAllIssues(userId, userRole)
            .stream()
            .map(this::convertFromDTO)
            .collect(Collectors.toList());

        return allIssues.stream()
            .filter(issue -> projectId == null ||
                           (issue.getProject() != null && issue.getProject().getId().equals(projectId)))
            .filter(issue -> status == null || issue.getStatus().equals(status))
            .filter(issue -> severity == null || issue.getSeverity().equals(severity))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public IssueDTO createIssue(CreateIssueRequest request, Long userId) {
        User user = userService.findById(userId);
        Project project = null;

        if (request.getProjectId() != null) {
            project = projectService.findById(request.getProjectId());
            if (!projectService.hasProjectAccess(project.getId(), userId)) {
                throw new AccessDeniedException("No access to project");
            }
        }

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setSeverity(request.getSeverity());
        issue.setStatus(request.getStatus() != null ? request.getStatus() : Status.OPEN);
        issue.setProject(project);
        issue.setUser(user);

        Issue savedIssue = issueRepository.save(issue);
        return convertToDTO(savedIssue);
    }

    public IssueDTO updateIssue(Long issueId, UpdateIssueRequest request, Long userId, Role userRole) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new EntityNotFoundException("Issue not found"));

        // Check permissions
        boolean canEdit = false;
        if (issue.getUser().getId().equals(userId)) {
            canEdit = true;
        } else if (userRole == Role.ADMIN) {
            canEdit = true;
        } else if (issue.getProject() != null &&
                   issue.getProject().getUser().getId().equals(userId)) {
            canEdit = true;
        }

        if (!canEdit) {
            throw new AccessDeniedException("Cannot edit this issue");
        }

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setSeverity(request.getSeverity());
        issue.setStatus(request.getStatus());

        if (request.getProjectId() != null) {
            Project project = projectService.findById(request.getProjectId());
            issue.setProject(project);
        }

        Issue savedIssue = issueRepository.save(issue);
        return convertToDTO(savedIssue);
    }

    public void deleteIssue(Long issueId, Long userId, Role userRole) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new EntityNotFoundException("Issue not found"));

        boolean canDelete = false;

        // User can delete their own issues
        if (issue.getUser().getId().equals(userId)) {
            canDelete = true;
        }
        // Admin can delete any issue
        else if (userRole == Role.ADMIN) {
            canDelete = true;
        }
        // Project creator can delete any issue in their project
        else if (issue.getProject() != null &&
                 issue.getProject().getUser().getId().equals(userId)) {
            canDelete = true;
        }

        if (!canDelete) {
            throw new AccessDeniedException("Cannot delete this issue");
        }

        issueRepository.delete(issue);
    }

    private IssueDTO convertToDTO(Issue issue) {
        IssueDTO dto = new IssueDTO();
        dto.setId(issue.getId());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setSeverity(issue.getSeverity());
        dto.setStatus(issue.getStatus());
        dto.setCreatedAt(issue.getCreatedAt());
        dto.setUpdatedAt(issue.getUpdatedAt());
        dto.setCreatedBy(issue.getUser().getUsername());

        if (issue.getProject() != null) {
            dto.setProjectId(issue.getProject().getId());
            dto.setProjectTitle(issue.getProject().getTitle());
        }

        return dto;
    }
}
```

### 5. DTO Classes

#### Note DTOs

```java
// NoteDTO.java
public class NoteDTO {
    private Long id;
    private String content;
    private Long projectId;
    private String projectTitle;
    private Boolean isPublic;
    private Boolean isCollaborative;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // constructors, getters, setters
}

// CreateNoteRequest.java
@NotNull
@NotBlank
public class CreateNoteRequest {
    @NotBlank(message = "Content is required")
    private String content;

    private Long projectId;
    private Boolean isPrivate = false;
    private Boolean isCollaborative = false;

    // constructors, getters, setters
}

// UpdateNoteRequest.java
public class UpdateNoteRequest {
    @NotBlank(message = "Content is required")
    private String content;

    private Long projectId;
    private Boolean isPrivate;
    private Boolean isCollaborative;

    // constructors, getters, setters
}
```

#### Issue DTOs

```java
// IssueDTO.java
public class IssueDTO {
    private Long id;
    private String title;
    private String description;
    private Severity severity;
    private Status status;
    private Long projectId;
    private String projectTitle;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // constructors, getters, setters
}

// CreateIssueRequest.java
public class CreateIssueRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Severity is required")
    private Severity severity;

    private Status status;
    private Long projectId;

    // constructors, getters, setters
}

// UpdateIssueRequest.java
public class UpdateIssueRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Severity is required")
    private Severity severity;

    @NotNull(message = "Status is required")
    private Status status;

    private Long projectId;

    // constructors, getters, setters
}
```

### 6. REST Controllers

#### NoteController

```java
@RestController
@RequestMapping("/api/notes")
@PreAuthorize("hasRole('USER')")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteDTO>>> getAllNotes(
            @RequestParam(required = false) Long projectId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        List<NoteDTO> notes;
        if (projectId != null) {
            notes = noteService.getNotesByProject(projectId, userId);
        } else {
            notes = noteService.getAllNotes(userId);
        }

        return ResponseEntity.ok(ApiResponse.success(notes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteDTO>> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        NoteDTO note = noteService.createNote(request, userId);

        return ResponseEntity.ok(ApiResponse.success(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoteDTO>> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        NoteDTO note = noteService.updateNote(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success(note));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        Role userRole = getCurrentUserRole(authentication);

        noteService.deleteNote(id, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Long getCurrentUserId(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    private Role getCurrentUserRole(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getRole();
    }
}
```

#### IssueController

```java
@RestController
@RequestMapping("/api/issues")
@PreAuthorize("hasRole('USER')")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueDTO>>> getAllIssues(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Severity severity,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        Role userRole = getCurrentUserRole(authentication);

        List<IssueDTO> issues;
        if (projectId != null || status != null || severity != null) {
            issues = issueService.getFilteredIssues(userId, userRole, projectId, status, severity);
        } else {
            issues = issueService.getAllIssues(userId, userRole);
        }

        return ResponseEntity.ok(ApiResponse.success(issues));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueDTO>> createIssue(
            @Valid @RequestBody CreateIssueRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        IssueDTO issue = issueService.createIssue(request, userId);

        return ResponseEntity.ok(ApiResponse.success(issue));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueDTO>> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        Role userRole = getCurrentUserRole(authentication);

        IssueDTO issue = issueService.updateIssue(id, request, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(issue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIssue(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        Role userRole = getCurrentUserRole(authentication);

        issueService.deleteIssue(id, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/convert-to-task")
    public ResponseEntity<ApiResponse<TaskDTO>> convertToTask(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        Role userRole = getCurrentUserRole(authentication);

        TaskDTO task = issueService.convertIssueToTask(id, userId, userRole);

        return ResponseEntity.ok(ApiResponse.success(task));
    }

    private Long getCurrentUserId(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    private Role getCurrentUserRole(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRole.getRole();
    }
}
```

### 7. Security Configuration

#### SecurityConfig

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/notes/**").hasRole("USER")
                .requestMatchers("/api/issues/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 8. API Response Wrapper

#### ApiResponse

```java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> error(String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }

    // constructors, getters, setters
}
```

## Key Features Implementation

### 1. Collaboration Features

The collaboration system works through:

1. **Project Membership**: Users can be added to projects as members
2. **Note Visibility**:
   - `is_public = false` → Private to creator only
   - `is_public = true` → Visible to project members
3. **Collaborative Editing**:
   - `is_collaborative = true` → Project members can edit
   - `is_collaborative = false` → Only creator can edit

### 2. Access Control

Permission matrix:

- **Note Creator**: Full access (read, edit, delete)
- **Project Owner**: Full access to all notes in their project
- **Project Members**: Read access to public notes, edit access to collaborative notes
- **Admin**: Full access to all notes and issues

### 3. API Endpoints

#### Notes API

```
GET    /api/notes                    - Get all accessible notes
GET    /api/notes?projectId={id}     - Get notes by project
POST   /api/notes                    - Create new note
PUT    /api/notes/{id}               - Update note
DELETE /api/notes/{id}               - Delete note
```

#### Issues API

```
GET    /api/issues                              - Get all accessible issues
GET    /api/issues?projectId={id}&status={s}    - Get filtered issues
POST   /api/issues                              - Create new issue
PUT    /api/issues/{id}                         - Update issue
DELETE /api/issues/{id}                         - Delete issue
POST   /api/issues/{id}/convert-to-task         - Convert to task
```

### 4. Database Migration

To migrate from your current PHP system:

1. **Schema Updates**: The existing schema is mostly compatible
2. **Data Migration**: Use Spring Boot's data migration tools or custom SQL scripts
3. **Authentication**: Implement JWT-based authentication to replace PHP sessions

### 5. Testing Strategy

#### Unit Tests

```java
@SpringBootTest
@Transactional
class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @Test
    void testCreateNote() {
        // Test note creation
    }

    @Test
    void testCollaborativeNoteAccess() {
        // Test collaborative note permissions
    }

    @Test
    void testPrivateNoteAccess() {
        // Test private note visibility
    }
}
```

#### Integration Tests

```java
@SpringBootTest
@AutoConfigureTestDatabase
@TestMethodOrder(OrderAnnotation.class)
class NoteControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void testCreateNote() {
        // Test API endpoint
    }

    @Test
    @Order(2)
    void testGetNotes() {
        // Test API endpoint
    }
}
```

## Migration Steps

1. **Setup Spring Boot Project**: Create new project with required dependencies
2. **Create Entity Classes**: Define JPA entities matching your database schema
3. **Implement Repositories**: Create repository interfaces with custom queries
4. **Develop Services**: Implement business logic with proper access control
5. **Create Controllers**: Implement REST API endpoints
6. **Setup Security**: Configure JWT authentication and authorization
7. **Add Validation**: Implement request validation and error handling
8. **Write Tests**: Create comprehensive test suite
9. **Data Migration**: Plan and execute database migration
10. **Frontend Integration**: Update frontend to use new APIs

This migration will give you a robust, scalable, and maintainable notes and issues management system with full collaboration features in Spring Boot.
