package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SocialCommentResponse {

    private UUID id;
    private UUID workspaceId;
    private UUID postId;
    private SocialUserSummary author;
    private String content;
    private UUID parentCommentId;
    private List<SocialCommentResponse> replies;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private boolean isDeleted;

    public SocialCommentResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public SocialUserSummary getAuthor() {
        return author;
    }

    public void setAuthor(SocialUserSummary author) {
        this.author = author;
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

    public List<SocialCommentResponse> getReplies() {
        return replies;
    }

    public void setReplies(List<SocialCommentResponse> replies) {
        this.replies = replies;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}