package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.payload.request.GraphCreateRequest;
import com.knowledgenetwork.domain.payload.request.GraphUpdateRequest;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.service.GraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/graphs")
@Tag(name = "Knowledge Graphs", description = "Core Knowledge Graph CRUD APIs")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @PostMapping
    @Operation(summary = "Create a new knowledge graph")
    public ResponseEntity<ApiResponse<GraphResponse>> createGraph(
            @Valid @RequestBody GraphCreateRequest request) {
        GraphResponse response = graphService.createGraph(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Knowledge graph created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated knowledge graphs accessible to current user")
    public ResponseEntity<ApiResponse<PageResponse<GraphResponse>>> getGraphs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Visibility visibility) {
        Page<GraphResponse> graphPage = graphService.getGraphs(page, size, sort, direction, visibility);
        return ResponseEntity.ok(ApiResponse.success("Knowledge graphs retrieved successfully", PageResponse.from(graphPage)));
    }

    @GetMapping("/{graphId}")
    @Operation(summary = "Get a knowledge graph by ID")
    public ResponseEntity<ApiResponse<GraphResponse>> getGraph(
            @PathVariable UUID graphId) {
        GraphResponse response = graphService.getGraphById(graphId);
        return ResponseEntity.ok(ApiResponse.success("Knowledge graph retrieved successfully", response));
    }

    @PutMapping("/{graphId}")
    @Operation(summary = "Update an existing knowledge graph")
    public ResponseEntity<ApiResponse<GraphResponse>> updateGraph(
            @PathVariable UUID graphId,
            @Valid @RequestBody GraphUpdateRequest request) {
        GraphResponse response = graphService.updateGraph(graphId, request);
        return ResponseEntity.ok(ApiResponse.success("Knowledge graph updated successfully", response));
    }

    @PutMapping("/{graphId}/publish")
    @Operation(summary = "Publish a knowledge graph and set license metadata")
    public ResponseEntity<ApiResponse<GraphResponse>> publishGraph(
            @PathVariable UUID graphId,
            @RequestBody(required = false) com.knowledgenetwork.domain.payload.request.GraphPublishRequest request) {
        com.knowledgenetwork.domain.enums.LicenseType license = request != null ? request.getLicenseType() : com.knowledgenetwork.domain.enums.LicenseType.ALL_RIGHTS_RESERVED;
        String attribution = request != null ? request.getCustomAttribution() : null;
        GraphResponse response = graphService.publishGraph(graphId, license, attribution);
        return ResponseEntity.ok(ApiResponse.success("Knowledge graph published successfully", response));
    }

    @PutMapping("/{graphId}/unpublish")
    @Operation(summary = "Unpublish a knowledge graph and revert to private status")
    public ResponseEntity<ApiResponse<GraphResponse>> unpublishGraph(
            @PathVariable UUID graphId) {
        GraphResponse response = graphService.unpublishGraph(graphId);
        return ResponseEntity.ok(ApiResponse.success("Knowledge graph unpublished successfully", response));
    }

    @DeleteMapping("/{graphId}")
    @Operation(summary = "Delete a knowledge graph")
    public ResponseEntity<ApiResponse<Void>> deleteGraph(
            @PathVariable UUID graphId) {
        graphService.deleteGraph(graphId);
        return ResponseEntity.ok(ApiResponse.success("Knowledge graph deleted successfully", null));
    }
}
