package com.knowledgenetwork.domain.payload.response;

import java.util.List;
import java.util.UUID;

public class SocialSummaryResponse {

    private UUID postId;
    private long likes;
    private long favorites;
    private long comments;
    private List<String> tags;

    public SocialSummaryResponse() {
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getFavorites() {
        return favorites;
    }

    public void setFavorites(long favorites) {
        this.favorites = favorites;
    }

    public long getComments() {
        return comments;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}