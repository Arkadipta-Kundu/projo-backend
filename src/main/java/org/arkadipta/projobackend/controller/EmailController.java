package org.arkadipta.projobackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.arkadipta.projobackend.dto.request.*;
import org.arkadipta.projobackend.dto.response.ApiResponse;
import org.arkadipta.projobackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailController {

    private final AuthService authService;

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            authService.verifyEmail(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Email verified successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/resend-verification-otp")
    public ResponseEntity<ApiResponse<String>> resendVerificationOTP(@Valid @RequestBody ResendOTPRequest request) {
        try {
            authService.resendVerificationOTP(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Verification OTP sent successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Password reset OTP sent to your email", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
