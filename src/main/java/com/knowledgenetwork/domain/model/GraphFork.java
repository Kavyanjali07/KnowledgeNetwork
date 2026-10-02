package com.knowledgenetwork.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "graph_forks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workspace_fork_name", columnNames = {"workspace_id", "name"})
})
public class GraphFork {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_version_id", nullable = false)
    private GraphVersion sourceVersion;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_workspace_id")
    private Workspace sourceWorkspace;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_license", length = 50)
    private com.knowledgenetwork.domain.enums.LicenseType sourceLicense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_creator_id")
    private User originalCreator;

    @Column(name = "is_derivative", nullable = false)
    private boolean isDerivative = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public GraphVersion getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(GraphVersion sourceVersion) {
        this.sourceVersion = sourceVersion;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Workspace getSourceWorkspace() {
        return sourceWorkspace;
    }

    public void setSourceWorkspace(Workspace sourceWorkspace) {
        this.sourceWorkspace = sourceWorkspace;
    }

    public com.knowledgenetwork.domain.enums.LicenseType getSourceLicense() {
        return sourceLicense;
    }

    public void setSourceLicense(com.knowledgenetwork.domain.enums.LicenseType sourceLicense) {
        this.sourceLicense = sourceLicense;
    }

    public User getOriginalCreator() {
        return originalCreator;
    }

    public void setOriginalCreator(User originalCreator) {
        this.originalCreator = originalCreator;
    }

    public boolean isDerivative() {
        return isDerivative;
    }

    public void setDerivative(boolean derivative) {
        isDerivative = derivative;
    }
}
