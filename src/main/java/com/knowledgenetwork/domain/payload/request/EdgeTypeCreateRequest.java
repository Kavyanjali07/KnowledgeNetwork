package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class EdgeTypeCreateRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    @NotBlank(message = "Edge type name is required")
    @Size(max = 100, message = "Edge type name cannot exceed 100 characters")
    private String name;

    private boolean isDirected = true;

    public EdgeTypeCreateRequest() {
    }

    public EdgeTypeCreateRequest(UUID workspaceId, String name, boolean isDirected) {
        this.workspaceId = workspaceId;
        this.name = name;
        this.isDirected = isDirected;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setDirected(boolean directed) {
        isDirected = directed;
    }
}
