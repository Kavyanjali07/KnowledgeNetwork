package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GraphSnapshotCreateRequest {

    @NotBlank(message = "Label is required")
    @Size(max = 150, message = "Label cannot exceed 150 characters")
    private String label;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    public GraphSnapshotCreateRequest() {
    }

    public GraphSnapshotCreateRequest(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}