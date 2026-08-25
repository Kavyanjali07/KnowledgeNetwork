package com.knowledgenetwork.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.domain.payload.request.ProfileUpdateRequest;
import com.knowledgenetwork.domain.payload.response.ProfileResponse;
import com.knowledgenetwork.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Profile loading, updates, graphs, and statistics")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    @Operation(summary = "Load the current user's profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentProfile() {
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profileService.getCurrentProfile()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the current user's profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateCurrentProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profileService.updateCurrentProfile(request)));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Load a user's public profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profileService.getProfile(userId)));
    }
}
