package com.knowledgenetwork.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.enums.Role;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.domain.payload.request.ResendOtpRequest;
import com.knowledgenetwork.domain.payload.request.VerifyEmailRequest;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFailureAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    private User verifiedUser;
    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // 1. Existing verified user
        User user1 = new User();
        user1.setEmail("verified@example.com");
        user1.setPasswordHash(passwordEncoder.encode("Password123!"));
        user1.setFirstName("Verified");
        user1.setLastName("User");
        user1.setRole(Role.USER);
        user1.setBio("");
        user1.setStatus("online");
        user1.setEnabled(true);
        user1.setEmailVerified(true);
        verifiedUser = userRepository.save(user1);

        // 2. Existing unverified user (e.g. created prior to email verification requirement)
        User user2 = new User();
        user2.setEmail("unverified@example.com");
        user2.setPasswordHash(passwordEncoder.encode("Password123!"));
        user2.setFirstName("Unverified");
        user2.setLastName("User");
        user2.setRole(Role.USER);
        user2.setBio("");
        user2.setStatus("online");
        user2.setEnabled(true);
        user2.setEmailVerified(false);
        unverifiedUser = userRepository.save(user2);
    }

    @Test
    @DisplayName("1. Verified existing user can log in successfully")
    void test1_VerifiedExistingUserCanLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("verified@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("verified@example.com"));
    }

    @Test
    @DisplayName("2. Existing unverified user receives EMAIL_NOT_VERIFIED (403 Forbidden)")
    void test2_UnverifiedUserReceivesEmailNotVerified() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("unverified@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("EMAIL_NOT_VERIFIED: Please verify your email before signing in."));
    }

    @Test
    @DisplayName("3. Existing unverified user can request a new OTP")
    void test3_UnverifiedUserCanRequestNewOtp() throws Exception {
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("unverified@example.com");

        mockMvc.perform(post("/api/v1/auth/resend-verification-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("4. Existing unverified user can verify OTP and 5. Log in after verification")
    void test4And5_VerifyOtpAndLogin() throws Exception {
        // Generate valid OTP for unverified user
        String otp = otpService.generateAndSaveOtp(unverifiedUser.getId());

        // Verify email via OTP endpoint
        VerifyEmailRequest verifyRequest = new VerifyEmailRequest();
        verifyRequest.setEmail("unverified@example.com");
        verifyRequest.setOtp(otp);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());

        // Assert DB status updated
        User updated = userRepository.findByEmail("unverified@example.com").orElseThrow();
        assertTrue(updated.isEmailVerified());

        // Now attempt login again -> should succeed
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("unverified@example.com");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("6. Wrong password remains rejected (401 Unauthorized)")
    void test6_WrongPasswordRejected() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("verified@example.com");
        request.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Invalid email or password provided."));
    }

    @Test
    @DisplayName("7. Unknown user remains rejected without account enumeration (401 Unauthorized)")
    void test7_UnknownUserRejected() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Invalid email or password provided."));
    }

    @Test
    @DisplayName("8. Invalid request payload returns validation errors (422 Unprocessable Entity)")
    void test8_InvalidPayloadReturnsValidationErrors() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }
}
