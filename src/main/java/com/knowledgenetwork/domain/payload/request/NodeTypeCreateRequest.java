package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class NodeTypeCreateRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    @NotBlank(message = "Node type name is required")
    @Size(max = 100, message = "Node type name cannot exceed 100 characters")
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color code must be a valid hex color code (e.g. #3B82F6)")
    private String colorCode = "#3B82F6";

    @Size(max = 50, message = "Icon identifier cannot exceed 50 characters")
    private String icon = "default-node";

    public NodeTypeCreateRequest() {
    }

    public NodeTypeCreateRequest(UUID workspaceId, String name, String colorCode, String icon) {
        this.workspaceId = workspaceId;
        this.name = name;
        this.colorCode = colorCode;
        this.icon = icon;
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

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
