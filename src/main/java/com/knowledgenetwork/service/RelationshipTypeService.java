package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.EdgeTypeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeTypeUpdateRequest;
import com.knowledgenetwork.domain.payload.request.NodeTypeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeTypeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeTypeResponse;
import com.knowledgenetwork.domain.payload.response.NodeTypeResponse;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class RelationshipTypeService {

    private final WorkspaceRepository workspaceRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final EdgeTypeRepository edgeTypeRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;

    public RelationshipTypeService(WorkspaceRepository workspaceRepository,
                                     NodeTypeRepository nodeTypeRepository,
                                     EdgeTypeRepository edgeTypeRepository,
                                     WorkspaceSecurityValidator workspaceSecurityValidator) {
        this.workspaceRepository = workspaceRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.edgeTypeRepository = edgeTypeRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
    }

    @Transactional
    public NodeTypeResponse createNodeType(NodeTypeCreateRequest request) {
        Workspace workspace = getWorkspace(request.getWorkspaceId());
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        if (nodeTypeRepository.existsByWorkspaceAndNameAndIsDeletedFalse(workspace, request.getName())) {
            throw new BusinessException("Node type already exists in this workspace");
        }
        NodeType nodeType = new NodeType(workspace, request.getName(), request.getColorCode(), request.getIcon());
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        nodeType.setCreatedBy(currentUserId);
        nodeType.setUpdatedBy(currentUserId);
        return mapNodeType(nodeTypeRepository.save(nodeType));
    }

    @Transactional(readOnly = true)
    public Page<NodeTypeResponse> getNodeTypes(UUID workspaceId, Pageable pageable) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return nodeTypeRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable).map(this::mapNodeType);
    }

    @Transactional
    public NodeTypeResponse updateNodeType(UUID workspaceId, UUID nodeTypeId, NodeTypeUpdateRequest request) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        NodeType nodeType = nodeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(nodeTypeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("NodeType", "id", nodeTypeId));
        if (request.getVersion() == null || !Objects.equals(request.getVersion(), nodeType.getVersion())) {
            throw new BusinessException("Optimistic locking failure: version mismatch");
        }
        if (request.getName() != null) {
            nodeType.setName(request.getName());
        }
        if (request.getColorCode() != null) {
            nodeType.setColorCode(request.getColorCode());
        }
        if (request.getIcon() != null) {
            nodeType.setIcon(request.getIcon());
        }
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        nodeType.setUpdatedBy(currentUserId);
        return mapNodeType(nodeTypeRepository.save(nodeType));
    }

    @Transactional
    public EdgeTypeResponse createEdgeType(EdgeTypeCreateRequest request) {
        Workspace workspace = getWorkspace(request.getWorkspaceId());
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        if (edgeTypeRepository.existsByWorkspaceAndNameAndIsDeletedFalse(workspace, request.getName())) {
            throw new BusinessException("Edge type already exists in this workspace");
        }
        EdgeType edgeType = new EdgeType(workspace, request.getName(), request.isDirected());
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        edgeType.setCreatedBy(currentUserId);
        edgeType.setUpdatedBy(currentUserId);
        return mapEdgeType(edgeTypeRepository.save(edgeType));
    }

    @Transactional(readOnly = true)
    public Page<EdgeTypeResponse> getEdgeTypes(UUID workspaceId, Pageable pageable) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return edgeTypeRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable).map(this::mapEdgeType);
    }

    @Transactional
    public EdgeTypeResponse updateEdgeType(UUID workspaceId, UUID edgeTypeId, EdgeTypeUpdateRequest request) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateWriteAccess(workspace, SecurityUtils.getCurrentUserId());
        EdgeType edgeType = edgeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(edgeTypeId, workspace)
                .orElseThrow(() -> new ResourceNotFoundException("EdgeType", "id", edgeTypeId));
        if (request.getVersion() == null || !Objects.equals(request.getVersion(), edgeType.getVersion())) {
            throw new BusinessException("Optimistic locking failure: version mismatch");
        }
        if (request.getName() != null) {
            edgeType.setName(request.getName());
        }
        if (request.getIsDirected() != null) {
            edgeType.setDirected(request.getIsDirected());
        }
        String currentUserId = SecurityUtils.getCurrentUserId().toString();
        edgeType.setUpdatedBy(currentUserId);
        return mapEdgeType(edgeTypeRepository.save(edgeType));
    }

    private Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(UUID.fromString(workspaceId.toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
    }

    private NodeTypeResponse mapNodeType(NodeType nodeType) {
        NodeTypeResponse response = new NodeTypeResponse();
        response.setId(nodeType.getId());
        response.setWorkspaceId(nodeType.getWorkspace().getId());
        response.setName(nodeType.getName());
        response.setColorCode(nodeType.getColorCode());
        response.setIcon(nodeType.getIcon());
        response.setCreatedAt(nodeType.getCreatedAt());
        response.setUpdatedAt(nodeType.getUpdatedAt());
        response.setVersion(nodeType.getVersion());
        response.setDeleted(nodeType.isDeleted());
        return response;
    }

    private EdgeTypeResponse mapEdgeType(EdgeType edgeType) {
        EdgeTypeResponse response = new EdgeTypeResponse();
        response.setId(edgeType.getId());
        response.setWorkspaceId(edgeType.getWorkspace().getId());
        response.setName(edgeType.getName());
        response.setDirected(edgeType.isDirected());
        response.setCreatedAt(edgeType.getCreatedAt());
        response.setUpdatedAt(edgeType.getUpdatedAt());
        response.setVersion(edgeType.getVersion());
        response.setDeleted(edgeType.isDeleted());
        return response;
    }
}

