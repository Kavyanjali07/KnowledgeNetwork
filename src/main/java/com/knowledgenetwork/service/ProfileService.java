package com.knowledgenetwork.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.ProfileUpdateRequest;
import com.knowledgenetwork.domain.payload.response.ProfileResponse;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.SocialFollowRepository;
import com.knowledgenetwork.repository.SocialPostRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final SocialPostRepository socialPostRepository;
    private final SocialFollowRepository socialFollowRepository;

    public ProfileService(UserRepository userRepository,
                          WorkspaceRepository workspaceRepository,
                          NodeRepository nodeRepository,
                          EdgeRepository edgeRepository,
                          SocialPostRepository socialPostRepository,
                          SocialFollowRepository socialFollowRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.socialPostRepository = socialPostRepository;
        this.socialFollowRepository = socialFollowRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentProfile() {
        return getProfile(SecurityUtils.getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = getUser(userId);
        List<Workspace> graphs = workspaceRepository.findByOwner(user).stream()
                .filter(workspace -> !workspace.isDeleted())
                .toList();
        return mapProfile(user, graphs);
    }

    @Transactional
    public ProfileResponse updateCurrentProfile(ProfileUpdateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = getUser(currentUserId);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setBio(request.getBio() != null ? request.getBio().trim() : "");
        user.setStatus(normalizeStatus(request.getStatus()));
        user.setUpdatedBy(currentUserId.toString());
        User savedUser = userRepository.save(user);
        List<Workspace> graphs = workspaceRepository.findByOwner(savedUser).stream()
                .filter(workspace -> !workspace.isDeleted())
                .toList();
        return mapProfile(savedUser, graphs);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private ProfileResponse mapProfile(User user, List<Workspace> graphs) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setBio(user.getBio());
        response.setStatus(user.getStatus());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        response.setEmailVerified(user.isEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setGraphs(graphs.stream().map(this::mapWorkspace).toList());
        response.setStatistics(mapStatistics(user, graphs));
        return response;
    }

    private ProfileResponse.ProfileStatistics mapStatistics(User user, List<Workspace> graphs) {
        ProfileResponse.ProfileStatistics statistics = new ProfileResponse.ProfileStatistics();
        statistics.setGraphsOwned(graphs.size());
        statistics.setNodesCreated(graphs.stream().mapToLong(nodeRepository::countByWorkspaceAndIsDeletedFalse).sum());
        statistics.setEdgesAuthored(graphs.stream().mapToLong(edgeRepository::countByWorkspaceAndIsDeletedFalse).sum());
        statistics.setPosts(socialPostRepository.countByAuthorAndIsDeletedFalse(user));
        statistics.setFollowers(socialFollowRepository.countByFollowee(user));
        statistics.setFollowing(socialFollowRepository.countByFollower(user));
        return statistics;
    }

    private WorkspaceResponse mapWorkspace(Workspace workspace) {
        WorkspaceResponse response = new WorkspaceResponse();
        response.setId(workspace.getId());
        response.setName(workspace.getName());
        response.setDescription(workspace.getDescription());
        response.setOwnerId(workspace.getOwner().getId());
        response.setOwnerEmail(workspace.getOwner().getEmail());
        response.setOwnerName(workspace.getOwner().getFirstName() + " " + workspace.getOwner().getLastName());
        response.setCreatedAt(workspace.getCreatedAt());
        response.setUpdatedAt(workspace.getUpdatedAt());
        response.setCreatedBy(workspace.getCreatedBy());
        response.setUpdatedBy(workspace.getUpdatedBy());
        response.setVersion(workspace.getVersion());
        response.setDeleted(workspace.isDeleted());
        return response;
    }

    private String normalizeStatus(String status) {
        String normalized = status != null ? status.trim().toLowerCase() : "";
        return normalized.isBlank() ? "online" : normalized;
    }
}
