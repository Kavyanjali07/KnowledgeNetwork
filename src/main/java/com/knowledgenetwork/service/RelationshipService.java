package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.EdgeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeUpdateRequest;
import com.knowledgenetwork.domain.payload.request.GraphTraversalRequest;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class RelationshipService {

    private final WorkspaceRepository workspaceRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final EdgeTypeRepository edgeTypeRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;

    public RelationshipService(WorkspaceRepository workspaceRepository,
                               NodeRepository nodeRepository,
                               EdgeRepository edgeRepository,
                               NodeTypeRepository nodeTypeRepository,
                               EdgeTypeRepository edgeTypeRepository,
                               WorkspaceSecurityValidator workspaceSecurityValidator) {
        this.workspaceRepository = workspaceRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
    }

    @Transactional
    public NodeResponse createNode(NodeCreateRequest request) {
        Workspace workspace = getWorkspace(request.getWorkspaceId());
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        NodeType nodeType = getNodeType(request.getNodeTypeId(), workspace);
        Node node = new Node(workspace, nodeType, request.getLabel(), request.getAttributes());
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        node.setCreatedBy(currentUserId);
        node.setUpdatedBy(currentUserId);
        node = nodeRepository.save(node);
        return mapNode(node);
    }

    @Transactional(readOnly = true)
    public Page<NodeResponse> getNodes(UUID workspaceId, Pageable pageable) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable).map(this::mapNode);
    }

    @Transactional
    public NodeResponse updateNode(UUID workspaceId, UUID nodeId, NodeUpdateRequest request) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        Node node = nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(nodeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", nodeId));
        if (request.getVersion() == null || !Objects.equals(request.getVersion(), node.getVersion())) {
            throw new com.knowledgenetwork.common.exception.ConcurrencyConflictException("Optimistic locking failure: version mismatch");
        }
        if (request.getNodeTypeId() != null) {
            NodeType nodeType = getNodeType(request.getNodeTypeId(), workspace);
            node.setNodeType(nodeType);
        }
        if (request.getLabel() != null) {
            node.setLabel(request.getLabel());
        }
        if (request.getAttributes() != null) {
            node.setAttributes(request.getAttributes());
        }
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        node.setUpdatedBy(currentUserId);
        node = nodeRepository.save(node);
        return mapNode(node);
    }

    @Transactional
    public void deleteNode(UUID workspaceId, UUID nodeId) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        Node node = nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(nodeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", nodeId));
        node.setDeleted(true);
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        node.setUpdatedBy(currentUserId);
        nodeRepository.save(node);

        List<Edge> edges = edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged()).getContent();
        for (Edge edge : edges) {
            if (edge.getSourceNode().getId().equals(nodeId) || edge.getTargetNode().getId().equals(nodeId)) {
                edge.setDeleted(true);
                edge.setUpdatedBy(currentUserId);
                edgeRepository.save(edge);
            }
        }
    }

    @Transactional
    public EdgeResponse createEdge(EdgeCreateRequest request) {
        Workspace workspace = getWorkspace(request.getWorkspaceId());
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        EdgeType edgeType = getEdgeType(request.getEdgeTypeId(), workspace);
        Node sourceNode = getNode(request.getSourceNodeId(), workspace);
        Node targetNode = getNode(request.getTargetNodeId(), workspace);
        if (Objects.equals(sourceNode.getId(), targetNode.getId())) {
            throw new BusinessException("Self-loops are not allowed");
        }
        if (edgeType.isDirected() && Objects.equals(sourceNode.getId(), targetNode.getId())) {
            throw new BusinessException("Directed edge cannot form a self-loop");
        }
        if (hasCycle(workspace, sourceNode, targetNode, edgeType)) {
            throw new BusinessException("Creating this relationship would introduce a cycle");
        }
        Edge edge = new Edge(workspace, edgeType, sourceNode, targetNode, request.getWeight(), request.getAttributes());
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        edge.setCreatedBy(currentUserId);
        edge.setUpdatedBy(currentUserId);
        edge = edgeRepository.save(edge);
        return mapEdge(edge);
    }

    @Transactional(readOnly = true)
    public Page<EdgeResponse> getEdges(UUID workspaceId, Pageable pageable) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable).map(this::mapEdge);
    }

    @Transactional
    public EdgeResponse updateEdge(UUID workspaceId, UUID edgeId, EdgeUpdateRequest request) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        Edge edge = edgeRepository.findByIdAndWorkspaceAndIsDeletedFalse(edgeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Edge", "id", edgeId));
        if (request.getVersion() == null || !Objects.equals(request.getVersion(), edge.getVersion())) {
            throw new BusinessException("Optimistic locking failure: version mismatch");
        }
        if (request.getEdgeTypeId() != null) {
            EdgeType edgeType = getEdgeType(request.getEdgeTypeId(), workspace);
            edge.setEdgeType(edgeType);
        }
        if (request.getWeight() != null) {
            edge.setWeight(request.getWeight());
        }
        if (request.getAttributes() != null) {
            edge.setAttributes(request.getAttributes());
        }
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        edge.setUpdatedBy(currentUserId);
        edge = edgeRepository.save(edge);
        return mapEdge(edge);
    }

    @Transactional
    public void deleteEdge(UUID workspaceId, UUID edgeId) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        Edge edge = edgeRepository.findByIdAndWorkspaceAndIsDeletedFalse(edgeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Edge", "id", edgeId));
        edge.setDeleted(true);
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        edge.setUpdatedBy(currentUserId);
        edgeRepository.save(edge);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> traverse(GraphTraversalRequest request) {
        Workspace workspace = getWorkspace(request.getWorkspaceId());
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        Node root = getNode(request.getRootNodeId(), workspace);
        Set<UUID> visited = new LinkedHashSet<>();
        Map<UUID, List<UUID>> adjacency = buildAdjacency(workspace, request);
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        ArrayDeque<TraversalNode> queue = new ArrayDeque<>();
        queue.add(new TraversalNode(root.getId(), 0));
        visited.add(root.getId());

        while (!queue.isEmpty()) {
            TraversalNode current = queue.removeFirst();
            if (current.depth >= request.getMaxDepth()) {
                continue;
            }
            for (UUID adjacentId : adjacency.getOrDefault(current.nodeId, Collections.emptyList())) {
                if (visited.contains(adjacentId)) {
                    continue;
                }
                visited.add(adjacentId);
                queue.addLast(new TraversalNode(adjacentId, current.depth + 1));
            }
        }

        for (UUID nodeId : visited) {
            Node node = nodeRepository.findById(UUID.fromString(nodeId.toString())).orElse(null);
            if (node != null && !node.isDeleted()) {
                nodes.add(Map.of(
                        "id", node.getId(),
                        "label", node.getLabel(),
                        "type", node.getNodeType().getName()
                ));
            }
        }

        for (Edge edge : edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged())) {
            if (visited.contains(edge.getSourceNode().getId()) && visited.contains(edge.getTargetNode().getId())) {
                edges.add(Map.of(
                        "id", edge.getId(),
                        "source", edge.getSourceNode().getId(),
                        "target", edge.getTargetNode().getId(),
                        "type", edge.getEdgeType().getName(),
                        "weight", edge.getWeight()
                ));
            }
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("rootNodeId", root.getId());
        payload.put("visitedNodeCount", nodes.size());
        payload.put("visitedEdgeCount", edges.size());
        payload.put("nodes", nodes);
        payload.put("edges", edges);
        return payload;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detectCycles(UUID workspaceId) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        List<Edge> edges = edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged()).getContent();
        Map<UUID, List<UUID>> adjacency = new HashMap<>();
        for (Edge edge : edges) {
            adjacency.computeIfAbsent(edge.getSourceNode().getId(), ignored -> new ArrayList<>()).add(edge.getTargetNode().getId());
        }

        Set<List<UUID>> cycles = new LinkedHashSet<>();
        for (UUID nodeId : adjacency.keySet()) {
            List<UUID> path = new ArrayList<>();
            Set<UUID> visited = new HashSet<>();
            dfsCycle(nodeId, nodeId, adjacency, visited, path, cycles);
        }

        List<Map<String, Object>> cyclePayload = new ArrayList<>();
        for (List<UUID> cycle : cycles) {
            cyclePayload.add(Map.of("path", cycle));
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("workspaceId", workspaceId);
        payload.put("cycleCount", cyclePayload.size());
        payload.put("cycles", cyclePayload);
        return payload;
    }

    private boolean hasCycle(Workspace workspace, Node sourceNode, Node targetNode, EdgeType edgeType) {
        if (sourceNode.getId() == null || targetNode.getId() == null) {
            return false;
        }
        List<Edge> existing = edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged()).getContent();
        Map<UUID, List<UUID>> adjacency = new HashMap<>();
        for (Edge edge : existing) {
            adjacency.computeIfAbsent(edge.getSourceNode().getId(), ignored -> new ArrayList<>()).add(edge.getTargetNode().getId());
        }
        adjacency.computeIfAbsent(sourceNode.getId(), ignored -> new ArrayList<>()).add(targetNode.getId());
        Set<UUID> visited = new HashSet<>();
        Set<UUID> stack = new HashSet<>();
        return dfsHasCycle(sourceNode.getId(), adjacency, visited, stack);
    }

    private boolean dfsHasCycle(UUID current, Map<UUID, List<UUID>> adjacency, Set<UUID> visited, Set<UUID> stack) {
        visited.add(current);
        stack.add(current);
        for (UUID next : adjacency.getOrDefault(current, Collections.emptyList())) {
            if (!visited.contains(next)) {
                if (dfsHasCycle(next, adjacency, visited, stack)) {
                    return true;
                }
            } else if (stack.contains(next)) {
                return true;
            }
        }
        stack.remove(current);
        return false;
    }

    private void dfsCycle(UUID start, UUID current, Map<UUID, List<UUID>> adjacency, Set<UUID> visited, List<UUID> path, Set<List<UUID>> cycles) {
        visited.add(current);
        path.add(current);
        for (UUID next : adjacency.getOrDefault(current, Collections.emptyList())) {
            if (!visited.contains(next)) {
                dfsCycle(start, next, adjacency, visited, path, cycles);
            } else if (Objects.equals(next, start)) {
                List<UUID> cycle = new ArrayList<>(path);
                if (!cycle.isEmpty()) {
                    cycles.add(cycle);
                }
            }
        }
        path.remove(path.size() - 1);
        visited.remove(current);
    }

    private Map<UUID, List<UUID>> buildAdjacency(Workspace workspace, GraphTraversalRequest request) {
        Map<UUID, List<UUID>> adjacency = new HashMap<>();
        for (Edge edge : edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged())) {
            if (request.getEdgeTypeNames() != null && !request.getEdgeTypeNames().isEmpty()) {
                if (!request.getEdgeTypeNames().contains(edge.getEdgeType().getName())) {
                    continue;
                }
            }
            if (request.getDirection() == GraphTraversalRequest.TraversalDirection.OUTGOING) {
                adjacency.computeIfAbsent(edge.getSourceNode().getId(), ignored -> new ArrayList<>()).add(edge.getTargetNode().getId());
            } else if (request.getDirection() == GraphTraversalRequest.TraversalDirection.INCOMING) {
                adjacency.computeIfAbsent(edge.getTargetNode().getId(), ignored -> new ArrayList<>()).add(edge.getSourceNode().getId());
            } else {
                adjacency.computeIfAbsent(edge.getSourceNode().getId(), ignored -> new ArrayList<>()).add(edge.getTargetNode().getId());
                adjacency.computeIfAbsent(edge.getTargetNode().getId(), ignored -> new ArrayList<>()).add(edge.getSourceNode().getId());
            }
        }
        return adjacency;
    }

    private Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(UUID.fromString(workspaceId.toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
    }

    private NodeType getNodeType(UUID nodeTypeId, Workspace workspace) {
        return nodeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(nodeTypeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("NodeType", "id", nodeTypeId));
    }

    private EdgeType getEdgeType(UUID edgeTypeId, Workspace workspace) {
        return edgeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(edgeTypeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("EdgeType", "id", edgeTypeId));
    }

    private Node getNode(UUID nodeId, Workspace workspace) {
        return nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(nodeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", nodeId));
    }

    private NodeResponse mapNode(Node node) {
        NodeResponse response = new NodeResponse();
        response.setId(node.getId());
        response.setWorkspaceId(node.getWorkspace().getId());
        response.setNodeTypeId(node.getNodeType().getId());
        response.setNodeTypeName(node.getNodeType().getName());
        response.setNodeTypeColor(node.getNodeType().getColorCode());
        response.setNodeTypeIcon(node.getNodeType().getIcon());
        response.setLabel(node.getLabel());
        response.setAttributes(node.getAttributes());
        response.setCreatedAt(node.getCreatedAt());
        response.setUpdatedAt(node.getUpdatedAt());
        response.setCreatedBy(node.getCreatedBy());
        response.setUpdatedBy(node.getUpdatedBy());
        response.setVersion(node.getVersion());
        response.setDeleted(node.isDeleted());
        return response;
    }

    private EdgeResponse mapEdge(Edge edge) {
        EdgeResponse response = new EdgeResponse();
        response.setId(edge.getId());
        response.setWorkspaceId(edge.getWorkspace().getId());
        response.setEdgeTypeId(edge.getEdgeType().getId());
        response.setEdgeTypeName(edge.getEdgeType().getName());
        response.setDirected(edge.getEdgeType().isDirected());
        response.setSourceNodeId(edge.getSourceNode().getId());
        response.setSourceNodeLabel(edge.getSourceNode().getLabel());
        response.setTargetNodeId(edge.getTargetNode().getId());
        response.setTargetNodeLabel(edge.getTargetNode().getLabel());
        response.setWeight(edge.getWeight());
        response.setAttributes(edge.getAttributes());
        response.setCreatedAt(edge.getCreatedAt());
        response.setUpdatedAt(edge.getUpdatedAt());
        response.setCreatedBy(edge.getCreatedBy());
        response.setUpdatedBy(edge.getUpdatedBy());
        response.setVersion(edge.getVersion());
        response.setDeleted(edge.isDeleted());
        return response;
    }

    private static class TraversalNode {
        private final UUID nodeId;
        private final int depth;

        private TraversalNode(UUID nodeId, int depth) {
            this.nodeId = nodeId;
            this.depth = depth;
        }
    }
}
