package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EdgeCreateRequest {

    private UUID workspaceId;

    private UUID edgeTypeId;

    private String relationshipType;

    @NotNull(message = "Source Node ID is required")
    private UUID sourceNodeId;

    @NotNull(message = "Target Node ID is required")
    private UUID targetNodeId;

    @Size(max = 255, message = "Label must not exceed 255 characters")
    private String label;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Double weight = 1.0;

    private Map<String, Object> attributes = new HashMap<>();

    public EdgeCreateRequest() {
    }

    public EdgeCreateRequest(UUID workspaceId, UUID edgeTypeId, UUID sourceNodeId, UUID targetNodeId, Double weight, Map<String, Object> attributes) {
        this.workspaceId = workspaceId;
        this.edgeTypeId = edgeTypeId;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.weight = weight != null ? weight : 1.0;
        if (attributes != null) {
            this.attributes = attributes;
        }
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UUID getEdgeTypeId() {
        return edgeTypeId;
    }

    public void setEdgeTypeId(UUID edgeTypeId) {
        this.edgeTypeId = edgeTypeId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public UUID getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(UUID sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public UUID getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(UUID targetNodeId) {
        this.targetNodeId = targetNodeId;
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? attributes : new HashMap<>();
    }
}
