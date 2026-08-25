package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EdgeTypeUpdateRequest {

    @NotBlank(message = "Edge type name is required")
    @Size(max = 100, message = "Edge type name cannot exceed 100 characters")
    private String name;

    private Boolean isDirected;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;

    public EdgeTypeUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDirected() {
        return isDirected;
    }

    public void setIsDirected(Boolean isDirected) {
        this.isDirected = isDirected;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
