package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.domain.enums.OtpType;
import com.knowledgenetwork.domain.model.VerificationOtp;
import com.knowledgenetwork.repository.VerificationOtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private VerificationOtpRepository otpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private OtpService otpService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(otpRepository, passwordEncoder, 600000L, 60L);
        userId = UUID.randomUUID();
    }

    @Test
    void generateAndSaveOtpShouldInvalidatePreviousAndSaveNew() {
        when(otpRepository.findTopByUserIdAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(userId, OtpType.EMAIL_VERIFICATION)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-otp");
        when(otpRepository.save(any(VerificationOtp.class))).thenAnswer(i -> i.getArgument(0));

        String rawOtp = otpService.generateAndSaveOtp(userId);

        assertNotNull(rawOtp);
        assertEquals(6, rawOtp.length());
        assertTrue(rawOtp.matches("\\d{6}"));
        verify(otpRepository).invalidateAllByUserIdAndOtpType(userId, OtpType.EMAIL_VERIFICATION);
        verify(otpRepository).save(any(VerificationOtp.class));
    }

    @Test
    void verifyOtpShouldSucceedOnValidUnexpiredOtp() {
        VerificationOtp otp = new VerificationOtp();
        otp.setUserId(userId);
        otp.setOtpHash("hashed-otp");
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        otp.setUsed(false);

        when(otpRepository.findTopByUserIdAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(userId, OtpType.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);

        assertDoesNotThrow(() -> otpService.verifyOtp(userId, "123456"));
        assertTrue(otp.isUsed());
        verify(otpRepository).save(otp);
    }

    @Test
    void verifyOtpShouldThrowOnInvalidCode() {
        VerificationOtp otp = new VerificationOtp();
        otp.setUserId(userId);
        otp.setOtpHash("hashed-otp");
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        otp.setUsed(false);

        when(otpRepository.findTopByUserIdAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(userId, OtpType.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));
        when(passwordEncoder.matches("654321", "hashed-otp")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> otpService.verifyOtp(userId, "654321"));
        assertTrue(ex.getMessage().contains("Invalid verification code"));
        assertFalse(otp.isUsed());
    }

    @Test
    void verifyOtpShouldThrowOnExpiredCode() {
        VerificationOtp otp = new VerificationOtp();
        otp.setUserId(userId);
        otp.setOtpHash("hashed-otp");
        otp.setExpiresAt(Instant.now().minusSeconds(10));
        otp.setUsed(false);

        when(otpRepository.findTopByUserIdAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(userId, OtpType.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));

        BusinessException ex = assertThrows(BusinessException.class, () -> otpService.verifyOtp(userId, "123456"));
        assertTrue(ex.getMessage().contains("expired"));
        assertTrue(otp.isUsed());
    }

    @Test
    void generateAndSaveOtpShouldThrowWhenCooldownActive() {
        VerificationOtp otp = new VerificationOtp();
        otp.setUserId(userId);
        otp.setCreatedAt(Instant.now().minusSeconds(20));

        when(otpRepository.findTopByUserIdAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(userId, OtpType.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));

        BusinessException exception = assertThrows(BusinessException.class, () -> otpService.generateAndSaveOtp(userId));
        assertTrue(exception.getMessage().contains("Please wait"));
    }
}
