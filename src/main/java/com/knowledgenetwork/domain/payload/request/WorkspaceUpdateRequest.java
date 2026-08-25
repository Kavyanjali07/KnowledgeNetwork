package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WorkspaceUpdateRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 150, message = "Workspace name cannot exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;

    private com.knowledgenetwork.domain.model.Visibility visibility;

    public WorkspaceUpdateRequest() {
    }

    public WorkspaceUpdateRequest(String name, String description, Long version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }

    public WorkspaceUpdateRequest(String name, String description, com.knowledgenetwork.domain.model.Visibility visibility, Long version) {
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return name;
    }

    public void setTitle(String title) {
        this.name = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public com.knowledgenetwork.domain.model.Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(com.knowledgenetwork.domain.model.Visibility visibility) {
        this.visibility = visibility;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
