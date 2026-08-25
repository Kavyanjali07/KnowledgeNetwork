package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WorkspaceCreateRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 150, message = "Workspace name cannot exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private com.knowledgenetwork.domain.model.Visibility visibility = com.knowledgenetwork.domain.model.Visibility.PRIVATE;

    public WorkspaceCreateRequest() {
    }

    public WorkspaceCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
        this.visibility = com.knowledgenetwork.domain.model.Visibility.PRIVATE;
    }

    public WorkspaceCreateRequest(String name, String description, com.knowledgenetwork.domain.model.Visibility visibility) {
        this.name = name;
        this.description = description;
        this.visibility = visibility != null ? visibility : com.knowledgenetwork.domain.model.Visibility.PRIVATE;
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
        this.visibility = visibility != null ? visibility : com.knowledgenetwork.domain.model.Visibility.PRIVATE;
    }
}
