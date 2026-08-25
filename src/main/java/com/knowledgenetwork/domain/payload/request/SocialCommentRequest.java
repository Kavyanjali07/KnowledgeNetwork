package com.knowledgenetwork.domain.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class SocialCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content cannot exceed 5000 characters")
    private String content;

    private UUID parentCommentId;

    public SocialCommentRequest() {
    }

    public SocialCommentRequest(String content, UUID parentCommentId) {
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}