package com.knowledgenetwork.util;

import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.*;

import java.util.HashMap;

public class TestFixtures {

    public static User createUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("$2a$10$wN35F7L1d.VqFqZ2L8j.8e1rJ8G6.VvB1zJ8k9l0m1n2o3p4q5r6s"); // BCrypt hashed "Password123!"
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(true);
        return user;
    }

    public static Workspace createWorkspace(String name, User owner) {
        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspace.setDescription("Test Workspace Description");
        workspace.setVisibility(Visibility.PRIVATE);
        workspace.setOwner(owner);
        return workspace;
    }

    public static WorkspaceMember createWorkspaceMember(Workspace workspace, User user, WorkspaceRole role) {
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(role);
        return member;
    }

    public static NodeType createNodeType(Workspace workspace, String name) {
        return new NodeType(workspace, name, "#22D3EE", "brain");
    }

    public static Node createNode(Workspace workspace, NodeType nodeType, String label, Double x, Double y) {
        return new Node(workspace, nodeType, label, new HashMap<>(), x, y);
    }

    public static Node createNode(Workspace workspace, String label, Double x, Double y) {
        NodeType nodeType = createNodeType(workspace, "Concept_" + java.util.UUID.randomUUID().toString().substring(0, 8));
        return createNode(workspace, nodeType, label, x, y);
    }

    public static EdgeType createEdgeType(Workspace workspace, String name) {
        return new EdgeType(workspace, name, true);
    }

    public static Edge createEdge(Workspace workspace, EdgeType edgeType, Node source, Node target) {
        return new Edge(workspace, edgeType, source, target, 1.0, new HashMap<>());
    }

    public static Edge createEdge(Workspace workspace, Node source, Node target, String relationshipType) {
        EdgeType edgeType = createEdgeType(workspace, relationshipType + "_" + java.util.UUID.randomUUID().toString().substring(0, 8));
        return createEdge(workspace, edgeType, source, target);
    }

    public static SocialPost createSocialPost(Workspace workspace, User author, String content) {
        return new SocialPost(workspace, author, content, "GRAPH", workspace.getId());
    }

    public static Notification createNotification(Workspace workspace, User recipient, String type, String message) {
        return new Notification(workspace, recipient, type, message);
    }
}
