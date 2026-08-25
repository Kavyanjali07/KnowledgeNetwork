package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EdgeResponse {

    private UUID id;
    private UUID workspaceId;
    private UUID edgeTypeId;
    private String edgeTypeName;
    private boolean isDirected;
    private UUID sourceNodeId;
    private String sourceNodeLabel;
    private UUID targetNodeId;
    private String targetNodeLabel;
    private Double weight;
    private Map<String, Object> attributes;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private boolean isDeleted;

    public EdgeResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getEdgeTypeName() {
        return edgeTypeName;
    }

    public void setEdgeTypeName(String edgeTypeName) {
        this.edgeTypeName = edgeTypeName;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setDirected(boolean directed) {
        isDirected = directed;
    }

    public UUID getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(UUID sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public String getSourceNodeLabel() {
        return sourceNodeLabel;
    }

    public void setSourceNodeLabel(String sourceNodeLabel) {
        this.sourceNodeLabel = sourceNodeLabel;
    }

    public UUID getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(UUID targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getTargetNodeLabel() {
        return targetNodeLabel;
    }

    public void setTargetNodeLabel(String targetNodeLabel) {
        this.targetNodeLabel = targetNodeLabel;
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
        this.attributes = attributes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
