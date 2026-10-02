package com.knowledgenetwork.domain.payload.response;

import java.util.List;
import java.util.UUID;

public class PublicCreatorProfileResponse {

    private UUID id;
    private String username;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private long totalPublicNetworks;
    private List<GraphResponse> publicNetworks;
    private List<String> topConcepts;

    public PublicCreatorProfileResponse() {
    }

    public PublicCreatorProfileResponse(UUID id, String username, String fullName, String bio, String avatarUrl,
                                        long totalPublicNetworks, List<GraphResponse> publicNetworks, List<String> topConcepts) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.totalPublicNetworks = totalPublicNetworks;
        this.publicNetworks = publicNetworks;
        this.topConcepts = topConcepts;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public long getTotalPublicNetworks() { return totalPublicNetworks; }
    public void setTotalPublicNetworks(long totalPublicNetworks) { this.totalPublicNetworks = totalPublicNetworks; }
    public List<GraphResponse> getPublicNetworks() { return publicNetworks; }
    public void setPublicNetworks(List<GraphResponse> publicNetworks) { this.publicNetworks = publicNetworks; }
    public List<String> getTopConcepts() { return topConcepts; }
    public void setTopConcepts(List<String> topConcepts) { this.topConcepts = topConcepts; }
}
