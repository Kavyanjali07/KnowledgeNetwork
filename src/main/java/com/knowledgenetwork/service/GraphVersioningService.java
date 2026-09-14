package com.knowledgenetwork.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.GraphFork;
import com.knowledgenetwork.domain.model.GraphForkOwner;
import com.knowledgenetwork.domain.model.GraphSnapshot;
import com.knowledgenetwork.domain.model.GraphVersion;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.domain.payload.response.GraphForkResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.GraphForkOwnerRepository;
import com.knowledgenetwork.repository.GraphForkRepository;
import com.knowledgenetwork.repository.GraphSnapshotRepository;
import com.knowledgenetwork.repository.GraphVersionRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;

@Service
public class GraphVersioningService {

    private final GraphSnapshotRepository graphSnapshotRepository;
    private final GraphVersionRepository graphVersionRepository;
    private final GraphForkRepository graphForkRepository;
    private final GraphForkOwnerRepository graphForkOwnerRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final EdgeTypeRepository edgeTypeRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;
    private final AuditLogService auditLogService;

    public GraphVersioningService(GraphSnapshotRepository graphSnapshotRepository,
                                    GraphVersionRepository graphVersionRepository,
                                    GraphForkRepository graphForkRepository,
                                    GraphForkOwnerRepository graphForkOwnerRepository,
                                    NodeRepository nodeRepository,
                                    EdgeRepository edgeRepository,
                                    UserRepository userRepository,
                                    WorkspaceRepository workspaceRepository,
                                    NodeTypeRepository nodeTypeRepository,
                                    EdgeTypeRepository edgeTypeRepository,
                                    WorkspaceMemberRepository workspaceMemberRepository,
                                    WorkspaceSecurityValidator workspaceSecurityValidator,
                                    AuditLogService auditLogService) {
        this.graphSnapshotRepository = graphSnapshotRepository;
        this.graphVersionRepository = graphVersionRepository;
        this.graphForkRepository = graphForkRepository;
        this.graphForkOwnerRepository = graphForkOwnerRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public GraphVersion createSnapshot(UUID workspaceId, String label, String description) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        long nextVersionNumber = graphVersionRepository.findTopByWorkspaceOrderByVersionNumberDesc(workspace)
                .map(GraphVersion::getVersionNumber)
                .orElse(0L) + 1;

        List<Node> nodes = nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<Edge> edges = edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged()).getContent();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nodes", nodes.stream().map(this::serializeNode).toList());
        payload.put("edges", edges.stream().map(this::serializeEdge).toList());

        GraphSnapshot snapshot = new GraphSnapshot();
        snapshot.setWorkspace(workspace);
        snapshot.setName(label);
        snapshot.setDescription(description);
        snapshot.setSnapshotData(payload);
        snapshot.setNodeCount(nodes.size());
        snapshot.setEdgeCount(edges.size());
        snapshot.setCreatedAt(Instant.now());
        snapshot.setCreatedBy(currentUserId);
        snapshot = graphSnapshotRepository.save(snapshot);

        GraphVersion version = new GraphVersion();
        version.setWorkspace(workspace);
        version.setSnapshot(snapshot);
        version.setVersionNumber(nextVersionNumber);
        version.setLabel(label);
        version.setDescription(description);
        version.setChangeType("SNAPSHOT");
        version.setCreatedAt(Instant.now());
        version.setCreatedBy(currentUserId);
        GraphVersion savedVersion = graphVersionRepository.save(version);

        auditLogService.recordEvent(
                AuditAction.GRAPH_VERSION_CREATED,
                AuditEntityType.GRAPH_VERSION,
                savedVersion.getId(),
                workspace.getId(),
                java.util.Map.of("label", label, "versionNumber", nextVersionNumber)
        );

