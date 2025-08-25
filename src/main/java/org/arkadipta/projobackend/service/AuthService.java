package org.arkadipta.projobackend.service;

import org.arkadipta.projobackend.dto.request.*;
import org.arkadipta.projobackend.dto.response.LoginResponse;
import org.arkadipta.projobackend.dto.response.UserResponse;
import org.arkadipta.projobackend.entity.ActivityLog;
import org.arkadipta.projobackend.entity.User;
import org.arkadipta.projobackend.repository.ActivityLogRepository;
import org.arkadipta.projobackend.repository.UserRepository;
import org.arkadipta.projobackend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private EmailService emailService;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email address before logging in");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtTokenUtil.generateToken(userDetails);

        // Log activity
        ActivityLog log = new ActivityLog(user, "User logged in");
        activityLogRepository.save(log);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.getCreatedAt());

        return new LoginResponse(true, jwtToken, userResponse);
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Generate OTP for email verification
        String otp = emailService.generateOTP();
        LocalDateTime otpExpiry = emailService.getOTPExpiryTime();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        user.setEmailVerified(false);
        user.setVerificationOtp(otp);
        user.setOtpExpiryTime(otpExpiry);

        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationOTP(request.getEmail(), request.getFullName(), otp);

        // Log activity
        ActivityLog log = new ActivityLog(user, "User registered - Email verification pending");
        activityLogRepository.save(log);
    }

    public boolean validateToken(String token, String username) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtTokenUtil.isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    public void verifyEmail(EmailVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        if (!emailService.isOTPValid(request.getOtp(), user.getVerificationOtp(), user.getOtpExpiryTime())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setEmailVerified(true);
        user.setVerificationOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Email verified successfully");
        activityLogRepository.save(log);
    }

    public void resendVerificationOTP(ResendOTPRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        // Generate new OTP
        String otp = emailService.generateOTP();
        LocalDateTime otpExpiry = emailService.getOTPExpiryTime();

        user.setVerificationOtp(otp);
        user.setOtpExpiryTime(otpExpiry);
        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationOTP(request.getEmail(), user.getFullName(), otp);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Verification OTP resent");
        activityLogRepository.save(log);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        // Generate OTP for password reset
        String otp = emailService.generateOTP();
        LocalDateTime otpExpiry = emailService.getOTPExpiryTime();

        user.setResetPasswordOtp(otp);
        user.setResetPasswordOtpExpiry(otpExpiry);
        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetOTP(request.getEmail(), user.getFullName(), otp);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Password reset OTP requested");
        activityLogRepository.save(log);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        if (!emailService.isOTPValid(request.getOtp(), user.getResetPasswordOtp(), user.getResetPasswordOtpExpiry())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordOtp(null);
        user.setResetPasswordOtpExpiry(null);
        userRepository.save(user);

        // Log activity
        ActivityLog log = new ActivityLog(user, "Password reset successfully");
        activityLogRepository.save(log);
    }
}
