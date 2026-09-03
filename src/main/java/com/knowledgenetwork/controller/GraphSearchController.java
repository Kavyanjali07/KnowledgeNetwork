package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.service.GraphSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/graphs")
@Tag(name = "Graph Search", description = "Graph node search and query APIs")
public class GraphSearchController {

    private final GraphSearchService graphSearchService;
    private final WorkspaceRepository workspaceRepository;

    public GraphSearchController(GraphSearchService graphSearchService,
                                  WorkspaceRepository workspaceRepository) {
        this.graphSearchService = graphSearchService;
        this.workspaceRepository = workspaceRepository;
    }

    @GetMapping("/search")
    @Operation(summary = "Search nodes within a workspace")
    public ResponseEntity<ApiResponse<PageResponse<NodeResponse>>> searchNodes(
            @RequestParam UUID workspaceId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) Instant updatedAfter,
            @RequestParam(required = false) Double minConfidence,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        com.knowledgenetwork.domain.model.Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new com.knowledgenetwork.common.exception.ResourceNotFoundException("Workspace", "id", workspaceId));
        Pageable pageable = graphSearchService.createPageable(page, size, sortBy);
        Page<Node> nodes = graphSearchService.searchNodes(workspace, q, tags, visibility, updatedAfter, minConfidence, pageable);
        PageResponse<NodeResponse> pageResponse = PageResponse.from(nodes.map(this::mapNodeResponse));
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", pageResponse));
    }

    private NodeResponse mapNodeResponse(Node node) {
        NodeResponse response = new NodeResponse();
        response.setId(node.getId());
        response.setWorkspaceId(node.getWorkspace().getId());
        response.setNodeTypeId(node.getNodeType().getId());
        response.setNodeTypeName(node.getNodeType().getName());
        response.setNodeTypeColor(node.getNodeType().getColorCode());
        response.setNodeTypeIcon(node.getNodeType().getIcon());
        response.setLabel(node.getLabel());
        response.setPositionX(node.getPositionX());
        response.setPositionY(node.getPositionY());
        response.setAttributes(node.getAttributes());
        response.setCreatedAt(node.getCreatedAt());
        response.setUpdatedAt(node.getUpdatedAt());
        response.setCreatedBy(node.getCreatedBy());
        response.setUpdatedBy(node.getUpdatedBy());
        response.setVersion(node.getVersion());
        response.setDeleted(node.isDeleted());
        return response;
    }
}