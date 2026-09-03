package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.domain.enums.OtpType;
import com.knowledgenetwork.domain.model.VerificationOtp;
import com.knowledgenetwork.repository.VerificationOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final SecureRandom secureRandom = new SecureRandom();

    private final VerificationOtpRepository verificationOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final long otpExpirationMs;
    private final long resendCooldownSeconds;

    public OtpService(
            VerificationOtpRepository verificationOtpRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.otp.expiration-ms:600000}") long otpExpirationMs,
            @Value("${app.otp.resend-cooldown-seconds:60}") long resendCooldownSeconds) {
        this.verificationOtpRepository = verificationOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpExpirationMs = otpExpirationMs;
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    @Transactional
    public String generateAndSaveOtp(UUID userId) {
        return generateAndSaveOtp(userId, OtpType.EMAIL_VERIFICATION);
    }

    @Transactional
    public String generateAndSaveOtp(UUID userId, OtpType otpType) {
        Optional<VerificationOtp> latestOtpOpt = verificationOtpRepository.findTopByUserIdAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(userId, otpType);
        if (latestOtpOpt.isPresent()) {
            VerificationOtp latest = latestOtpOpt.get();
            Instant cooldownTime = latest.getCreatedAt().plusSeconds(resendCooldownSeconds);
            if (Instant.now().isBefore(cooldownTime)) {
                long remainingSeconds = cooldownTime.getEpochSecond() - Instant.now().getEpochSecond();
                throw new BusinessException("Please wait " + Math.max(1, remainingSeconds) + " seconds before requesting another code.");
            }
        }

        // Invalidate all previous unused OTPs of this type for this user
        verificationOtpRepository.invalidateAllByUserIdAndOtpType(userId, otpType);

        // Generate 6-digit OTP using SecureRandom
        int number = secureRandom.nextInt(1_000_000);
        String rawOtp = String.format("%06d", number);

        VerificationOtp otpEntity = new VerificationOtp();
        otpEntity.setUserId(userId);
        otpEntity.setOtpType(otpType);
        otpEntity.setOtpHash(passwordEncoder.encode(rawOtp));
        otpEntity.setExpiresAt(Instant.now().plusMillis(otpExpirationMs));
        otpEntity.setUsed(false);

        verificationOtpRepository.save(otpEntity);
        log.info("Generated secure [{}] OTP for user [{}]", otpType, userId);

        return rawOtp;
    }

    @Transactional
    public void verifyOtp(UUID userId, String rawOtp) {
        verifyOtp(userId, rawOtp, OtpType.EMAIL_VERIFICATION);
    }

    @Transactional
    public void verifyOtp(UUID userId, String rawOtp, OtpType otpType) {
        if (rawOtp == null || rawOtp.trim().length() != 6) {
            throw new BusinessException("Verification code must be 6 digits.");
        }

        VerificationOtp otpEntity = verificationOtpRepository.findTopByUserIdAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(userId, otpType)
                .orElseThrow(() -> new BusinessException("No active verification code found. Please request a new code."));

        if (otpEntity.isExpired()) {
            otpEntity.setUsed(true);
            verificationOtpRepository.save(otpEntity);
            throw new BusinessException("Verification code has expired. Please request a new code.");
        }

        if (!passwordEncoder.matches(rawOtp.trim(), otpEntity.getOtpHash())) {
            throw new BusinessException("Invalid verification code. Please check and try again.");
        }

        otpEntity.setUsed(true);
        verificationOtpRepository.save(otpEntity);
        log.info("Successfully verified [{}] OTP for user [{}]", otpType, userId);
    }
}
