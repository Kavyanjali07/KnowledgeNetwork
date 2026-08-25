package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.UUID;

public class WorkspaceResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private String ownerEmail;
    private String ownerName;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private boolean isDeleted;

    private String title;
    private com.knowledgenetwork.domain.model.Visibility visibility = com.knowledgenetwork.domain.model.Visibility.PRIVATE;

    public WorkspaceResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.title = name;
    }

    public String getTitle() {
        return title != null ? title : name;
    }

    public void setTitle(String title) {
        this.title = title;
        if (this.name == null) {
            this.name = title;
        }
    }

    public com.knowledgenetwork.domain.model.Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(com.knowledgenetwork.domain.model.Visibility visibility) {
        this.visibility = visibility;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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
