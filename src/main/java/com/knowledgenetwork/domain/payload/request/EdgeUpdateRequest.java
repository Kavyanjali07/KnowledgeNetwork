package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public class EdgeUpdateRequest {

    private UUID edgeTypeId;
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
