package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EdgeCreateRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    @NotNull(message = "Edge Type ID is required")
    private UUID edgeTypeId;

    @NotNull(message = "Source Node ID is required")
    private UUID sourceNodeId;

    @NotNull(message = "Target Node ID is required")
    private UUID targetNodeId;

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
