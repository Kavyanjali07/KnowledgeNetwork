package com.knowledgenetwork.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Domain Entity representing a Graph Node (Vertex) in the Knowledge Graph.
 * Includes label, dynamic JSONB key-value attributes, entity mapping, position coordinates, and optimistic locking capabilities.
 */
@Entity
@Table(name = "nodes")
public class Node extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_type_id", nullable = false)
    private NodeType nodeType;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> attributes = new HashMap<>();

    @Column(name = "position_x", nullable = false)
    private Double positionX = 0.0;

    @Column(name = "position_y", nullable = false)
    private Double positionY = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private Visibility visibility = Visibility.PRIVATE;

    @ElementCollection
    @CollectionTable(name = "node_tags", joinColumns = @JoinColumn(name = "node_id"))
    @Column(name = "tag", nullable = false, length = 50)
    private Set<String> tags = new HashSet<>();

    public Node() {
    }

    public Node(Workspace workspace, NodeType nodeType, String label, Map<String, Object> attributes) {
        this.workspace = workspace;
        this.nodeType = nodeType;
        this.label = label;
        if (attributes != null) {
            this.attributes = attributes;
        }
    }

    public Node(Workspace workspace, NodeType nodeType, String label, Map<String, Object> attributes, Double positionX, Double positionY) {
        this.workspace = workspace;
        this.nodeType = nodeType;
        this.label = label;
        if (attributes != null) {
            this.attributes = attributes;
        }
        if (positionX != null) {
            this.positionX = positionX;
        }
        if (positionY != null) {
            this.positionY = positionY;
        }
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? attributes : new HashMap<>();
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX != null ? positionX : 0.0;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY != null ? positionY : 0.0;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility != null ? visibility : Visibility.PRIVATE;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }
}
