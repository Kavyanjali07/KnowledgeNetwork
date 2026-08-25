package com.knowledgenetwork.domain.payload.response;

import java.util.List;
import java.util.UUID;

public class GraphTraversalResponse {

    private UUID workspaceId;
    private UUID rootNodeId;
    private int maxDepth;
    private int totalNodesFound;
    private int totalEdgesFound;
    private List<NodeResponse> nodes;
    private List<EdgeResponse> edges;

    public GraphTraversalResponse() {
    }

    public GraphTraversalResponse(UUID workspaceId, UUID rootNodeId, int maxDepth, List<NodeResponse> nodes, List<EdgeResponse> edges) {
        this.workspaceId = workspaceId;
        this.rootNodeId = rootNodeId;
        this.maxDepth = maxDepth;
        this.nodes = nodes;
        this.edges = edges;
        this.totalNodesFound = nodes != null ? nodes.size() : 0;
        this.totalEdgesFound = edges != null ? edges.size() : 0;
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

    public int getTotalNodesFound() {
        return totalNodesFound;
    }

    public void setTotalNodesFound(int totalNodesFound) {
        this.totalNodesFound = totalNodesFound;
    }

    public int getTotalEdgesFound() {
        return totalEdgesFound;
    }

    public void setTotalEdgesFound(int totalEdgesFound) {
        this.totalEdgesFound = totalEdgesFound;
    }

    public List<NodeResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeResponse> nodes) {
        this.nodes = nodes;
        this.totalNodesFound = nodes != null ? nodes.size() : 0;
    }

    public List<EdgeResponse> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeResponse> edges) {
        this.edges = edges;
        this.totalEdgesFound = edges != null ? edges.size() : 0;
    }
}
