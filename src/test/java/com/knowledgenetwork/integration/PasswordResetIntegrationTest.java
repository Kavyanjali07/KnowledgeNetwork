package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.enums.OtpType;
import com.knowledgenetwork.domain.model.RefreshToken;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.domain.payload.request.ResetPasswordRequest;
import com.knowledgenetwork.domain.payload.response.AuthResponse;
import com.knowledgenetwork.repository.RefreshTokenRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.service.AuthService;
import com.knowledgenetwork.service.OtpService;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private String email;

    @BeforeEach
    void setUp() {
        email = "reset_user_" + UUID.randomUUID() + "@example.com";
        user = TestFixtures.createUser(email, "Reset", "Tester");
        user.setPasswordHash(passwordEncoder.encode("OldPassword123!"));
        user = userRepository.saveAndFlush(user);

        // Add active refresh token session
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setToken("token_" + UUID.randomUUID());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        refreshToken.setRevoked(false);
        refreshTokenRepository.saveAndFlush(refreshToken);
    }

    @Test
    void forgotPasswordAndResetFlow_ShouldSuccessfullyUpdatePasswordAndRevokeExistingSessions() {
        // Generate active raw OTP for user
        String rawOtp = otpService.generateAndSaveOtp(user.getId(), OtpType.PASSWORD_RESET);

        // Step 2: Confirm Reset Password
        ResetPasswordRequest resetReq = new ResetPasswordRequest();
        resetReq.setEmail(email);
        resetReq.setCode(rawOtp);
        resetReq.setNewPassword("NewSecurePassword123!");
        authService.resetPassword(resetReq);

        // Step 3: Verify Refresh Sessions Revoked
        boolean hasActiveTokens = refreshTokenRepository.findByUserId(user.getId()).stream().anyMatch(t -> !t.isRevoked());
        assertFalse(hasActiveTokens, "Existing refresh tokens must be revoked after password reset");

        // Step 4: Login with Old Password Fails
        assertThrows(Exception.class, () -> authService.login(new LoginRequest(email, "OldPassword123!")));

        // Step 5: Login with New Password Succeeds
        AuthResponse loginRes = authService.login(new LoginRequest(email, "NewSecurePassword123!"));
        assertNotNull(loginRes);
        assertNotNull(loginRes.getAccessToken());
    }
}
