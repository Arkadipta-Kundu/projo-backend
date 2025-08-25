package org.arkadipta.projobackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskReminderEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    /**
     * Send task reminder email with beautiful HTML template
     */
    public void sendTaskReminderEmail(String toEmail, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set email properties
            helper.setFrom(fromEmail, "Projo Reminders");
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Create Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("subject", subject);
            context.setVariable("message", message);

            // Process the template
            String htmlContent = templateEngine.process("email/task-reminder", context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Task reminder email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send task reminder email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send task reminder email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending task reminder email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send task reminder email", e);
        }
    }
}
