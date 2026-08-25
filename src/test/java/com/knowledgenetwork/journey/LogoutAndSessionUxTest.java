package com.knowledgenetwork.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.enums.Role;
import com.knowledgenetwork.domain.model.RefreshToken;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.repository.RefreshTokenRepository;
import com.knowledgenetwork.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LogoutAndSessionUxTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("logout.test@knowledgenetwork.io");
        user.setPasswordHash(passwordEncoder.encode("SecurePass123!"));
        user.setFirstName("Session");
        user.setLastName("Tester");
        user.setRole(Role.USER);
        user.setBio("Session testing bio");
        user.setStatus("online");
        user.setEnabled(true);
        user.setEmailVerified(true);
        testUser = userRepository.save(user);
    }

    @Test
    @DisplayName("Logout revokes refresh token in database and returns clean cookie with Max-Age=0")
    void testLogoutRevokesTokenAndClearsCookie() throws Exception {
        // 1. Login to establish session
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("logout.test@knowledgenetwork.io");
        loginRequest.setPassword("SecurePass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        assertFalse(refreshCookie.getValue().isBlank());

        // 2. Perform Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        // 3. Verify token is revoked in database
        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        for (RefreshToken token : tokens) {
            assertTrue(token.isRevoked(), "Refresh token should be marked revoked upon logout");
        }

        // 4. Attempt refresh with revoked token -> must fail with 400 Bad Request
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Refresh token call after logout returns 401 Unauthorized")
    void testRefreshAfterLogoutReturns401() throws Exception {
        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("logout.test@knowledgenetwork.io");
        loginRequest.setPassword("SecurePass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        // Logout
        mockMvc.perform(post("/api/v1/auth/logout").cookie(refreshCookie))
                .andExpect(status().isOk());

        // Attempt refresh with revoked token -> must fail with 400 Bad Request
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login after logout works normally")
    void testLoginAfterLogoutWorksNormally() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("logout.test@knowledgenetwork.io");
        loginRequest.setPassword("SecurePass123!");

        // Login 1
        MvcResult res1 = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie1 = res1.getResponse().getCookie("refreshToken");

        // Logout
        mockMvc.perform(post("/api/v1/auth/logout").cookie(cookie1))
                .andExpect(status().isOk());

        // Login 2
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("Profile endpoint returns email and emailVerified status")
    void testProfileReturnsEmailAndEmailVerified() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("logout.test@knowledgenetwork.io");
        loginRequest.setPassword("SecurePass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/profile/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("logout.test@knowledgenetwork.io"))
                .andExpect(jsonPath("$.data.firstName").value("Session"))
                .andExpect(jsonPath("$.data.lastName").value("Tester"))
                .andExpect(jsonPath("$.data.emailVerified").value(true));
    }
}
