package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public class NodeUpdateRequest {

    private UUID nodeTypeId;

    @Size(max = 255, message = "Label cannot exceed 255 characters")
    private String label;

    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;

    private Double positionX;
    private Double positionY;

    private Map<String, Object> attributes;

    private Long version;

    public NodeUpdateRequest() {
    }

    public NodeUpdateRequest(UUID nodeTypeId, String label, Map<String, Object> attributes, Long version) {
        this.nodeTypeId = nodeTypeId;
        this.label = label;
        this.attributes = attributes;
        this.version = version;
    }

    public UUID getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(UUID nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return label;
    }

    public void setTitle(String title) {
        this.label = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
