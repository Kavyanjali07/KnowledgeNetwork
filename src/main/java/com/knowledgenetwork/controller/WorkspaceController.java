package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.domain.payload.request.WorkspaceCreateRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceMemberAddRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceUpdateRequest;
import com.knowledgenetwork.domain.payload.response.WorkspaceMemberResponse;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import com.knowledgenetwork.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@Tag(name = "Workspaces", description = "Workspace and membership management APIs")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    @Operation(summary = "List all workspaces for the current user")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        List<WorkspaceResponse> workspaces = workspaceService.getWorkspacesForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved successfully", workspaces));
    }

    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @Valid @RequestBody WorkspaceCreateRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace by ID")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(@PathVariable UUID id) {
        WorkspaceResponse response = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable UUID id,
            @Valid @RequestBody WorkspaceUpdateRequest request) {
        WorkspaceResponse response = workspaceService.updateWorkspace(id, request);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a workspace")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(@PathVariable UUID id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted successfully", null));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List members of a workspace")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getMembers(@PathVariable UUID id) {
        List<WorkspaceMemberResponse> members = workspaceService.getMembers(id);
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to a workspace")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody WorkspaceMemberAddRequest request) {
        WorkspaceMemberResponse response = workspaceService.addMember(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", response));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Remove a member from a workspace")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID memberId) {
        workspaceService.removeMember(id, memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}