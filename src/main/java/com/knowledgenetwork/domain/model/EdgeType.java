package com.knowledgenetwork.domain.model;

import jakarta.persistence.*;

/**
 * Domain Entity representing a classification type for graph edges (e.g., DEPENDS_ON, USES).
 */
@Entity
@Table(name = "edge_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workspace_edgetype", columnNames = {"workspace_id", "name"})
})
public class EdgeType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_directed", nullable = false)
    private boolean isDirected = true;

    public EdgeType() {
    }

    public EdgeType(Workspace workspace, String name, boolean isDirected) {
        this.workspace = workspace;
        this.name = name;
        this.isDirected = isDirected;
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

    public boolean isDirected() {
        return isDirected;
    }

    public void setDirected(boolean directed) {
        isDirected = directed;
    }
}
