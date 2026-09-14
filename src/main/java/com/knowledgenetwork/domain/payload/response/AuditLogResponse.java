package com.knowledgenetwork.domain.payload.response;

import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AuditLogResponse {

    private UUID id;
    private UUID workspaceId;
    private UUID actorId;
    private String actorEmail;
    private String actorName;
    private AuditAction action;
    private AuditEntityType entityType;
    private UUID entityId;
    private Map<String, Object> metadata;
    private Instant timestamp;

    public AuditLogResponse() {
    }

    public AuditLogResponse(UUID id, UUID workspaceId, UUID actorId, String actorEmail, String actorName,
                            AuditAction action, AuditEntityType entityType, UUID entityId,
                            Map<String, Object> metadata, Instant timestamp) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.actorName = actorName;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.metadata = metadata;
        this.timestamp = timestamp;
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

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(AuditEntityType entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
