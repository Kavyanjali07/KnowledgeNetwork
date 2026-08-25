package com.knowledgenetwork.domain.model;

import jakarta.persistence.*;

/**
 * Domain Entity representing a classification type for graph nodes (e.g., Concept, Document, Technology).
 */
@Entity
@Table(name = "node_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workspace_nodetype", columnNames = {"workspace_id", "name"})
})
public class NodeType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "color_code", length = 10)
    private String colorCode = "#3B82F6";

    @Column(name = "icon", length = 50)
    private String icon = "default-node";

    public NodeType() {
    }

    public NodeType(Workspace workspace, String name, String colorCode, String icon) {
        this.workspace = workspace;
        this.name = name;
        this.colorCode = colorCode;
        this.icon = icon;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
