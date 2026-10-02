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
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
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
    private final com.knowledgenetwork.repository.GraphForkRepository graphForkRepository;
    private final com.knowledgenetwork.repository.NetworkReferenceRepository networkReferenceRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;
    private final GraphMapper graphMapper;
    private final AuditLogService auditLogService;

    public GraphService(WorkspaceRepository workspaceRepository,
                        WorkspaceMemberRepository workspaceMemberRepository,
                        UserRepository userRepository,
                        NodeTypeRepository nodeTypeRepository,
                        EdgeTypeRepository edgeTypeRepository,
                        NodeRepository nodeRepository,
                        EdgeRepository edgeRepository,
                        com.knowledgenetwork.repository.GraphForkRepository graphForkRepository,
                        com.knowledgenetwork.repository.NetworkReferenceRepository networkReferenceRepository,
                        WorkspaceSecurityValidator workspaceSecurityValidator,
                        GraphMapper graphMapper,
                        AuditLogService auditLogService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.graphForkRepository = graphForkRepository;
        this.networkReferenceRepository = networkReferenceRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
        this.graphMapper = graphMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public GraphResponse createGraph(GraphCreateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        String title = request.getTitle() != null ? request.getTitle().trim() : request.getName();
        Visibility visibility = request.getVisibility() != null ? request.getVisibility() : Visibility.PRIVATE;

        Workspace workspace = new Workspace(title, request.getDescription(), owner, visibility);
        if (request.getLicenseType() != null) {
            workspace.setLicenseType(request.getLicenseType());
        }
        if (request.isPublished()) {
            workspace.setPublished(true);
            workspace.setPublishedAt(java.time.Instant.now());
            workspace.setVisibility(Visibility.PUBLIC);
        }
        String currentUserIdString = currentUserId.toString();
        workspace.setCreatedBy(currentUserIdString);
        workspace.setUpdatedBy(currentUserIdString);

        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember(workspace, owner, WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);

        seedDefaultRelationshipTypes(workspace, currentUserIdString);

        auditLogService.recordEvent(
                AuditAction.GRAPH_CREATED,
                AuditEntityType.GRAPH,
                workspace.getId(),
                workspace.getId(),
                java.util.Map.of("title", workspace.getName())
        );

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

        if (request.getLicenseType() != null) {
            workspace.setLicenseType(request.getLicenseType());
        }

        if (request.getIsPublished() != null) {
            boolean pub = request.getIsPublished();
            workspace.setPublished(pub);
            if (pub) {
                if (workspace.getPublishedAt() == null) {
                    workspace.setPublishedAt(java.time.Instant.now());
                }
                workspace.setVisibility(Visibility.PUBLIC);
            }
        }

        if (request.getCustomAttribution() != null) {
            workspace.setCustomAttribution(request.getCustomAttribution());
        }

        workspace.setUpdatedBy(currentUserId.toString());
        workspace = workspaceRepository.save(workspace);

        auditLogService.recordEvent(
                AuditAction.GRAPH_UPDATED,
                AuditEntityType.GRAPH,
                workspace.getId(),
                workspace.getId(),
                java.util.Map.of("title", workspace.getName())
        );

        return mapToGraphResponse(workspace);
    }

    @Transactional
    public GraphResponse publishGraph(UUID graphId, com.knowledgenetwork.domain.enums.LicenseType licenseType, String customAttribution) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateOwnerAccess(workspace, currentUserId);

        workspace.setPublished(true);
        if (workspace.getPublishedAt() == null) {
            workspace.setPublishedAt(java.time.Instant.now());
        }
        if (licenseType != null) {
            workspace.setLicenseType(licenseType);
        }
        if (customAttribution != null) {
            workspace.setCustomAttribution(customAttribution);
        }
        workspace.setVisibility(Visibility.PUBLIC);
        workspace.setUpdatedBy(currentUserId.toString());

        workspace = workspaceRepository.save(workspace);

        auditLogService.recordEvent(
                AuditAction.GRAPH_PUBLISHED,
                AuditEntityType.GRAPH,
                workspace.getId(),
                workspace.getId(),
                java.util.Map.of("title", workspace.getName(), "licenseType", workspace.getLicenseType().name())
        );

        return mapToGraphResponse(workspace);
    }

    @Transactional
    public GraphResponse unpublishGraph(UUID graphId) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateOwnerAccess(workspace, currentUserId);

        workspace.setPublished(false);
        workspace.setVisibility(Visibility.PRIVATE);
        workspace.setUpdatedBy(currentUserId.toString());

        workspace = workspaceRepository.save(workspace);

        auditLogService.recordEvent(
                AuditAction.GRAPH_UNPUBLISHED,
                AuditEntityType.GRAPH,
                workspace.getId(),
                workspace.getId(),
                java.util.Map.of("title", workspace.getName())
        );

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

        auditLogService.recordEvent(
                AuditAction.GRAPH_DELETED,
                AuditEntityType.GRAPH,
                workspace.getId(),
                workspace.getId(),
                java.util.Map.of("title", workspace.getName())
        );
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
                new EdgeType(workspace, "RELATED_TO", true),
                new EdgeType(workspace, "DEPENDS_ON", true),
                new EdgeType(workspace, "PART_OF", true),
                new EdgeType(workspace, "PREREQUISITE_OF", true),
                new EdgeType(workspace, "CAUSES", true),
                new EdgeType(workspace, "SUPPORTS", true),
                new EdgeType(workspace, "CONTRADICTS", true),
                new EdgeType(workspace, "REFERENCES", true)
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
            response.setDerivativeCount(graphForkRepository.countBySourceWorkspace(workspace));
            response.setReferenceCount(networkReferenceRepository.countByTargetWorkspace(workspace));
        } catch (Exception ignored) {
        }
        return response;
    }
}
