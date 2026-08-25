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

    public void addMember(WorkspaceMember member) {
        this.members.add(member);
        member.setWorkspace(this);
    }

    public void removeMember(WorkspaceMember member) {
        this.members.remove(member);
        member.setWorkspace(null);
    }
}
