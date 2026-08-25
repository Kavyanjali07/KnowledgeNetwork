package com.knowledgenetwork.domain.payload.request;

import java.time.Instant;
import java.util.UUID;

public class NodeFilterRequest {

    private UUID workspaceId;
    private UUID nodeTypeId;
    private String searchLabel;
    private String attributeKey;
    private String attributeValue;
    private Instant createdAfter;
    private Instant createdBefore;
    private String createdBy;
    private Boolean includeDeleted = false;

    public NodeFilterRequest() {
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

    public String getSearchLabel() {
        return searchLabel;
    }

    public void setSearchLabel(String searchLabel) {
        this.searchLabel = searchLabel;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public Instant getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(Instant createdAfter) {
        this.createdAfter = createdAfter;
    }

    public Instant getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(Instant createdBefore) {
        this.createdBefore = createdBefore;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getIncludeDeleted() {
        return includeDeleted != null && includeDeleted;
    }

    public void setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }
}
