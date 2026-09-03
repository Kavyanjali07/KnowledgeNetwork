package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Unified search result DTO that can represent both graph (workspace) and node results.
 */
public class SearchResultResponse {

    public enum ResultType {
        GRAPH, NODE
    }

    private ResultType resultType;
    private UUID id;
    private String title;
    private String description;
    private UUID graphId;
    private String graphTitle;

    // Node-specific fields
    private String nodeTypeName;
    private String nodeTypeColor;
    private String nodeTypeIcon;
    private Double positionX;
    private Double positionY;

    // Graph-specific fields
    private String visibility;
    private long nodeCount;
    private long edgeCount;

    // Common metadata
    private Instant createdAt;
    private Instant updatedAt;

    public SearchResultResponse() {
    }

    // --- Factory methods ---

    public static SearchResultResponse fromGraph(
            UUID id, String name, String description, String visibility,
            long nodeCount, long edgeCount, Instant createdAt, Instant updatedAt) {
        SearchResultResponse r = new SearchResultResponse();
        r.resultType = ResultType.GRAPH;
        r.id = id;
        r.title = name;
        r.description = description;
        r.graphId = id;
        r.graphTitle = name;
        r.visibility = visibility;
        r.nodeCount = nodeCount;
        r.edgeCount = edgeCount;
        r.createdAt = createdAt;
        r.updatedAt = updatedAt;
        return r;
    }

    public static SearchResultResponse fromNode(
            UUID id, String label, String description, UUID graphId, String graphTitle,
            String nodeTypeName, String nodeTypeColor, String nodeTypeIcon,
            Double positionX, Double positionY,
            Instant createdAt, Instant updatedAt) {
        SearchResultResponse r = new SearchResultResponse();
        r.resultType = ResultType.NODE;
        r.id = id;
        r.title = label;
        r.description = description;
        r.graphId = graphId;
        r.graphTitle = graphTitle;
        r.nodeTypeName = nodeTypeName;
        r.nodeTypeColor = nodeTypeColor;
        r.nodeTypeIcon = nodeTypeIcon;
        r.positionX = positionX;
        r.positionY = positionY;
        r.createdAt = createdAt;
        r.updatedAt = updatedAt;
        return r;
    }

    // --- Getters and Setters ---

    public ResultType getResultType() { return resultType; }
    public void setResultType(ResultType resultType) { this.resultType = resultType; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getGraphId() { return graphId; }
    public void setGraphId(UUID graphId) { this.graphId = graphId; }

    public String getGraphTitle() { return graphTitle; }
    public void setGraphTitle(String graphTitle) { this.graphTitle = graphTitle; }

    public String getNodeTypeName() { return nodeTypeName; }
    public void setNodeTypeName(String nodeTypeName) { this.nodeTypeName = nodeTypeName; }

    public String getNodeTypeColor() { return nodeTypeColor; }
    public void setNodeTypeColor(String nodeTypeColor) { this.nodeTypeColor = nodeTypeColor; }

    public String getNodeTypeIcon() { return nodeTypeIcon; }
    public void setNodeTypeIcon(String nodeTypeIcon) { this.nodeTypeIcon = nodeTypeIcon; }

    public Double getPositionX() { return positionX; }
    public void setPositionX(Double positionX) { this.positionX = positionX; }

    public Double getPositionY() { return positionY; }
    public void setPositionY(Double positionY) { this.positionY = positionY; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public long getNodeCount() { return nodeCount; }
    public void setNodeCount(long nodeCount) { this.nodeCount = nodeCount; }

    public long getEdgeCount() { return edgeCount; }
    public void setEdgeCount(long edgeCount) { this.edgeCount = edgeCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
