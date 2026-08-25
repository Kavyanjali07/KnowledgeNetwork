package com.knowledgenetwork.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain Entity representing a Graph Edge (Relationship) connecting two Node vertices.
 * Includes relationship properties, weight, JSONB dynamic attributes, and optimistic locking capabilities.
 */
@Entity
@Table(name = "edges")
public class Edge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "edge_type_id", nullable = false)
    private EdgeType edgeType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_node_id", nullable = false)
    private Node sourceNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_node_id", nullable = false)
    private Node targetNode;

    @Column(name = "weight")
    private Double weight = 1.0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> attributes = new HashMap<>();

    public Edge() {
    }

    public Edge(Workspace workspace, EdgeType edgeType, Node sourceNode, Node targetNode, Double weight, Map<String, Object> attributes) {
        this.workspace = workspace;
        this.edgeType = edgeType;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.weight = weight != null ? weight : 1.0;
        if (attributes != null) {
            this.attributes = attributes;
        }
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(EdgeType edgeType) {
        this.edgeType = edgeType;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(Node targetNode) {
        this.targetNode = targetNode;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? attributes : new HashMap<>();
    }
}
