package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.domain.payload.request.ForgotPasswordRequest;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.domain.payload.request.RefreshTokenRequest;
import com.knowledgenetwork.domain.payload.request.RegisterRequest;
import com.knowledgenetwork.domain.payload.request.ResendOtpRequest;
import com.knowledgenetwork.domain.payload.request.ResetPasswordRequest;
import com.knowledgenetwork.domain.payload.request.VerifyEmailRequest;
import com.knowledgenetwork.domain.payload.response.AuthResponse;
import com.knowledgenetwork.domain.payload.response.RegisterResponse;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration, email OTP verification, login, token refresh, and session logout APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // Local dev (HTTPS proxy in production)
                .path("/api/v1/auth")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createCleanRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account (unverified)")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Verification code sent to your email address", response));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify account email with 6-digit OTP code")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthResponse response = authService.verifyEmail(request);
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Email verified successfully", response));
    }

    @PostMapping("/resend-verification-otp")
    @Operation(summary = "Resend a new 6-digit verification code")
    public ResponseEntity<ApiResponse<String>> resendVerificationOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("A new verification code has been sent to your email address", "OTP_SENT"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and receive JWT access token + HttpOnly refresh cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a valid refresh token for a new JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        String tokenToUse = (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank())
                ? request.getRefreshToken()
                : refreshTokenCookie;

        if (tokenToUse == null || tokenToUse.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No refresh token provided"));
        }

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(tokenToUse);

        AuthResponse response = authService.refreshToken(refreshRequest);
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Tokens refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke user refresh tokens and terminate session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        UUID userId = (userPrincipal != null) ? userPrincipal.getId() : null;
        authService.logout(userId, refreshTokenCookie);

        ResponseCookie cleanCookie = createCleanRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a 6-digit password reset OTP (does not disclose user existence)")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If an account exists for this email, we've sent a password reset code.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using 6-digit OTP code and revoke all previous sessions")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Your password has been reset successfully. You can now log in with your new password.", null));
    }
}
