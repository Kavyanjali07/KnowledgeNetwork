package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ConcurrencyConflictException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.GraphMapper;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.domain.payload.request.GraphCreateRequest;
import com.knowledgenetwork.domain.payload.request.GraphUpdateRequest;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GraphService {

    private static final int MAX_PAGE_SIZE = 100;

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final EdgeTypeRepository edgeTypeRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;
    private final GraphMapper graphMapper;

    public GraphService(WorkspaceRepository workspaceRepository,
                        WorkspaceMemberRepository workspaceMemberRepository,
                        UserRepository userRepository,
                        NodeTypeRepository nodeTypeRepository,
                        EdgeTypeRepository edgeTypeRepository,
                        NodeRepository nodeRepository,
                        EdgeRepository edgeRepository,
                        WorkspaceSecurityValidator workspaceSecurityValidator,
                        GraphMapper graphMapper) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
        this.graphMapper = graphMapper;
    }

    @Transactional
    public GraphResponse createGraph(GraphCreateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        String title = request.getTitle() != null ? request.getTitle().trim() : request.getName();
        Visibility visibility = request.getVisibility() != null ? request.getVisibility() : Visibility.PRIVATE;

        Workspace workspace = new Workspace(title, request.getDescription(), owner, visibility);
        String currentUserIdString = currentUserId.toString();
        workspace.setCreatedBy(currentUserIdString);
        workspace.setUpdatedBy(currentUserIdString);

        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember(workspace, owner, WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);

        seedDefaultRelationshipTypes(workspace, currentUserIdString);

        return mapToGraphResponse(workspace);
    }

    @Transactional(readOnly = true)
    public Page<GraphResponse> getGraphs(int page, int size, String sortBy, String directionStr, Visibility visibilityFilter) {
        int validatedPage = Math.max(0, page);
        int validatedSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

        String property = "updatedAt";
        if ("title".equalsIgnoreCase(sortBy) || "name".equalsIgnoreCase(sortBy)) {
            property = "name";
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            property = "createdAt";
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if ("asc".equalsIgnoreCase(directionStr)) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by(direction, property));

        User currentUser = null;
        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            currentUser = userRepository.findById(currentUserId).orElse(null);
        } catch (Exception ignored) {
            // Unauthenticated requests will rely solely on PUBLIC visibility filter in query
        }

        Page<Workspace> workspaces = workspaceRepository.findAccessibleWorkspaces(currentUser, visibilityFilter, pageable);
        return workspaces.map(this::mapToGraphResponse);
    }

    @Transactional(readOnly = true)
    public GraphResponse getGraphById(UUID graphId) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));

        UUID currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
        }

        workspaceSecurityValidator.validateReadAccess(workspace, currentUserId);

        return mapToGraphResponse(workspace);
    }

    @Transactional
    public GraphResponse updateGraph(UUID graphId, GraphUpdateRequest request) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateWriteAccess(workspace, currentUserId);

        if (request.getVersion() != null && !request.getVersion().equals(workspace.getVersion())) {
            throw new ConcurrencyConflictException("Graph version mismatch. Entity was modified concurrently.");
        }

        String updatedTitle = request.getTitle() != null ? request.getTitle().trim() : request.getName();
        if (updatedTitle != null && !updatedTitle.isBlank()) {
            workspace.setName(updatedTitle);
        }

        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }

        if (request.getVisibility() != null) {
            workspace.setVisibility(request.getVisibility());
        }

        workspace.setUpdatedBy(currentUserId.toString());
        workspace = workspaceRepository.save(workspace);

        return mapToGraphResponse(workspace);
    }

    @Transactional
    public void deleteGraph(UUID graphId) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateOwnerAccess(workspace, currentUserId);

        workspace.setDeleted(true);
        workspace.setUpdatedBy(currentUserId.toString());
        workspaceRepository.save(workspace);
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

    private GraphResponse mapToGraphResponse(Workspace workspace) {
        GraphResponse response = graphMapper.toGraphResponse(workspace);
        try {
            response.setNodeCount(nodeRepository.countByWorkspaceAndIsDeletedFalse(workspace));
            response.setEdgeCount(edgeRepository.countByWorkspaceAndIsDeletedFalse(workspace));
        } catch (Exception ignored) {
        }
        return response;
    }
}
