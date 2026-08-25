package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class NodeResponse {

    private UUID id;
    private UUID workspaceId;
    private UUID nodeTypeId;
    private String nodeTypeName;
    private String nodeTypeColor;
    private String nodeTypeIcon;
    private String label;
    private Double positionX = 0.0;
    private Double positionY = 0.0;
    private Map<String, Object> attributes;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private boolean isDeleted;

    public NodeResponse() {
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

    public UUID getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(UUID nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    public String getNodeTypeColor() {
        return nodeTypeColor;
    }

    public void setNodeTypeColor(String nodeTypeColor) {
        this.nodeTypeColor = nodeTypeColor;
    }

    public String getNodeTypeIcon() {
        return nodeTypeIcon;
    }

    public void setNodeTypeIcon(String nodeTypeIcon) {
        this.nodeTypeIcon = nodeTypeIcon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return label;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX != null ? positionX : 0.0;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY != null ? positionY : 0.0;
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
