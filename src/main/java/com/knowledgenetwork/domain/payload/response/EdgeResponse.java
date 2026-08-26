package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EdgeResponse {

    private UUID id;
    private UUID graphId;
    private UUID workspaceId;
    private UUID edgeTypeId;
    private String edgeTypeName;
    private String relationshipType;
    private boolean isDirected;
    private UUID sourceNodeId;
    private String sourceNodeLabel;
    private String sourceNodeTitle;
    private UUID targetNodeId;
    private String targetNodeLabel;
    private String targetNodeTitle;
    private String label;
    private String description;
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

    public UUID getGraphId() {
        return graphId != null ? graphId : workspaceId;
    }

    public void setGraphId(UUID graphId) {
        this.graphId = graphId;
        if (this.workspaceId == null) {
            this.workspaceId = graphId;
        }
    }

    public UUID getWorkspaceId() {
        return workspaceId != null ? workspaceId : graphId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        if (this.graphId == null) {
            this.graphId = workspaceId;
        }
    }

    public UUID getEdgeTypeId() {
        return edgeTypeId;
    }

    public void setEdgeTypeId(UUID edgeTypeId) {
        this.edgeTypeId = edgeTypeId;
    }

    public String getEdgeTypeName() {
        return edgeTypeName != null ? edgeTypeName : relationshipType;
    }

    public void setEdgeTypeName(String edgeTypeName) {
        this.edgeTypeName = edgeTypeName;
        if (this.relationshipType == null) {
            this.relationshipType = edgeTypeName;
        }
    }

    public String getRelationshipType() {
        return relationshipType != null ? relationshipType : edgeTypeName;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
        if (this.edgeTypeName == null) {
            this.edgeTypeName = relationshipType;
        }
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
        return sourceNodeLabel != null ? sourceNodeLabel : sourceNodeTitle;
    }

    public void setSourceNodeLabel(String sourceNodeLabel) {
        this.sourceNodeLabel = sourceNodeLabel;
        if (this.sourceNodeTitle == null) {
            this.sourceNodeTitle = sourceNodeLabel;
        }
    }

    public String getSourceNodeTitle() {
        return sourceNodeTitle != null ? sourceNodeTitle : sourceNodeLabel;
    }

    public void setSourceNodeTitle(String sourceNodeTitle) {
        this.sourceNodeTitle = sourceNodeTitle;
        if (this.sourceNodeLabel == null) {
            this.sourceNodeLabel = sourceNodeTitle;
        }
    }

    public UUID getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(UUID targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getTargetNodeLabel() {
        return targetNodeLabel != null ? targetNodeLabel : targetNodeTitle;
    }

    public void setTargetNodeLabel(String targetNodeLabel) {
        this.targetNodeLabel = targetNodeLabel;
        if (this.targetNodeTitle == null) {
            this.targetNodeTitle = targetNodeLabel;
        }
    }

    public String getTargetNodeTitle() {
        return targetNodeTitle != null ? targetNodeTitle : targetNodeLabel;
    }

    public void setTargetNodeTitle(String targetNodeTitle) {
        this.targetNodeTitle = targetNodeTitle;
        if (this.targetNodeLabel == null) {
            this.targetNodeLabel = targetNodeTitle;
        }
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
