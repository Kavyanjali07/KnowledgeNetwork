package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class GraphTraversalRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    @NotNull(message = "Root Node ID is required")
    private UUID rootNodeId;

    @Min(value = 1, message = "Max depth must be at least 1")
    @Max(value = 10, message = "Max depth cannot exceed 10 hops")
    private int maxDepth = 3;

    private List<String> edgeTypeNames;

    private TraversalDirection direction = TraversalDirection.BOTH;

    public enum TraversalDirection {
        OUTGOING,
        INCOMING,
        BOTH
    }

    public GraphTraversalRequest() {
    }

    public GraphTraversalRequest(UUID workspaceId, UUID rootNodeId, int maxDepth, List<String> edgeTypeNames, TraversalDirection direction) {
        this.workspaceId = workspaceId;
        this.rootNodeId = rootNodeId;
        this.maxDepth = maxDepth;
        this.edgeTypeNames = edgeTypeNames;
        this.direction = direction != null ? direction : TraversalDirection.BOTH;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UUID getRootNodeId() {
        return rootNodeId;
    }

    public void setRootNodeId(UUID rootNodeId) {
        this.rootNodeId = rootNodeId;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public List<String> getEdgeTypeNames() {
        return edgeTypeNames;
    }

    public void setEdgeTypeNames(List<String> edgeTypeNames) {
        this.edgeTypeNames = edgeTypeNames;
    }

    public TraversalDirection getDirection() {
        return direction;
    }

    public void setDirection(TraversalDirection direction) {
        this.direction = direction;
    }
}
