package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final Environment environment;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromAddress,
            Environment environment) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.environment = environment;
    }

    private boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    public void sendOtpEmail(String toEmail, String otp) {
        boolean isProd = isProduction();

        if (fromAddress == null || fromAddress.isBlank()) {
            if (isProd) {
                log.error("SMTP credentials not configured in production. Cannot send verification email to [{}]", toEmail);
                throw new BusinessException("Email delivery service is currently unavailable. Please contact system administrator.");
            }
            log.warn("SMTP credentials not configured (MAIL_USERNAME is empty). Development fallback OTP for [{}] is [{}]", toEmail, otp);
            log.warn("Actual email delivery skipped. Configure MAIL_USERNAME and MAIL_PASSWORD in your environment to send real emails.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, "KnowledgeNetwork Security");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your KnowledgeNetwork Account");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #080c14; color: #e2e8f0; padding: 20px; }
                        .card { background-color: #0f172a; border: 1px solid #1e293b; border-radius: 12px; max-width: 500px; margin: 0 auto; padding: 30px; }
                        .title { color: #38bdf8; font-size: 22px; font-weight: 600; margin-bottom: 12px; }
                        .otp-box { background-color: #1e293b; border: 1px dashed #38bdf8; border-radius: 8px; font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #38bdf8; text-align: center; padding: 16px; margin: 24px 0; }
                        .footer { font-size: 12px; color: #64748b; margin-top: 24px; text-align: center; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="title">KnowledgeNetwork Email Verification</div>
                        <p>Welcome! Use the 6-digit verification code below to complete your registration:</p>
                        <div class="otp-box">%s</div>
                        <p>This verification code will expire in <strong>10 minutes</strong>. If you did not request this, please ignore this email.</p>
                        <div class="footer">&copy; 2026 KnowledgeNetwork. All rights reserved.</div>
                    </div>
                </body>
                </html>
                """.formatted(otp);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent verification OTP email to [{}]", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to [{}]: {}", toEmail, ex.getMessage());
            if (isProd) {
                throw new BusinessException("Failed to send verification email. Please check your email address or try again later.");
            }
            throw new BusinessException("Email delivery failed: " + ex.getMessage(), ex);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String otp) {
        boolean isProd = isProduction();

        if (fromAddress == null || fromAddress.isBlank()) {
            if (isProd) {
                log.error("SMTP credentials not configured in production. Cannot send password reset email to [{}]", toEmail);
                throw new BusinessException("Email delivery service is currently unavailable. Please contact system administrator.");
            }
            log.warn("SMTP credentials not configured (MAIL_USERNAME is empty). Development fallback Password Reset OTP for [{}] is [{}]", toEmail, otp);
            log.warn("Actual email delivery skipped. Configure MAIL_USERNAME and MAIL_PASSWORD in your environment to send real emails.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, "KnowledgeNetwork Security");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - KnowledgeNetwork");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #080c14; color: #e2e8f0; padding: 20px; }
                        .card { background-color: #0f172a; border: 1px solid #1e293b; border-radius: 12px; max-width: 500px; margin: 0 auto; padding: 30px; }
                        .title { color: #38bdf8; font-size: 22px; font-weight: 600; margin-bottom: 12px; }
                        .otp-box { background-color: #1e293b; border: 1px dashed #38bdf8; border-radius: 8px; font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #38bdf8; text-align: center; padding: 16px; margin: 24px 0; }
                        .footer { font-size: 12px; color: #64748b; margin-top: 24px; text-align: center; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="title">Password Reset Request</div>
                        <p>We received a request to reset your KnowledgeNetwork password. Use the 6-digit code below to proceed:</p>
                        <div class="otp-box">%s</div>
                        <p>This code expires in <strong>10 minutes</strong> and can only be used once.</p>
                        <p>If you did not request this, you can safely ignore this email.</p>
                        <div class="footer">&copy; 2026 KnowledgeNetwork. All rights reserved.</div>
                    </div>
                </body>
                </html>
                """.formatted(otp);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent password reset OTP email to [{}]", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to [{}]: {}", toEmail, ex.getMessage());
            if (isProd) {
                throw new BusinessException("Failed to send password reset email. Please try again later.");
            }
            throw new BusinessException("Email delivery failed: " + ex.getMessage(), ex);
        }
    }
}
