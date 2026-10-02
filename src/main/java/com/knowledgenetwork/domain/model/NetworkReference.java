package com.knowledgenetwork.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "network_references", uniqueConstraints = {
        @UniqueConstraint(name = "uk_network_reference", columnNames = {"source_workspace_id", "source_node_id", "target_workspace_id", "target_node_id"})
})
public class NetworkReference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_workspace_id", nullable = false)
    private Workspace sourceWorkspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_node_id")
    private Node sourceNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_workspace_id", nullable = false)
    private Workspace targetWorkspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_node_id")
    private Node targetNode;

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType = "CITES";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public NetworkReference() {
    }

    public NetworkReference(Workspace sourceWorkspace, Node sourceNode, Workspace targetWorkspace, Node targetNode, User createdBy) {
        this.sourceWorkspace = sourceWorkspace;
        this.sourceNode = sourceNode;
        this.targetWorkspace = targetWorkspace;
        this.targetNode = targetNode;
        this.createdBy = createdBy;
        this.referenceType = "CITES";
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Workspace getSourceWorkspace() {
        return sourceWorkspace;
    }

    public void setSourceWorkspace(Workspace sourceWorkspace) {
        this.sourceWorkspace = sourceWorkspace;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Workspace getTargetWorkspace() {
        return targetWorkspace;
    }

    public void setTargetWorkspace(Workspace targetWorkspace) {
        this.targetWorkspace = targetWorkspace;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(Node targetNode) {
        this.targetNode = targetNode;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType != null ? referenceType : "CITES";
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
