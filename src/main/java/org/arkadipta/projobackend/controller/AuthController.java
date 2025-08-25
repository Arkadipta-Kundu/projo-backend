package org.arkadipta.projobackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.arkadipta.projobackend.dto.request.LoginRequest;
import org.arkadipta.projobackend.dto.request.RegisterRequest;
import org.arkadipta.projobackend.dto.response.ApiResponse;
import org.arkadipta.projobackend.dto.response.LoginResponse;
import org.arkadipta.projobackend.dto.response.UserResponse;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.repository.UserRepository;
import org.arkadipta.projobackend.service.AuthService;
import org.arkadipta.projobackend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "http://localhost:5174",
        "http://127.0.0.1:5174" })
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "User login", description = "Authenticate user with username/email and password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Account not verified")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "User registration", description = "Register a new user account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registration successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Username or email already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation errors")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // In a stateless JWT implementation, logout is handled client-side
        // You could implement a token blacklist here if needed
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("valid", false));
            }

            String token = authHeader.substring(7);
            String username = jwtTokenUtil.extractUsername(token);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isValid = authService.validateToken(token, username);

            if (isValid) {
                UserResponse userResponse = new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.isEmailVerified(),
                        user.getCreatedAt());

                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "user", userResponse));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.isEmailVerified(),
                    user.getCreatedAt());

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
