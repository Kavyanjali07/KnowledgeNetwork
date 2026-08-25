package com.knowledgenetwork.domain.payload.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProfileResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String status;
    private String role;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;
    private ProfileStatistics statistics;
    private List<WorkspaceResponse> graphs;

    public ProfileResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
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

    public ProfileStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(ProfileStatistics statistics) {
        this.statistics = statistics;
    }

    public List<WorkspaceResponse> getGraphs() {
        return graphs;
    }

    public void setGraphs(List<WorkspaceResponse> graphs) {
        this.graphs = graphs;
    }

    public static class ProfileStatistics {
        private long graphsOwned;
        private long nodesCreated;
        private long edgesAuthored;
        private long posts;
        private long followers;
        private long following;

        public long getGraphsOwned() {
            return graphsOwned;
        }

        public void setGraphsOwned(long graphsOwned) {
            this.graphsOwned = graphsOwned;
        }

        public long getNodesCreated() {
            return nodesCreated;
        }

        public void setNodesCreated(long nodesCreated) {
            this.nodesCreated = nodesCreated;
        }

        public long getEdgesAuthored() {
            return edgesAuthored;
        }

        public void setEdgesAuthored(long edgesAuthored) {
            this.edgesAuthored = edgesAuthored;
        }

        public long getPosts() {
            return posts;
        }

        public void setPosts(long posts) {
            this.posts = posts;
        }

        public long getFollowers() {
            return followers;
        }

        public void setFollowers(long followers) {
            this.followers = followers;
        }

        public long getFollowing() {
            return following;
        }

        public void setFollowing(long following) {
            this.following = following;
        }
    }
}