        return savedVersion;
    }

    @Transactional(readOnly = true)
    public Page<GraphVersion> listHistory(UUID workspaceId, org.springframework.data.domain.Pageable pageable) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return graphVersionRepository.findByWorkspaceOrderByVersionNumberDesc(workspace, pageable);
    }

    @Transactional
    public GraphVersion restoreVersion(UUID workspaceId, UUID versionId) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        GraphVersion targetVersion = graphVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("GraphVersion", "id", versionId));

        if (!targetVersion.getWorkspace().getId().equals(workspace.getId())) {
            throw new BusinessException("Version does not belong to workspace");
        }

        GraphSnapshot snapshot = targetVersion.getSnapshot();
        if (snapshot == null) {
            throw new BusinessException("Version has no snapshot payload");
        }

        restoreGraph(workspace, snapshot.getSnapshotData(), currentUserId);

        long nextVersionNumber = graphVersionRepository.findTopByWorkspaceOrderByVersionNumberDesc(workspace)
                .map(GraphVersion::getVersionNumber)
                .orElse(0L) + 1;

        GraphVersion restoreVersion = new GraphVersion();
        restoreVersion.setWorkspace(workspace);
        restoreVersion.setSnapshot(snapshot);
        restoreVersion.setParentVersion(targetVersion);
        restoreVersion.setVersionNumber(nextVersionNumber);
        restoreVersion.setLabel("restore-" + targetVersion.getVersionNumber());
        restoreVersion.setDescription("Restored from version " + targetVersion.getVersionNumber());
        restoreVersion.setChangeType("RESTORE");
        restoreVersion.setCreatedAt(Instant.now());
        restoreVersion.setCreatedBy(currentUserId);
        GraphVersion savedRestoreVersion = graphVersionRepository.save(restoreVersion);

        auditLogService.recordEvent(
                AuditAction.GRAPH_VERSION_RESTORED,
                AuditEntityType.GRAPH_VERSION,
                savedRestoreVersion.getId(),
                workspace.getId(),
                java.util.Map.of("targetVersionNumber", targetVersion.getVersionNumber(), "restoredVersionNumber", nextVersionNumber)
        );

        return savedRestoreVersion;
    }

    @Transactional
    public GraphForkResponse createFork(UUID workspaceId, UUID sourceVersionId, String name, String description) {
        Workspace sourceWorkspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(sourceWorkspace, SecurityUtils.getCurrentUserId());
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        GraphVersion sourceVersion = graphVersionRepository.findById(sourceVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("GraphVersion", "id", sourceVersionId));
        if (!sourceVersion.getWorkspace().getId().equals(sourceWorkspace.getId())) {
            throw new BusinessException("Version does not belong to workspace");
        }
        if (sourceVersion.getSnapshot() == null) {
            throw new BusinessException("Version has no snapshot payload");
        }
        User owner = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", SecurityUtils.getCurrentUserId()));

        Workspace forkWorkspace = new Workspace(name, description, owner);
        forkWorkspace.setCreatedBy(currentUserId);
        forkWorkspace.setUpdatedBy(currentUserId);
        forkWorkspace = workspaceRepository.save(forkWorkspace);
        workspaceMemberRepository.save(new WorkspaceMember(forkWorkspace, owner, WorkspaceRole.OWNER));
        copyForkGraph(forkWorkspace, sourceVersion.getSnapshot().getSnapshotData(), currentUserId, sourceWorkspace);

        GraphFork fork = new GraphFork();
        fork.setWorkspace(forkWorkspace);
        fork.setSourceVersion(sourceVersion);
        fork.setName(name);
        fork.setDescription(description);
        fork.setCreatedAt(Instant.now());
        fork.setCreatedBy(currentUserId);
        fork = graphForkRepository.save(fork);

        GraphForkOwner ownerAssignment = new GraphForkOwner();
        ownerAssignment.setFork(fork);
        ownerAssignment.setUser(owner);
        ownerAssignment.setRole("OWNER");
        ownerAssignment.setGrantedAt(Instant.now());
        graphForkOwnerRepository.save(ownerAssignment);

        auditLogService.recordEvent(
                AuditAction.GRAPH_FORKED,
                AuditEntityType.GRAPH_VERSION,
                fork.getId(),
                forkWorkspace.getId(),
                java.util.Map.of("sourceWorkspaceId", sourceWorkspace.getId(), "sourceVersionId", sourceVersionId, "name", name)
        );

        GraphForkResponse response = new GraphForkResponse();
        response.setId(fork.getId());
        response.setWorkspaceId(forkWorkspace.getId());
        response.setSourceVersionId(sourceVersionId);
        response.setName(name);
        response.setDescription(description);
        response.setCreatedAt(fork.getCreatedAt());
        response.setCreatedBy(currentUserId);
        return response;
    }

    @Transactional(readOnly = true)
    public GraphVersion getVersion(UUID workspaceId, UUID versionId) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        GraphVersion version = graphVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("GraphVersion", "id", versionId));
        if (!version.getWorkspace().getId().equals(workspace.getId())) {
            throw new BusinessException("Version does not belong to workspace");
        }
        return version;
    }


    private Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(UUID.fromString(workspaceId.toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
    }

    private Map<String, Object> serializeNode(Node node) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", node.getId().toString());
        data.put("label", node.getLabel());
        data.put("attributes", node.getAttributes());
        if (node.getNodeType() != null && node.getNodeType().getId() != null) {
            data.put("nodeTypeId", node.getNodeType().getId().toString());
        }
        if (node.getVisibility() != null) {
            data.put("visibility", node.getVisibility().name());
        }
        data.put("tags", new ArrayList<>(node.getTags()));
        return data;
    }

    private Map<String, Object> serializeEdge(Edge edge) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", edge.getId().toString());
        data.put("sourceNodeId", edge.getSourceNode() != null ? edge.getSourceNode().getId().toString() : null);
        data.put("targetNodeId", edge.getTargetNode() != null ? edge.getTargetNode().getId().toString() : null);
        if (edge.getEdgeType() != null && edge.getEdgeType().getId() != null) {
            data.put("edgeTypeId", edge.getEdgeType().getId().toString());
        }
        data.put("weight", edge.getWeight());
        data.put("attributes", edge.getAttributes());
        return data;
    }

    @SuppressWarnings("unchecked")
    private void restoreGraph(Workspace workspace, Map<String, Object> snapshotData, String currentUserId) {
        List<Map<String, Object>> snapshotNodes = (List<Map<String, Object>>) snapshotData.getOrDefault("nodes", List.of());
        List<Map<String, Object>> snapshotEdges = (List<Map<String, Object>>) snapshotData.getOrDefault("edges", List.of());

        Map<UUID, Node> restoredNodes = new HashMap<>();
        Set<UUID> snapshotNodeIds = new HashSet<>();
        NodeType defaultNodeType = nodeTypeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged())
                .stream().findFirst().orElseThrow(() -> new BusinessException("Workspace has no node types"));

        for (Map<String, Object> data : snapshotNodes) {
            UUID nodeId = UUID.fromString(String.valueOf(data.get("id")));
            snapshotNodeIds.add(nodeId);
            Node node = nodeRepository.findById(nodeId).orElseGet(Node::new);
            node.setId(nodeId);
            node.setWorkspace(workspace);
            node.setNodeType(resolveNodeType(workspace, data, defaultNodeType));
            node.setLabel(String.valueOf(data.get("label")));
            node.setAttributes((Map<String, Object>) data.getOrDefault("attributes", Map.of()));
            node.setVisibility(resolveVisibility(data));
            node.setTags(new HashSet<>((List<String>) data.getOrDefault("tags", List.of())));
            node.setDeleted(false);
            node.setCreatedBy(currentUserId);
            node.setUpdatedBy(currentUserId);
            restoredNodes.put(nodeId, nodeRepository.save(node));
        }

        for (Node node : nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged())) {
            if (!snapshotNodeIds.contains(node.getId())) {
                node.setDeleted(true);
                node.setUpdatedBy(currentUserId);
                nodeRepository.save(node);
            }
        }

        Map<UUID, Edge> existingEdges = new HashMap<>();
        for (Edge edge : edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged()).getContent()) {
            existingEdges.put(edge.getId(), edge);
        }
        Set<UUID> snapshotEdgeIds = new HashSet<>();
        EdgeType defaultEdgeType = edgeTypeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged())
                .stream().findFirst().orElseThrow(() -> new BusinessException("Workspace has no edge types"));

        for (Map<String, Object> data : snapshotEdges) {
            UUID edgeId = UUID.fromString(String.valueOf(data.get("id")));
            UUID sourceId = UUID.fromString(String.valueOf(data.get("sourceNodeId")));
            UUID targetId = UUID.fromString(String.valueOf(data.get("targetNodeId")));
            Node source = restoredNodes.get(sourceId);
            Node target = restoredNodes.get(targetId);
            if (source == null || target == null) {
                throw new BusinessException("Snapshot contains an edge with a missing node");
            }
            snapshotEdgeIds.add(edgeId);
            Edge edge = edgeRepository.findById(edgeId).orElseGet(Edge::new);
            edge.setId(edgeId);
            edge.setWorkspace(workspace);
            edge.setEdgeType(resolveEdgeType(workspace, data, defaultEdgeType));
            edge.setSourceNode(source);
            edge.setTargetNode(target);
            edge.setWeight(data.get("weight") instanceof Number number ? number.doubleValue() : 1.0);
            edge.setAttributes((Map<String, Object>) data.getOrDefault("attributes", Map.of()));
            edge.setDeleted(false);
            edge.setCreatedBy(currentUserId);
            edge.setUpdatedBy(currentUserId);
            edgeRepository.save(edge);
        }

        for (Edge edge : existingEdges.values()) {
            if (!snapshotEdgeIds.contains(edge.getId())) {
                edge.setDeleted(true);
                edge.setUpdatedBy(currentUserId);
                edgeRepository.save(edge);
            }
        }
    }

    private NodeType resolveNodeType(Workspace workspace, Map<String, Object> data, NodeType fallback) {
        Object typeId = data.get("nodeTypeId");
        if (typeId == null) return fallback;
        return nodeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(UUID.fromString(String.valueOf(typeId)), workspace).orElse(fallback);
    }

    private EdgeType resolveEdgeType(Workspace workspace, Map<String, Object> data, EdgeType fallback) {
        Object typeId = data.get("edgeTypeId");
        if (typeId == null) return fallback;
        return edgeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(UUID.fromString(String.valueOf(typeId)), workspace).orElse(fallback);
    }

    private Visibility resolveVisibility(Map<String, Object> data) {
        Object visibility = data.get("visibility");
        if (visibility == null) return Visibility.PRIVATE;
        try {
            return Visibility.valueOf(String.valueOf(visibility));
        } catch (IllegalArgumentException ignored) {
            return Visibility.PRIVATE;
        }
    }

    @SuppressWarnings("unchecked")
    private void copyForkGraph(Workspace forkWorkspace, Map<String, Object> snapshotData,
                               String currentUserId, Workspace sourceWorkspace) {
        List<Map<String, Object>> snapshotNodes = (List<Map<String, Object>>) snapshotData.getOrDefault("nodes", List.of());
        List<Map<String, Object>> snapshotEdges = (List<Map<String, Object>>) snapshotData.getOrDefault("edges", List.of());
        if (snapshotNodes.isEmpty() && snapshotEdges.isEmpty()) {
            return;
        }
        Map<UUID, NodeType> nodeTypes = new HashMap<>();
        for (NodeType sourceType : nodeTypeRepository.findByWorkspaceAndIsDeletedFalse(sourceWorkspace, Pageable.unpaged())) {
            NodeType copy = new NodeType(forkWorkspace, sourceType.getName(), sourceType.getColorCode(), sourceType.getIcon());
            copy.setCreatedBy(currentUserId);
            copy.setUpdatedBy(currentUserId);
            nodeTypes.put(sourceType.getId(), nodeTypeRepository.save(copy));
        }
        Map<UUID, EdgeType> edgeTypes = new HashMap<>();
        for (EdgeType sourceType : edgeTypeRepository.findByWorkspaceAndIsDeletedFalse(sourceWorkspace, Pageable.unpaged())) {
            EdgeType copy = new EdgeType(forkWorkspace, sourceType.getName(), sourceType.isDirected());
            copy.setCreatedBy(currentUserId);
            copy.setUpdatedBy(currentUserId);
            edgeTypes.put(sourceType.getId(), edgeTypeRepository.save(copy));
        }

        NodeType defaultNodeType = nodeTypes.values().stream().findFirst()
                .orElseThrow(() -> new BusinessException("Source workspace has no node types"));
        EdgeType defaultEdgeType = edgeTypes.values().stream().findFirst()
                .orElseThrow(() -> new BusinessException("Source workspace has no edge types"));
        Map<UUID, Node> copiedNodes = new HashMap<>();
        for (Map<String, Object> data : snapshotNodes) {
            UUID sourceNodeId = UUID.fromString(String.valueOf(data.get("id")));
            Object typeId = data.get("nodeTypeId");
            NodeType nodeType = typeId == null ? defaultNodeType : nodeTypes.getOrDefault(UUID.fromString(String.valueOf(typeId)), defaultNodeType);
            Node copy = new Node(forkWorkspace, nodeType, String.valueOf(data.get("label")),
                    (Map<String, Object>) data.getOrDefault("attributes", Map.of()));
            copy.setVisibility(resolveVisibility(data));
            copy.setTags(new HashSet<>((List<String>) data.getOrDefault("tags", List.of())));
            copy.setCreatedBy(currentUserId);
            copy.setUpdatedBy(currentUserId);
            copiedNodes.put(sourceNodeId, nodeRepository.save(copy));
        }

        for (Map<String, Object> data : snapshotEdges) {
            Node source = copiedNodes.get(UUID.fromString(String.valueOf(data.get("sourceNodeId"))));
            Node target = copiedNodes.get(UUID.fromString(String.valueOf(data.get("targetNodeId"))));
            if (source == null || target == null) {
                throw new BusinessException("Snapshot contains an edge with a missing node");
            }
            Object typeId = data.get("edgeTypeId");
            EdgeType edgeType = typeId == null ? defaultEdgeType : edgeTypes.getOrDefault(UUID.fromString(String.valueOf(typeId)), defaultEdgeType);
            Edge copy = new Edge(forkWorkspace, edgeType, source, target,
                    data.get("weight") instanceof Number number ? number.doubleValue() : 1.0,
                    (Map<String, Object>) data.getOrDefault("attributes", Map.of()));
            copy.setCreatedBy(currentUserId);
            copy.setUpdatedBy(currentUserId);
            edgeRepository.save(copy);
        }
    }
}
