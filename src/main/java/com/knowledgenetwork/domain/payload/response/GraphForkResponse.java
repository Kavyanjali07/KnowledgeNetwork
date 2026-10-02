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
    private UUID sourceWorkspaceId;
    private com.knowledgenetwork.domain.enums.LicenseType sourceLicense;
    private UUID originalCreatorId;
    private String originalCreatorName;
    private boolean isDerivative = true;

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
    public UUID getSourceWorkspaceId() { return sourceWorkspaceId; }
    public void setSourceWorkspaceId(UUID sourceWorkspaceId) { this.sourceWorkspaceId = sourceWorkspaceId; }
    public com.knowledgenetwork.domain.enums.LicenseType getSourceLicense() { return sourceLicense; }
    public void setSourceLicense(com.knowledgenetwork.domain.enums.LicenseType sourceLicense) { this.sourceLicense = sourceLicense; }
    public UUID getOriginalCreatorId() { return originalCreatorId; }
    public void setOriginalCreatorId(UUID originalCreatorId) { this.originalCreatorId = originalCreatorId; }
    public String getOriginalCreatorName() { return originalCreatorName; }
    public void setOriginalCreatorName(String originalCreatorName) { this.originalCreatorName = originalCreatorName; }
    public boolean isDerivative() { return isDerivative; }
    public void setDerivative(boolean derivative) { isDerivative = derivative; }
}