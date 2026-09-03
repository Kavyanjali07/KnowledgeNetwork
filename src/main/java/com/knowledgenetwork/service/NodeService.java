package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.ConcurrencyConflictException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.NodeMapper;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class NodeService {

    private static final int MAX_PAGE_SIZE = 200;

    private final NodeRepository nodeRepository;
    private final WorkspaceRepository workspaceRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final EdgeRepository edgeRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;
    private final NodeMapper nodeMapper;

    public NodeService(NodeRepository nodeRepository,
                       WorkspaceRepository workspaceRepository,
                       NodeTypeRepository nodeTypeRepository,
                       EdgeRepository edgeRepository,
                       WorkspaceSecurityValidator workspaceSecurityValidator,
                       NodeMapper nodeMapper) {
        this.nodeRepository = nodeRepository;
        this.workspaceRepository = workspaceRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeRepository = edgeRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
        this.nodeMapper = nodeMapper;
    }

    @Transactional
    public NodeResponse createNode(UUID graphId, NodeCreateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));

        workspaceSecurityValidator.validateWriteAccess(workspace, currentUserId);

        NodeType nodeType;
        if (request.getNodeTypeId() != null) {
            nodeType = nodeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(request.getNodeTypeId(), workspace)
                    .orElseThrow(() -> new ResourceNotFoundException("NodeType", "id", request.getNodeTypeId()));
        } else {
            List<NodeType> defaultTypes = nodeTypeRepository.findByWorkspaceAndIsDeletedFalse(workspace);
            if (defaultTypes.isEmpty()) {
                NodeType seeded = new NodeType(workspace, "Concept", "#22D3EE", "brain");
                seeded.setCreatedBy(currentUserId.toString());
                seeded.setUpdatedBy(currentUserId.toString());
                nodeType = nodeTypeRepository.save(seeded);
            } else {
                nodeType = defaultTypes.get(0);
            }
        }

        Map<String, Object> attributes = request.getAttributes() != null ? new HashMap<>(request.getAttributes()) : new HashMap<>();
        if (request.getContent() != null && !request.getContent().isBlank()) {
            attributes.put("description", request.getContent().trim());
        }

        Double posX = request.getPositionX();
        if (posX == null && attributes.containsKey("positionX") && attributes.get("positionX") != null) {
            try {
                posX = Double.parseDouble(attributes.get("positionX").toString());
            } catch (Exception ignored) {}
        }

        Double posY = request.getPositionY();
        if (posY == null && attributes.containsKey("positionY") && attributes.get("positionY") != null) {
            try {
                posY = Double.parseDouble(attributes.get("positionY").toString());
            } catch (Exception ignored) {}
        }

        if (posX == null || posY == null) {
            long existingCount = nodeRepository.countByWorkspaceAndIsDeletedFalse(workspace);
            int col = (int) (existingCount % 3);
            int row = (int) (existingCount / 3);
            if (posX == null) posX = 100.0 + (col * 300.0);
            if (posY == null) posY = 100.0 + (row * 200.0);
        }

        attributes.put("positionX", posX);
        attributes.put("positionY", posY);

        Node node = new Node(workspace, nodeType, request.getLabel().trim(), attributes, posX, posY);
        node.setCreatedBy(currentUserId.toString());
        node.setUpdatedBy(currentUserId.toString());

        node = nodeRepository.save(node);
        return nodeMapper.toNodeResponse(node);
    }

    @Transactional(readOnly = true)
    public Page<NodeResponse> getGraphNodes(UUID graphId, int page, int size) {
        Workspace workspace = workspaceRepository.findByIdAndIsDeletedFalse(graphId)
                .orElseThrow(() -> new ResourceNotFoundException("Graph", "id", graphId));

        UUID currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
        }

        workspaceSecurityValidator.validateReadAccess(workspace, currentUserId);

        int validatedPage = Math.max(0, page);
        int validatedSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by(Sort.Direction.ASC, "createdAt"));

        return nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable).map(nodeMapper::toNodeResponse);
    }

    @Transactional(readOnly = true)
    public NodeResponse getNodeById(UUID nodeId) {
        Node node = nodeRepository.findById(nodeId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", nodeId));

        UUID currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
        }

        workspaceSecurityValidator.validateReadAccess(node.getWorkspace(), currentUserId);

        return nodeMapper.toNodeResponse(node);
    }

    @Transactional
    public NodeResponse updateNode(UUID nodeId, NodeUpdateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Node node = nodeRepository.findById(nodeId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", nodeId));

        workspaceSecurityValidator.validateWriteAccess(node.getWorkspace(), currentUserId);

        if (request.getVersion() != null && !Objects.equals(request.getVersion(), node.getVersion())) {
            throw new ConcurrencyConflictException("Node version mismatch. Entity was modified concurrently.");
        }

        if (request.getNodeTypeId() != null) {
            NodeType nodeType = nodeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(request.getNodeTypeId(), node.getWorkspace())
                    .orElseThrow(() -> new ResourceNotFoundException("NodeType", "id", request.getNodeTypeId()));
            node.setNodeType(nodeType);
        }

        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            node.setLabel(request.getLabel().trim());
        }

        Map<String, Object> attributes = node.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
        } else {
            attributes = new HashMap<>(attributes);
        }

        if (request.getAttributes() != null) {
            attributes.putAll(request.getAttributes());
        }

        if (request.getContent() != null) {
            attributes.put("description", request.getContent().trim());
        }

        Double posX = request.getPositionX();
        if (posX == null && request.getAttributes() != null && request.getAttributes().containsKey("positionX") && request.getAttributes().get("positionX") != null) {
            try {
                posX = Double.parseDouble(request.getAttributes().get("positionX").toString());
            } catch (Exception ignored) {}
        }
        if (posX != null) {
            node.setPositionX(posX);
            attributes.put("positionX", posX);
        }

        Double posY = request.getPositionY();
        if (posY == null && request.getAttributes() != null && request.getAttributes().containsKey("positionY") && request.getAttributes().get("positionY") != null) {
            try {
                posY = Double.parseDouble(request.getAttributes().get("positionY").toString());
            } catch (Exception ignored) {}
        }
        if (posY != null) {
            node.setPositionY(posY);
            attributes.put("positionY", posY);
        }

        node.setAttributes(attributes);
        node.setUpdatedBy(currentUserId.toString());

        node = nodeRepository.save(node);
        return nodeMapper.toNodeResponse(node);
    }

    @Transactional
    public void deleteNode(UUID nodeId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Node node = nodeRepository.findById(nodeId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Node", "id", nodeId));

        workspaceSecurityValidator.validateWriteAccess(node.getWorkspace(), currentUserId);

        node.setDeleted(true);
        node.setUpdatedBy(currentUserId.toString());
        nodeRepository.save(node);

        var edges = edgeRepository.findByWorkspaceAndIsDeletedFalse(node.getWorkspace(), Pageable.unpaged()).getContent();
        for (var edge : edges) {
            if (edge.getSourceNode().getId().equals(nodeId) || edge.getTargetNode().getId().equals(nodeId)) {
                edge.setDeleted(true);
                edge.setUpdatedBy(currentUserId.toString());
                edgeRepository.save(edge);
            }
        }
    }
}
