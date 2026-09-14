package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.mapper.UserMapper;
import com.knowledgenetwork.domain.model.RefreshToken;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.domain.payload.request.RefreshTokenRequest;
import com.knowledgenetwork.domain.payload.request.RegisterRequest;
import com.knowledgenetwork.domain.payload.request.ResendOtpRequest;
import com.knowledgenetwork.domain.payload.request.VerifyEmailRequest;
import com.knowledgenetwork.domain.payload.response.AuthResponse;
import com.knowledgenetwork.domain.payload.response.RegisterResponse;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.VerificationOtpRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.JwtTokenProvider;
import com.knowledgenetwork.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private NodeTypeRepository nodeTypeRepository;

    @Mock
    private EdgeTypeRepository edgeTypeRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationOtpRepository verificationOtpRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldCreateUserAndReturnRegisterResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("Alice@example.com");
        request.setPassword("password123");
        request.setFirstName("Alice");
        request.setLastName("Example");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(workspaceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(otpService.generateAndSaveOtp(any(UUID.class))).thenReturn("123456");

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertTrue(response.isVerificationRequired());
        assertEquals("alice@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
        verify(workspaceRepository).save(any());
        verify(workspaceMemberRepository).save(any());
        verify(nodeTypeRepository, times(4)).save(any());
        verify(edgeTypeRepository, times(3)).save(any());
        verify(otpService).generateAndSaveOtp(any(UUID.class));
        verify(emailService).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setFirstName("Alice");
        request.setLastName("Example");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
    }

    @Test
    void verifyEmailShouldVerifyUserAndReturnTokens() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("alice@example.com");
        request.setOtp("123456");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("alice@example.com");
        user.setEmailVerified(false);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(new RefreshToken());
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new com.knowledgenetwork.domain.payload.response.UserResponse());

        AuthResponse response = authService.verifyEmail(request);

        assertNotNull(response);
        assertTrue(user.isEmailVerified());
        assertEquals("access-token", response.getAccessToken());
        verify(otpService).verifyOtp(userId, "123456");
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmailShouldRejectInvalidOtp() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("alice@example.com");
        request.setOtp("111111");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        doThrow(new BusinessException("Invalid verification code")).when(otpService).verifyOtp(userId, "111111");

        assertThrows(BusinessException.class, () -> authService.verifyEmail(request));
    }

    @Test
    void resendOtpShouldGenerateNewOtp() {
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("alice@example.com");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("alice@example.com");
        user.setEmailVerified(false);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateAndSaveOtp(userId)).thenReturn("654321");

        authService.resendOtp(request);

        verify(otpService).generateAndSaveOtp(userId);
        verify(emailService).sendOtpEmail("alice@example.com", "654321");
    }

    @Test
    void loginShouldAuthenticateAndReturnTokensWhenVerified() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("alice@example.com");
        user.setEmailVerified(true);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(UserPrincipal.fromUser(user), null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(new RefreshToken());
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new com.knowledgenetwork.domain.payload.response.UserResponse());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void loginShouldRejectUnverifiedUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("alice@example.com");
        user.setEmailVerified(false);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(UserPrincipal.fromUser(user), null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(com.knowledgenetwork.common.exception.EmailNotVerifiedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldRejectInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any())).thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refreshTokenShouldIssueNewTokensWhenVerified() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUserId(UUID.randomUUID());

        User user = new User();
        user.setEmailVerified(true);

        when(refreshTokenService.findByToken("refresh-token")).thenReturn(refreshToken);
        when(userRepository.findById(refreshToken.getUserId())).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("new-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(new RefreshToken());
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new com.knowledgenetwork.domain.payload.response.UserResponse());

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-token", response.getAccessToken());
    }

    @Test
    void forgotPasswordShouldGenerateResetOtpAndSendEmailForExistingUser() {
        com.knowledgenetwork.domain.payload.request.ForgotPasswordRequest request =
                new com.knowledgenetwork.domain.payload.request.ForgotPasswordRequest("alice@example.com");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateAndSaveOtp(userId, com.knowledgenetwork.domain.enums.OtpType.PASSWORD_RESET)).thenReturn("123456");

        authService.forgotPassword(request);

        verify(otpService).generateAndSaveOtp(userId, com.knowledgenetwork.domain.enums.OtpType.PASSWORD_RESET);
        verify(emailService).sendPasswordResetEmail("alice@example.com", "123456");
    }

    @Test
    void forgotPasswordShouldNotThrowExceptionForNonExistentUser() {
        com.knowledgenetwork.domain.payload.request.ForgotPasswordRequest request =
                new com.knowledgenetwork.domain.payload.request.ForgotPasswordRequest("nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.forgotPassword(request));
    }

    @Test
    void resetPasswordShouldUpdatePasswordAndRevokeSessionsForValidOtp() {
        com.knowledgenetwork.domain.payload.request.ResetPasswordRequest request =
                new com.knowledgenetwork.domain.payload.request.ResetPasswordRequest("alice@example.com", "123456", "newPassword123");

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");

        authService.resetPassword(request);

        verify(otpService).verifyOtp(userId, "123456", com.knowledgenetwork.domain.enums.OtpType.PASSWORD_RESET);
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);
        verify(verificationOtpRepository).invalidateAllByUserIdAndOtpType(userId, com.knowledgenetwork.domain.enums.OtpType.PASSWORD_RESET);
        verify(refreshTokenService).revokeByUserId(userId);
        assertEquals("hashedNewPassword", user.getPasswordHash());
    }

    @Test
    void resetPasswordShouldRejectShortPassword() {
        com.knowledgenetwork.domain.payload.request.ResetPasswordRequest request =
                new com.knowledgenetwork.domain.payload.request.ResetPasswordRequest("alice@example.com", "123456", "short");

        User user = new User();
        user.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> authService.resetPassword(request));
    }
}
