package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.payload.response.SearchResultResponse;
import com.knowledgenetwork.service.GraphSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Unified Search", description = "Search across graphs and knowledge nodes")
public class SearchController {

    private final GraphSearchService graphSearchService;

    public SearchController(GraphSearchService graphSearchService) {
        this.graphSearchService = graphSearchService;
    }

    @GetMapping
    @Operation(summary = "Search graphs and knowledge nodes",
            description = "Unified search across all accessible graphs and nodes. " +
                    "Respects workspace visibility and membership authorization.")
    public ResponseEntity<ApiResponse<PageResponse<SearchResultResponse>>> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) String nodeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;
        if (size < 1) size = 20;
        if (page < 0) page = 0;

        PageResponse<SearchResultResponse> results = graphSearchService.unifiedSearch(
                q, type, nodeType, page, size);

        return ResponseEntity.ok(ApiResponse.success("Search completed", results));
    }
}
