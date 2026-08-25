package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.domain.payload.request.RegisterRequest;
import com.knowledgenetwork.domain.payload.response.AuthResponse;
import com.knowledgenetwork.domain.payload.response.RegisterResponse;
import com.knowledgenetwork.security.CustomUserDetailsService;
import com.knowledgenetwork.security.JwtTokenProvider;
import com.knowledgenetwork.security.RestAuthenticationEntryPoint;
import com.knowledgenetwork.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Test
    void registerShouldReturnCreatedResponse() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");
        request.setFirstName("User");
        request.setLastName("Example");

        RegisterResponse response = new RegisterResponse("user@example.com", true, "Verification code sent to your email address.");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationRequired").value(true));
    }

    @Test
    void loginShouldReturnOkResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("access-token", "refresh-token", 900L, null);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void loginUnverifiedUserShouldReturn403ForbiddenWithEmailNotVerified() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("unverified@example.com");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new com.knowledgenetwork.common.exception.EmailNotVerifiedException("unverified@example.com", "EMAIL_NOT_VERIFIED: Please verify your email before signing in."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("EMAIL_NOT_VERIFIED: Please verify your email before signing in."));
    }

    @Test
    void loginInvalidCredentialsShouldReturn401Unauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid email or password provided."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginValidationFailureShouldReturn422UnprocessableEntity() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
