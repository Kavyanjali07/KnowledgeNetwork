package com.knowledgenetwork.domain.payload.request;

import com.knowledgenetwork.domain.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class WorkspaceMemberAddRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Workspace role is required")
    private WorkspaceRole role;

    public WorkspaceMemberAddRequest() {
    }

    public WorkspaceMemberAddRequest(UUID userId, WorkspaceRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public WorkspaceRole getRole() {
        return role;
    }

    public void setRole(WorkspaceRole role) {
        this.role = role;
    }
}
