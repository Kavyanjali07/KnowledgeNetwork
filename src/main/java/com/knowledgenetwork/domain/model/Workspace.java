package com.knowledgenetwork.domain.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain Entity representing a collaborative Knowledge Graph Workspace.
 * Provides multi-tenant logical boundary for nodes, edges, node types, and edge types.
 */
@Entity
@Table(name = "workspaces")
public class Workspace extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceMember> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private Visibility visibility = Visibility.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 50)
    private com.knowledgenetwork.domain.enums.LicenseType licenseType = com.knowledgenetwork.domain.enums.LicenseType.ALL_RIGHTS_RESERVED;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished = false;

    @Column(name = "published_at")
    private java.time.Instant publishedAt;

    @Column(name = "custom_attribution", columnDefinition = "TEXT")
    private String customAttribution;

    public Workspace() {
    }

    public Workspace(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.visibility = Visibility.PRIVATE;
    }

    public Workspace(String name, String description, User owner, Visibility visibility) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.visibility = visibility != null ? visibility : Visibility.PRIVATE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<WorkspaceMember> getMembers() {
        return members;
    }

    public void setMembers(List<WorkspaceMember> members) {
        this.members = members;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility != null ? visibility : Visibility.PRIVATE;
    }

    public com.knowledgenetwork.domain.enums.LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(com.knowledgenetwork.domain.enums.LicenseType licenseType) {
        this.licenseType = licenseType != null ? licenseType : com.knowledgenetwork.domain.enums.LicenseType.ALL_RIGHTS_RESERVED;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public java.time.Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(java.time.Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCustomAttribution() {
        return customAttribution;
    }

    public void setCustomAttribution(String customAttribution) {
        this.customAttribution = customAttribution;
    }

    public void addMember(WorkspaceMember member) {
        this.members.add(member);
        member.setWorkspace(this);
    }

    public void removeMember(WorkspaceMember member) {
        this.members.remove(member);
        member.setWorkspace(null);
    }
}
