package org.arkadipta.projobackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.otp.expiry.minutes}")
    private int otpExpiryMinutes;
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a 6-digit OTP
     */
    public String generateOTP() {
        StringBuilder otp = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            otp.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return otp.toString();
    }

    /**
     * Calculate OTP expiry time
     */
    public LocalDateTime getOTPExpiryTime() {
        return LocalDateTime.now().plusMinutes(otpExpiryMinutes);
    }

    /**
     * Send email verification OTP with beautiful HTML template
     */
    public void sendVerificationOTP(String toEmail, String username, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email properties
            helper.setFrom(fromEmail, "Projo Team");
            helper.setTo(toEmail);
            helper.setSubject("🔐 Verify Your Email - Welcome to Projo!");

            // Create Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("otp", otp);
            context.setVariable("expiryMinutes", otpExpiryMinutes);

            // Process the template
            String htmlContent = templateEngine.process("email/email-verification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending verification OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send password reset OTP with beautiful HTML template
     */
    public void sendPasswordResetOTP(String toEmail, String username, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email properties
            helper.setFrom(fromEmail, "Projo Security Team");
            helper.setTo(toEmail);
            helper.setSubject("🔒 Password Reset Request - Projo");

            // Create Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("otp", otp);
            context.setVariable("expiryMinutes", otpExpiryMinutes);

            // Process the template
            String htmlContent = templateEngine.process("email/password-reset", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending password reset OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Check if OTP is valid and not expired
     */
    public boolean isOTPValid(String providedOTP, String storedOTP, LocalDateTime expiryTime) {
        if (providedOTP == null || storedOTP == null || expiryTime == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(expiryTime)) {
            log.warn("OTP has expired");
            return false;
        }

        return providedOTP.equals(storedOTP);
    }
}
