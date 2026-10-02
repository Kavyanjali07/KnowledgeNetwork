package com.knowledgenetwork.domain.payload.response;

import com.knowledgenetwork.domain.model.Visibility;
import java.time.Instant;
import java.util.UUID;

public class GraphResponse {

    private UUID id;
    private String title;
    private String name;
    private String description;
    private UUID ownerId;
    private String ownerEmail;
    private String ownerName;
    private Visibility visibility;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private boolean isDeleted;
    private long nodeCount;
    private long edgeCount;
    private com.knowledgenetwork.domain.enums.LicenseType licenseType;
    private boolean isPublished;
    private Instant publishedAt;
    private String customAttribution;
    private long derivativeCount;
    private long referenceCount;

    public GraphResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getName() {
        return name != null ? name : title;
    }

    public void setName(String name) {
        this.name = name;
        if (this.title == null) {
            this.title = name;
        }
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

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
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

    public long getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(long nodeCount) {
        this.nodeCount = nodeCount;
    }

    public long getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(long edgeCount) {
        this.edgeCount = edgeCount;
    }

    public com.knowledgenetwork.domain.enums.LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(com.knowledgenetwork.domain.enums.LicenseType licenseType) {
        this.licenseType = licenseType;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCustomAttribution() {
        return customAttribution;
    }

    public void setCustomAttribution(String customAttribution) {
        this.customAttribution = customAttribution;
    }

    public long getDerivativeCount() {
        return derivativeCount;
    }

    public void setDerivativeCount(long derivativeCount) {
        this.derivativeCount = derivativeCount;
    }

    public long getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(long referenceCount) {
        this.referenceCount = referenceCount;
    }
}
