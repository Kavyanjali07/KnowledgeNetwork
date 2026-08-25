package com.knowledgenetwork.domain.payload.request;

import com.knowledgenetwork.domain.model.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GraphUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private Visibility visibility;

    private Long version;

    public GraphUpdateRequest() {
    }

    public GraphUpdateRequest(String title, String description, Visibility visibility, Long version) {
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return title;
    }

    public void setName(String name) {
        this.title = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
