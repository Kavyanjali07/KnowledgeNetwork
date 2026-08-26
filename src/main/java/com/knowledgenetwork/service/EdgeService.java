package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.ConcurrencyConflictException;
import com.knowledgenetwork.common.exception.DuplicateEdgeException;
import com.knowledgenetwork.common.exception.InvalidEdgeConnectionException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.EdgeMapper;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.EdgeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class EdgeService {

    private final WorkspaceRepository workspaceRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final EdgeTypeRepository edgeTypeRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;
    private final EdgeMapper edgeMapper;

    public EdgeService(WorkspaceRepository workspaceRepository,
                       NodeRepository nodeRepository,
                       EdgeRepository edgeRepository,
                       EdgeTypeRepository edgeTypeRepository,
                       WorkspaceSecurityValidator workspaceSecurityValidator,
                       EdgeMapper edgeMapper) {
        this.workspaceRepository = workspaceRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
        this.edgeMapper = edgeMapper;
    }

    @Transactional
    public EdgeResponse createEdge(UUID graphId, EdgeCreateRequest request) {
        Workspace workspace = getWorkspace(graphId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateWriteAccess(workspace, currentUserId);

        Node sourceNode = nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(request.getSourceNodeId(), workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", request.getSourceNodeId()));

        Node targetNode = nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(request.getTargetNodeId(), workspace)
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", request.getTargetNodeId()));

        if (Objects.equals(sourceNode.getId(), targetNode.getId())) {
            throw new InvalidEdgeConnectionException("A topic cannot be connected to itself.");
        }

        if (!sourceNode.getWorkspace().getId().equals(workspace.getId()) ||
            !targetNode.getWorkspace().getId().equals(workspace.getId())) {
            throw new InvalidEdgeConnectionException("Both ideas must belong to the same knowledge graph.");
        }

        EdgeType edgeType = resolveEdgeType(workspace, request.getEdgeTypeId(), request.getRelationshipType());

        if (edgeRepository.existsByWorkspaceAndSourceNodeAndTargetNodeAndEdgeTypeAndIsDeletedFalse(workspace, sourceNode, targetNode, edgeType)) {
            throw new DuplicateEdgeException("These ideas already have this connection.");
        }

        Map<String, Object> attributes = request.getAttributes() != null ? new HashMap<>(request.getAttributes()) : new HashMap<>();
        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            attributes.put("label", request.getLabel().trim());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            attributes.put("description", request.getDescription().trim());
        }

        Edge edge = new Edge(workspace, edgeType, sourceNode, targetNode, request.getWeight(), attributes);
        String currentUserIdStr = currentUserId.toString();
        edge.setCreatedBy(currentUserIdStr);
        edge.setUpdatedBy(currentUserIdStr);

        edge = edgeRepository.save(edge);
        return edgeMapper.toEdgeResponse(edge);
    }

    @Transactional(readOnly = true)
    public List<EdgeResponse> getEdgesByGraph(UUID graphId) {
        Workspace workspace = getWorkspace(graphId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        List<Edge> edges = edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace);
        return edges.stream().map(edgeMapper::toEdgeResponse).toList();
    }

    @Transactional(readOnly = true)
    public EdgeResponse getEdgeById(UUID edgeId) {
        Edge edge = edgeRepository.findById(edgeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Edge", "id", edgeId));

        workspaceSecurityValidator.validateReadAccess(edge.getWorkspace(), SecurityUtils.getCurrentUserId());
        return edgeMapper.toEdgeResponse(edge);
    }

    @Transactional
    public EdgeResponse updateEdge(UUID edgeId, EdgeUpdateRequest request) {
        Edge edge = edgeRepository.findById(edgeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Edge", "id", edgeId));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateWriteAccess(edge.getWorkspace(), currentUserId);

        if (request.getVersion() == null || !Objects.equals(request.getVersion(), edge.getVersion())) {
            throw new ConcurrencyConflictException("Optimistic locking failure: version mismatch");
        }

        if (request.getEdgeTypeId() != null || (request.getRelationshipType() != null && !request.getRelationshipType().isBlank())) {
            EdgeType edgeType = resolveEdgeType(edge.getWorkspace(), request.getEdgeTypeId(), request.getRelationshipType());
            edge.setEdgeType(edgeType);
        }

        if (request.getWeight() != null) {
            edge.setWeight(request.getWeight());
        }

        Map<String, Object> attributes = edge.getAttributes() != null ? new HashMap<>(edge.getAttributes()) : new HashMap<>();
        if (request.getAttributes() != null) {
            attributes.putAll(request.getAttributes());
        }

        if (request.getLabel() != null) {
            if (request.getLabel().isBlank()) {
                attributes.remove("label");
            } else {
                attributes.put("label", request.getLabel().trim());
            }
        }

        if (request.getDescription() != null) {
            if (request.getDescription().isBlank()) {
                attributes.remove("description");
            } else {
                attributes.put("description", request.getDescription().trim());
            }
        }

        edge.setAttributes(attributes);
        edge.setUpdatedBy(currentUserId.toString());
        edge = edgeRepository.save(edge);

        return edgeMapper.toEdgeResponse(edge);
    }

    @Transactional
    public void deleteEdge(UUID edgeId) {
        Edge edge = edgeRepository.findById(edgeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Edge", "id", edgeId));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateWriteAccess(edge.getWorkspace(), currentUserId);

        edge.setDeleted(true);
        edge.setUpdatedBy(currentUserId.toString());
        edgeRepository.save(edge);
    }

    private Workspace getWorkspace(UUID graphId) {
        Workspace workspace = workspaceRepository.findById(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));
        if (workspace.isDeleted()) {
            throw new ResourceNotFoundException("Graph", "id", graphId);
        }
        return workspace;
    }

    private EdgeType resolveEdgeType(Workspace workspace, UUID edgeTypeId, String relationshipType) {
        if (edgeTypeId != null) {
            return edgeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(edgeTypeId, workspace)
                    .orElseThrow(() -> new ResourceNotFoundException("EdgeType", "id", edgeTypeId));
        }

        String typeName = (relationshipType != null && !relationshipType.isBlank()) ? relationshipType.trim() : "RELATED_TO";
        return edgeTypeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(et -> et.getName().equalsIgnoreCase(typeName) || et.getName().equalsIgnoreCase(typeName.replace("_", " ")))
                .findFirst()
                .orElseGet(() -> {
                    EdgeType newType = new EdgeType(workspace, typeName, true);
                    String userId = SecurityUtils.getCurrentUserId().toString();
                    newType.setCreatedBy(userId);
                    newType.setUpdatedBy(userId);
                    return edgeTypeRepository.save(newType);
                });
    }
}
