package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.payload.request.EdgeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeUpdateRequest;
import com.knowledgenetwork.domain.payload.request.GraphTraversalRequest;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.service.RelationshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relationships")
@Tag(name = "Relationships", description = "Graph node and edge relationship management APIs")
public class RelationshipController {

    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @PostMapping("/nodes")
    @Operation(summary = "Create a new node")
    public ResponseEntity<ApiResponse<NodeResponse>> createNode(
            @Valid @RequestBody NodeCreateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        NodeResponse response = relationshipService.createNode(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Node created successfully", response));
    }

    @GetMapping("/nodes/{workspaceId}")
    @Operation(summary = "List nodes for a workspace")
    public ResponseEntity<ApiResponse<PageResponse<NodeResponse>>> getNodes(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NodeResponse> nodes = relationshipService.getNodes(workspaceId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Nodes retrieved successfully", PageResponse.from(nodes)));
    }

    @PutMapping("/nodes/{workspaceId}/{nodeId}")
    @Operation(summary = "Update an existing node")
    public ResponseEntity<ApiResponse<NodeResponse>> updateNode(
            @PathVariable UUID workspaceId,
            @PathVariable UUID nodeId,
            @Valid @RequestBody NodeUpdateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        NodeResponse response = relationshipService.updateNode(workspaceId, nodeId, request);
        return ResponseEntity.ok(ApiResponse.success("Node updated successfully", response));
    }

    @DeleteMapping("/nodes/{workspaceId}/{nodeId}")
    @Operation(summary = "Delete a node (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteNode(
            @PathVariable UUID workspaceId,
            @PathVariable UUID nodeId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        relationshipService.deleteNode(workspaceId, nodeId);
        return ResponseEntity.ok(ApiResponse.success("Node deleted successfully", null));
    }

    @PostMapping("/edges")
    @Operation(summary = "Create a new edge")
    public ResponseEntity<ApiResponse<EdgeResponse>> createEdge(
            @Valid @RequestBody EdgeCreateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        EdgeResponse response = relationshipService.createEdge(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Edge created successfully", response));
    }

    @GetMapping("/edges/{workspaceId}")
    @Operation(summary = "List edges for a workspace")
    public ResponseEntity<ApiResponse<PageResponse<EdgeResponse>>> getEdges(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EdgeResponse> edges = relationshipService.getEdges(workspaceId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Edges retrieved successfully", PageResponse.from(edges)));
    }

    @PutMapping("/edges/{workspaceId}/{edgeId}")
    @Operation(summary = "Update an existing edge")
    public ResponseEntity<ApiResponse<EdgeResponse>> updateEdge(
            @PathVariable UUID workspaceId,
            @PathVariable UUID edgeId,
            @Valid @RequestBody EdgeUpdateRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        EdgeResponse response = relationshipService.updateEdge(workspaceId, edgeId, request);
        return ResponseEntity.ok(ApiResponse.success("Edge updated successfully", response));
    }

    @DeleteMapping("/edges/{workspaceId}/{edgeId}")
    @Operation(summary = "Delete an edge (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteEdge(
            @PathVariable UUID workspaceId,
            @PathVariable UUID edgeId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        relationshipService.deleteEdge(workspaceId, edgeId);
        return ResponseEntity.ok(ApiResponse.success("Edge deleted successfully", null));
    }

    @PostMapping("/traverse")
    @Operation(summary = "Traverse the graph from a root node")
    public ResponseEntity<ApiResponse<Map<String, Object>>> traverse(
            @Valid @RequestBody GraphTraversalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Traversal completed successfully", relationshipService.traverse(request)));
    }

    @GetMapping("/cycles/{workspaceId}")
    @Operation(summary = "Detect cycles in the graph")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detectCycles(
            @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.success("Cycle detection completed successfully", relationshipService.detectCycles(workspaceId)));
    }
}
