package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.UUID;

public class GraphForkResponse {
    private UUID id;
    private UUID workspaceId;
    private UUID sourceVersionId;
    private String name;
    private String description;
    private Instant createdAt;
    private String createdBy;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
    public UUID getSourceVersionId() { return sourceVersionId; }
    public void setSourceVersionId(UUID sourceVersionId) { this.sourceVersionId = sourceVersionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}