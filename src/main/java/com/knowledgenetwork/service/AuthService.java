package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.UserMapper;
import com.knowledgenetwork.domain.enums.Role;
import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.RefreshToken;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.domain.payload.request.RefreshTokenRequest;
import com.knowledgenetwork.domain.payload.request.RegisterRequest;
import com.knowledgenetwork.domain.payload.request.ResendOtpRequest;
import com.knowledgenetwork.domain.payload.request.VerifyEmailRequest;
import com.knowledgenetwork.domain.payload.response.AuthResponse;
import com.knowledgenetwork.domain.payload.response.RegisterResponse;
import com.knowledgenetwork.domain.payload.response.UserResponse;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.JwtTokenProvider;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.domain.enums.OtpType;
import com.knowledgenetwork.domain.payload.request.ForgotPasswordRequest;
import com.knowledgenetwork.domain.payload.request.ResetPasswordRequest;
import com.knowledgenetwork.repository.VerificationOtpRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final EdgeTypeRepository edgeTypeRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final VerificationOtpRepository verificationOtpRepository;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       RefreshTokenService refreshTokenService,
                       UserMapper userMapper,
                       WorkspaceRepository workspaceRepository,
                       WorkspaceMemberRepository workspaceMemberRepository,
                       NodeTypeRepository nodeTypeRepository,
                       EdgeTypeRepository edgeTypeRepository,
                       OtpService otpService,
                       EmailService emailService,
                       VerificationOtpRepository verificationOtpRepository,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.verificationOtpRepository = verificationOtpRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email address is already registered: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
        user.setBio("");
        user.setStatus("online");
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(true);

        User savedUser = userRepository.save(user);
        createDefaultUserStorage(savedUser);

        String otp = otpService.generateAndSaveOtp(savedUser.getId());
        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        return new RegisterResponse(
                savedUser.getEmail(),
                true,
                "Verification code sent to your email address."
        );
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.isEmailVerified()) {
            otpService.verifyOtp(user.getId(), request.getOtp());
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        UserPrincipal userPrincipal = UserPrincipal.fromUser(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        UserResponse userResponse = userMapper.toUserResponse(user);

        auditLogService.recordEventForUser(
                AuditAction.EMAIL_VERIFIED,
                AuditEntityType.AUTH,
                user.getId(),
                null,
                user,
                java.util.Map.of("email", user.getEmail())
        );

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                userResponse
        );
    }

    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            org.slf4j.LoggerFactory.getLogger(AuthService.class)
                    .info("Resend OTP requested for non-existent email address: [{}]", email);
            return;
        }

        User user = userOpt.get();
        if (user.isEmailVerified()) {
            throw new BusinessException("Your email is already verified. Please sign in.");
        }

        String newOtp = otpService.generateAndSaveOtp(user.getId());
        emailService.sendOtpEmail(user.getEmail(), newOtp);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (Exception ex) {
            userRepository.findByEmail(email).ifPresent(failedUser ->
                    auditLogService.recordEventForUser(
                            AuditAction.LOGIN_FAILURE,
                            AuditEntityType.AUTH,
                            failedUser.getId(),
                            null,
                            failedUser,
                            java.util.Map.of("email", email, "reason", "INVALID_CREDENTIALS")
                    )
            );
            throw ex;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password provided."));

        if (!user.isEmailVerified()) {
            auditLogService.recordEventForUser(
                    AuditAction.LOGIN_FAILURE,
                    AuditEntityType.AUTH,
                    user.getId(),
                    null,
                    user,
                    java.util.Map.of("email", email, "reason", "EMAIL_NOT_VERIFIED")
            );
            throw new com.knowledgenetwork.common.exception.EmailNotVerifiedException(
                    user.getEmail(),
                    "EMAIL_NOT_VERIFIED: Please verify your email before signing in."
            );
        }

        auditLogService.recordEventForUser(
                AuditAction.LOGIN_SUCCESS,
                AuditEntityType.AUTH,
                user.getId(),
                null,
                user,
                java.util.Map.of("email", email)
        );

        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        UserResponse userResponse = userMapper.toUserResponse(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                userResponse
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(token);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", token.getUserId()));

        if (!user.isEmailVerified()) {
            throw new com.knowledgenetwork.common.exception.EmailNotVerifiedException(
                    user.getEmail(),
                    "EMAIL_NOT_VERIFIED: Please verify your email."
            );
        }

        UserPrincipal userPrincipal = UserPrincipal.fromUser(user);
        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        UserResponse userResponse = userMapper.toUserResponse(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                userResponse
        );
    }

    @Transactional
    public void logout(UUID userId) {
        logout(userId, null);
    }

    @Transactional
    public void logout(UUID userId, String refreshTokenStr) {
        if (userId != null) {
            refreshTokenService.revokeByUserId(userId);
            auditLogService.recordEvent(
                    AuditAction.LOGOUT,
                    AuditEntityType.AUTH,
                    userId,
                    null,
                    java.util.Map.of("actorId", userId)
            );
        } else if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenService.revokeByToken(refreshTokenStr);
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String otp = otpService.generateAndSaveOtp(user.getId(), OtpType.PASSWORD_RESET);
            emailService.sendPasswordResetEmail(user.getEmail(), otp);
            auditLogService.recordEventForUser(
                    AuditAction.PASSWORD_RESET_REQUESTED,
                    AuditEntityType.AUTH,
                    user.getId(),
                    null,
                    user,
                    java.util.Map.of("email", email)
            );
        } else {
            org.slf4j.LoggerFactory.getLogger(AuthService.class)
                    .info("Password reset requested for non-existent email address: [{}]", email);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid password reset request or verification code."));

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new BusinessException("New password must be at least 8 characters long.");
        }

        // Verify OTP for PASSWORD_RESET purpose
        otpService.verifyOtp(user.getId(), request.getCode(), OtpType.PASSWORD_RESET);

        // Update password hash
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all remaining reset OTPs for user
        verificationOtpRepository.invalidateAllByUserIdAndOtpType(user.getId(), OtpType.PASSWORD_RESET);

        // Revoke all active user sessions / refresh tokens
        refreshTokenService.revokeByUserId(user.getId());

        auditLogService.recordEventForUser(
                AuditAction.PASSWORD_RESET_COMPLETED,
                AuditEntityType.AUTH,
                user.getId(),
                null,
                user,
                java.util.Map.of("email", email)
        );

        org.slf4j.LoggerFactory.getLogger(AuthService.class)
                .info("Successfully reset password and revoked sessions for user [{}]", user.getId());
    }

    private void createDefaultUserStorage(User user) {
        String userId = user.getId().toString();
        Workspace workspace = new Workspace(
                "My Knowledge Space",
                "Your private graph workspace for saved notes, ideas, decisions, and relationships.",
                user
        );
        workspace.setCreatedBy(userId);
        workspace.setUpdatedBy(userId);
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember ownerMembership = new WorkspaceMember(savedWorkspace, user, WorkspaceRole.OWNER);
        workspaceMemberRepository.save(ownerMembership);

        nodeTypeRepository.save(new NodeType(savedWorkspace, "Concept", "#22D3EE", "brain"));
        nodeTypeRepository.save(new NodeType(savedWorkspace, "Document", "#A78BFA", "file-text"));
        nodeTypeRepository.save(new NodeType(savedWorkspace, "Person", "#6EE7B7", "user"));
        nodeTypeRepository.save(new NodeType(savedWorkspace, "Decision", "#FCD34D", "check-circle"));

        edgeTypeRepository.save(new EdgeType(savedWorkspace, "Related to", true));
        edgeTypeRepository.save(new EdgeType(savedWorkspace, "Depends on", true));
        edgeTypeRepository.save(new EdgeType(savedWorkspace, "References", true));
    }
}
