package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public class EdgeUpdateRequest {

    private UUID edgeTypeId;
    private String relationshipType;

    @Size(max = 255, message = "Label must not exceed 255 characters")
    private String label;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Double weight;
    private Map<String, Object> attributes;

    @NotNull(message = "Version is required for optimistic locking concurrency control")
    private Long version;

    public EdgeUpdateRequest() {
    }

    public EdgeUpdateRequest(UUID edgeTypeId, Double weight, Map<String, Object> attributes, Long version) {
        this.edgeTypeId = edgeTypeId;
        this.weight = weight;
        this.attributes = attributes;
        this.version = version;
    }

    public UUID getEdgeTypeId() {
        return edgeTypeId;
    }

    public void setEdgeTypeId(UUID edgeTypeId) {
        this.edgeTypeId = edgeTypeId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        this.attributes = attributes;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
