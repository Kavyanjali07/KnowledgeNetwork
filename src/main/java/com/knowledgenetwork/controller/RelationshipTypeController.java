package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.payload.request.EdgeTypeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeTypeUpdateRequest;
import com.knowledgenetwork.domain.payload.request.NodeTypeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeTypeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeTypeResponse;
import com.knowledgenetwork.domain.payload.response.NodeTypeResponse;
import com.knowledgenetwork.service.RelationshipTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relationships/types")
@Tag(name = "Relationship Types", description = "Node and edge type management APIs")
public class RelationshipTypeController {

    private final RelationshipTypeService relationshipTypeService;

    public RelationshipTypeController(RelationshipTypeService relationshipTypeService) {
        this.relationshipTypeService = relationshipTypeService;
    }

    @PostMapping("/nodes")
    @Operation(summary = "Create a new node type")
    public ResponseEntity<ApiResponse<NodeTypeResponse>> createNodeType(
            @Valid @RequestBody NodeTypeCreateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        NodeTypeResponse response = relationshipTypeService.createNodeType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Node type created successfully", response));
    }

    @GetMapping("/nodes/{workspaceId}")
    @Operation(summary = "List node types for a workspace")
    public ResponseEntity<ApiResponse<PageResponse<NodeTypeResponse>>> getNodeTypes(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NodeTypeResponse> types = relationshipTypeService.getNodeTypes(workspaceId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Node types retrieved successfully", PageResponse.from(types)));
    }

    @PutMapping("/nodes/{workspaceId}/{nodeTypeId}")
    @Operation(summary = "Update an existing node type")
    public ResponseEntity<ApiResponse<NodeTypeResponse>> updateNodeType(
            @PathVariable UUID workspaceId,
            @PathVariable UUID nodeTypeId,
            @Valid @RequestBody NodeTypeUpdateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        NodeTypeResponse response = relationshipTypeService.updateNodeType(workspaceId, nodeTypeId, request);
        return ResponseEntity.ok(ApiResponse.success("Node type updated successfully", response));
    }

    @PostMapping("/edges")
    @Operation(summary = "Create a new edge type")
    public ResponseEntity<ApiResponse<EdgeTypeResponse>> createEdgeType(
            @Valid @RequestBody EdgeTypeCreateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        EdgeTypeResponse response = relationshipTypeService.createEdgeType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Edge type created successfully", response));
    }

    @GetMapping("/edges/{workspaceId}")
    @Operation(summary = "List edge types for a workspace")
    public ResponseEntity<ApiResponse<PageResponse<EdgeTypeResponse>>> getEdgeTypes(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EdgeTypeResponse> types = relationshipTypeService.getEdgeTypes(workspaceId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Edge types retrieved successfully", PageResponse.from(types)));
    }

    @PutMapping("/edges/{workspaceId}/{edgeTypeId}")
    @Operation(summary = "Update an existing edge type")
    public ResponseEntity<ApiResponse<EdgeTypeResponse>> updateEdgeType(
            @PathVariable UUID workspaceId,
            @PathVariable UUID edgeTypeId,
            @Valid @RequestBody EdgeTypeUpdateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        EdgeTypeResponse response = relationshipTypeService.updateEdgeType(workspaceId, edgeTypeId, request);
        return ResponseEntity.ok(ApiResponse.success("Edge type updated successfully", response));
    }
}
