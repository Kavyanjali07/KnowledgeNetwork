package com.knowledgenetwork.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.model.GraphVersion;
import com.knowledgenetwork.domain.payload.request.GraphForkCreateRequest;
import com.knowledgenetwork.domain.payload.request.GraphSnapshotCreateRequest;
import com.knowledgenetwork.domain.payload.response.GraphForkResponse;
import com.knowledgenetwork.service.GraphVersioningService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/graphs")
@Tag(name = "Graph Versioning", description = "Graph snapshot, version history, fork, and restore APIs")
public class GraphVersioningController {

    private final GraphVersioningService graphVersioningService;

    public GraphVersioningController(GraphVersioningService graphVersioningService) {
        this.graphVersioningService = graphVersioningService;
    }

    @PostMapping("/{workspaceId}/snapshots")
    @Operation(summary = "Create a graph snapshot for a workspace")
    public ResponseEntity<ApiResponse<GraphVersion>> createSnapshot(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody GraphSnapshotCreateRequest request) {
        GraphVersion version = graphVersioningService.createSnapshot(workspaceId, request.getLabel(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Snapshot created successfully", version));
    }

    @GetMapping("/{workspaceId}/versions")
    @Operation(summary = "List version history for a workspace")
    public ResponseEntity<ApiResponse<PageResponse<GraphVersion>>> listHistory(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GraphVersion> versions = graphVersioningService.listHistory(workspaceId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Version history retrieved successfully", PageResponse.from(versions)));
    }

    @PostMapping("/{workspaceId}/forks")
    @Operation(summary = "Create a fork from a version")
    public ResponseEntity<ApiResponse<GraphForkResponse>> createFork(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody GraphForkCreateRequest request) {
        GraphForkResponse fork = graphVersioningService.createFork(workspaceId, request.getSourceVersionId(), request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fork created successfully", fork));
    }

    @GetMapping("/{workspaceId}/versions/{versionId}")
    @Operation(summary = "Get version detail")
    public ResponseEntity<ApiResponse<GraphVersion>> getVersion(
            @PathVariable UUID workspaceId,
            @PathVariable UUID versionId) {
        GraphVersion version = graphVersioningService.getVersion(workspaceId, versionId);
        return ResponseEntity.ok(ApiResponse.success("Version retrieved successfully", version));
    }

    @PostMapping("/{workspaceId}/versions/{versionId}/restore")
    @Operation(summary = "Restore a workspace to a previous version")
    public ResponseEntity<ApiResponse<GraphVersion>> restoreVersion(
            @PathVariable UUID workspaceId,
            @PathVariable UUID versionId) {
        GraphVersion version = graphVersioningService.restoreVersion(workspaceId, versionId);
        return ResponseEntity.ok(ApiResponse.success("Version restored successfully", version));
    }

    @GetMapping("/{workspaceId}/provenance")
    @Operation(summary = "Get derivation lineage and provenance metadata for a workspace")
    public ResponseEntity<ApiResponse<GraphForkResponse>> getProvenance(
            @PathVariable UUID workspaceId) {
        GraphForkResponse response = graphVersioningService.getProvenance(workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Provenance retrieved successfully", response));
    }
}