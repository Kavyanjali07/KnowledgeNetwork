package com.knowledgenetwork.domain.payload.response;

import com.knowledgenetwork.domain.enums.LicenseType;
import java.time.Instant;
import java.util.UUID;

public class PublicRelatedNetworkResponse {

    private UUID id;
    private String title;
    private String description;
    private String ownerName;
    private String ownerUsername;
    private LicenseType licenseType;
    private long nodeCount;
    private long edgeCount;
    private String relationReason;
    private Instant publishedAt;

    public PublicRelatedNetworkResponse() {
    }

    public PublicRelatedNetworkResponse(UUID id, String title, String description,
                                       String ownerName, String ownerUsername,
                                       LicenseType licenseType, long nodeCount, long edgeCount,
                                       String relationReason, Instant publishedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerName = ownerName;
        this.ownerUsername = ownerUsername;
        this.licenseType = licenseType;
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
        this.relationReason = relationReason;
        this.publishedAt = publishedAt;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
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

    public String getRelationReason() {
        return relationReason;
    }

    public void setRelationReason(String relationReason) {
        this.relationReason = relationReason;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

}
