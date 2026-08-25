package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class GraphForkCreateRequest {

    @NotNull(message = "Source version ID is required")
    private UUID sourceVersionId;

    @NotBlank(message = "Fork name is required")
    @Size(max = 150, message = "Fork name cannot exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    public GraphForkCreateRequest() {
    }

    public GraphForkCreateRequest(UUID sourceVersionId, String name, String description) {
        this.sourceVersionId = sourceVersionId;
        this.name = name;
        this.description = description;
    }

    public UUID getSourceVersionId() {
        return sourceVersionId;
    }

    public void setSourceVersionId(UUID sourceVersionId) {
        this.sourceVersionId = sourceVersionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}