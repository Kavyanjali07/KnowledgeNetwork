package com.knowledgenetwork.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.domain.payload.request.WorkspaceCreateRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceMemberAddRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceUpdateRequest;
import com.knowledgenetwork.domain.payload.response.WorkspaceMemberResponse;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final EdgeTypeRepository edgeTypeRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository workspaceMemberRepository,
                            UserRepository userRepository,
                            NodeTypeRepository nodeTypeRepository,
                            EdgeTypeRepository edgeTypeRepository,
                            WorkspaceSecurityValidator workspaceSecurityValidator) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
    }

    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        Workspace workspace = new Workspace(request.getName(), request.getDescription(), owner, request.getVisibility());
        String currentUserIdString = currentUserId.toString();
        workspace.setCreatedBy(currentUserIdString);
        workspace.setUpdatedBy(currentUserIdString);
        workspace = workspaceRepository.save(workspace);
        WorkspaceMember member = new WorkspaceMember(workspace, owner, WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);
        seedDefaultRelationshipTypes(workspace, currentUserIdString);
        return mapWorkspace(workspace);
    }

    private void seedDefaultRelationshipTypes(Workspace workspace, String creatorId) {
        List.of(
                new NodeType(workspace, "Concept", "#22D3EE", "brain"),
                new NodeType(workspace, "Document", "#A78BFA", "file-text"),
                new NodeType(workspace, "Person", "#6EE7B7", "user"),
                new NodeType(workspace, "Decision", "#FCD34D", "check-circle")
        ).forEach(nodeType -> {
            nodeType.setCreatedBy(creatorId);
            nodeType.setUpdatedBy(creatorId);
            nodeTypeRepository.save(nodeType);
        });
        List.of(
                new EdgeType(workspace, "Related to", true),
                new EdgeType(workspace, "Depends on", true),
                new EdgeType(workspace, "References", true)
        ).forEach(edgeType -> {
            edgeType.setCreatedBy(creatorId);
            edgeType.setUpdatedBy(creatorId);
            edgeTypeRepository.save(edgeType);
        });
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesForCurrentUser() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        List<Workspace> owned = workspaceRepository.findByOwner(user).stream()
                .filter(w -> !w.isDeleted())
                .toList();
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUser(user);
        List<Workspace> memberWorkspaces = memberships.stream()
                .map(WorkspaceMember::getWorkspace)
                .filter(w -> !w.isDeleted())
                .toList();
        Set<UUID> seenIds = new HashSet<>();
        List<Workspace> allWorkspaces = new ArrayList<>();
        for (Workspace ws : owned) {
            seenIds.add(ws.getId());
            allWorkspaces.add(ws);
        }
        for (Workspace ws : memberWorkspaces) {
            if (!seenIds.contains(ws.getId())) {
                seenIds.add(ws.getId());
                allWorkspaces.add(ws);
            }
        }
        return allWorkspaces.stream().map(this::mapWorkspace).toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID id) {
        Workspace workspace = getWorkspace(id);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return mapWorkspace(workspace);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, WorkspaceUpdateRequest request) {
        Workspace workspace = getWorkspace(id);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        if (request.getVersion() == null || !request.getVersion().equals(workspace.getVersion())) {
            throw new BusinessException("Optimistic locking failure: version mismatch");
        }
        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            workspace.setVisibility(request.getVisibility());
        }
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        workspace.setUpdatedBy(currentUserId);
        workspace = workspaceRepository.save(workspace);
        return mapWorkspace(workspace);
    }

    @Transactional
    public void deleteWorkspace(UUID id) {
        Workspace workspace = getWorkspace(id);
        workspaceSecurityValidator.validateOwnerAccess(workspace, SecurityUtils.getCurrentUserId());
        workspace.setDeleted(true);
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        workspace.setUpdatedBy(currentUserId);
        workspaceRepository.save(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(UUID workspaceId) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return workspaceMemberRepository.findByWorkspace(workspace).stream()
                .map(this::mapWorkspaceMember)
                .toList();
    }

    @Transactional
    public WorkspaceMemberResponse addMember(UUID workspaceId, WorkspaceMemberAddRequest request) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateOwnerAccess(workspace, SecurityUtils.getCurrentUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        if (workspaceMemberRepository.existsByWorkspaceAndUser(workspace, user)) {
            throw new BusinessException("User is already a member of this workspace");
        }
        WorkspaceMember member = new WorkspaceMember(workspace, user, request.getRole());
        workspaceMemberRepository.save(member);
        return mapWorkspaceMember(member);
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID memberId) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateOwnerAccess(workspace, SecurityUtils.getCurrentUserId());
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkspaceMember", "id", memberId));
        if (!member.getWorkspace().getId().equals(workspace.getId())) {
            throw new BusinessException("Member does not belong to this workspace");
        }
        workspaceMemberRepository.delete(member);
    }

    private Workspace getWorkspace(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", id));
    }

    private WorkspaceResponse mapWorkspace(Workspace workspace) {
        WorkspaceResponse response = new WorkspaceResponse();
        response.setId(workspace.getId());
        response.setName(workspace.getName());
        response.setTitle(workspace.getName());
        response.setDescription(workspace.getDescription());
        response.setVisibility(workspace.getVisibility());
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

    private WorkspaceMemberResponse mapWorkspaceMember(WorkspaceMember member) {
        WorkspaceMemberResponse response = new WorkspaceMemberResponse();
        response.setId(member.getId());
        response.setWorkspaceId(member.getWorkspace().getId());
        response.setUserId(member.getUser().getId());
        response.setUserEmail(member.getUser().getEmail());
        response.setUserName(member.getUser().getFirstName() + " " + member.getUser().getLastName());
        response.setRole(member.getRole());
        response.setJoinedAt(member.getJoinedAt());
        return response;
    }
}

