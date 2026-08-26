package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.domain.payload.request.EdgeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.service.EdgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Knowledge Graph Edges", description = "REST APIs for managing graph relationship edges")
public class EdgeController {

    private final EdgeService edgeService;

    public EdgeController(EdgeService edgeService) {
        this.edgeService = edgeService;
    }

    @PostMapping("/graphs/{graphId}/edges")
    @Operation(summary = "Create a relationship edge between two nodes in a knowledge graph")
    public ResponseEntity<ApiResponse<EdgeResponse>> createEdge(
            @PathVariable UUID graphId,
            @Valid @RequestBody EdgeCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        EdgeResponse response = edgeService.createEdge(graphId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Relationship edge created successfully", response));
    }

    @GetMapping("/graphs/{graphId}/edges")
    @Operation(summary = "List all relationship edges in a knowledge graph")
    public ResponseEntity<ApiResponse<List<EdgeResponse>>> getGraphEdges(
            @PathVariable UUID graphId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<EdgeResponse> edges = edgeService.getEdgesByGraph(graphId);
        return ResponseEntity.ok(ApiResponse.success("Graph edges retrieved successfully", edges));
    }

    @GetMapping("/edges/{edgeId}")
    @Operation(summary = "Get a relationship edge by ID")
    public ResponseEntity<ApiResponse<EdgeResponse>> getEdgeById(
            @PathVariable UUID edgeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        EdgeResponse edge = edgeService.getEdgeById(edgeId);
        return ResponseEntity.ok(ApiResponse.success("Edge retrieved successfully", edge));
    }

    @PutMapping("/edges/{edgeId}")
    @Operation(summary = "Update an existing relationship edge")
    public ResponseEntity<ApiResponse<EdgeResponse>> updateEdge(
            @PathVariable UUID edgeId,
            @Valid @RequestBody EdgeUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        EdgeResponse response = edgeService.updateEdge(edgeId, request);
        return ResponseEntity.ok(ApiResponse.success("Relationship edge updated successfully", response));
    }

    @DeleteMapping("/edges/{edgeId}")
    @Operation(summary = "Delete a relationship edge")
    public ResponseEntity<Void> deleteEdge(
            @PathVariable UUID edgeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        edgeService.deleteEdge(edgeId);
        return ResponseEntity.noContent().build();
    }
}
