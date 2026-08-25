package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.service.NodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Knowledge Nodes", description = "Knowledge Graph Node CRUD APIs")
public class NodeController {

    private final NodeService nodeService;

    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @PostMapping("/graphs/{graphId}/nodes")
    @Operation(summary = "Create a new node within a knowledge graph")
    public ResponseEntity<ApiResponse<NodeResponse>> createNode(
            @PathVariable UUID graphId,
            @Valid @RequestBody NodeCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NodeResponse response = nodeService.createNode(graphId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Knowledge node created successfully", response));
    }

    @GetMapping("/graphs/{graphId}/nodes")
    @Operation(summary = "List all nodes belonging to a knowledge graph")
    public ResponseEntity<ApiResponse<PageResponse<NodeResponse>>> getGraphNodes(
            @PathVariable UUID graphId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<NodeResponse> nodes = nodeService.getGraphNodes(graphId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Graph nodes retrieved successfully", PageResponse.from(nodes)));
    }

    @GetMapping("/nodes/{nodeId}")
    @Operation(summary = "Get a knowledge node by ID")
    public ResponseEntity<ApiResponse<NodeResponse>> getNodeById(@PathVariable UUID nodeId) {
        NodeResponse node = nodeService.getNodeById(nodeId);
        return ResponseEntity.ok(ApiResponse.success("Knowledge node retrieved successfully", node));
    }

    @PutMapping("/nodes/{nodeId}")
    @Operation(summary = "Update an existing knowledge node")
    public ResponseEntity<ApiResponse<NodeResponse>> updateNode(
            @PathVariable UUID nodeId,
            @Valid @RequestBody NodeUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NodeResponse response = nodeService.updateNode(nodeId, request);
        return ResponseEntity.ok(ApiResponse.success("Knowledge node updated successfully", response));
    }

    @DeleteMapping("/nodes/{nodeId}")
    @Operation(summary = "Delete a knowledge node")
    public ResponseEntity<ApiResponse<Void>> deleteNode(
            @PathVariable UUID nodeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        nodeService.deleteNode(nodeId);
        return ResponseEntity.ok(ApiResponse.success("Knowledge node deleted successfully", null));
    }
}
