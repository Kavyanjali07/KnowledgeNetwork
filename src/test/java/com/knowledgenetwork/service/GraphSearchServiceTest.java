package com.knowledgenetwork.service;

import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.model.*;
import com.knowledgenetwork.domain.payload.response.SearchResultResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphSearchServiceTest {

    @Mock private NodeRepository nodeRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private UserRepository userRepository;
    @Mock private EdgeRepository edgeRepository;

    @InjectMocks
    private GraphSearchService graphSearchService;

    private Workspace workspace;
    private NodeType nodeType;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
        workspace.setId(UUID.randomUUID());
        workspace.setName("Spring Boot Learning Path");
        workspace.setDescription("Learn Spring Boot fundamentals");
        workspace.setVisibility(Visibility.PUBLIC);
        workspace.setCreatedAt(Instant.now());
        workspace.setUpdatedAt(Instant.now());

        nodeType = new NodeType();
        nodeType.setId(UUID.randomUUID());
        nodeType.setName("Concept");
        nodeType.setColorCode("#22D3EE");
    }

    // --- Existing workspace-scoped search test (preserved) ---

    @Test
    void searchNodesShouldDelegateToRepositoryWithSpecificationsAndPagination() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("label").ascending());
        when(nodeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        Page<Node> result = graphSearchService.searchNodes(workspace, "alpha", List.of("ai"), Visibility.PUBLIC, pageable);

        assertNotNull(result);
        verify(nodeRepository).findAll(any(Specification.class), eq(pageable));
    }

    // --- Unified search tests ---

    @Test
    void emptyQueryShouldReturnEmptyResults() {
        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("", "all", null, 0, 20);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void nullQueryShouldReturnEmptyResults() {
        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch(null, "all", null, 0, 20);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchGraphsByTitle() {
        when(workspaceRepository.searchAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of(workspace)));
        when(nodeRepository.searchNodesInAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(nodeRepository.countByWorkspaceAndIsDeletedFalse(workspace)).thenReturn(5L);
        when(edgeRepository.countByWorkspaceAndIsDeletedFalse(workspace)).thenReturn(3L);

        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("Spring", "all", null, 0, 20);

        assertFalse(result.getContent().isEmpty());
        SearchResultResponse graphResult = result.getContent().stream()
                .filter(r -> r.getResultType() == SearchResultResponse.ResultType.GRAPH)
                .findFirst().orElse(null);
        assertNotNull(graphResult);
        assertEquals("Spring Boot Learning Path", graphResult.getTitle());
        assertEquals(5, graphResult.getNodeCount());
    }

    @Test
    void searchNodesByLabel() {
        Node node = createNode("Spring Security", "Authentication framework");
        when(workspaceRepository.searchAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(nodeRepository.searchNodesInAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of(node)));

        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("Spring", "all", null, 0, 20);

        SearchResultResponse nodeResult = result.getContent().stream()
                .filter(r -> r.getResultType() == SearchResultResponse.ResultType.NODE)
                .findFirst().orElse(null);
        assertNotNull(nodeResult);
        assertEquals("Spring Security", nodeResult.getTitle());
        assertEquals(workspace.getId(), nodeResult.getGraphId());
        assertEquals("Spring Boot Learning Path", nodeResult.getGraphTitle());
    }

    @Test
    void searchOnlyGraphsWhenTypeIsGraphs() {
        when(workspaceRepository.searchAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of(workspace)));
        when(nodeRepository.countByWorkspaceAndIsDeletedFalse(workspace)).thenReturn(0L);
        when(edgeRepository.countByWorkspaceAndIsDeletedFalse(workspace)).thenReturn(0L);

        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("Spring", "graphs", null, 0, 20);

        assertTrue(result.getContent().stream().allMatch(r -> r.getResultType() == SearchResultResponse.ResultType.GRAPH));
        verify(nodeRepository, never()).searchNodesInAccessibleWorkspaces(any(), any(), any());
    }

    @Test
    void searchOnlyNodesWhenTypeIsNodes() {
        Node node = createNode("Spring Boot", "Framework");
        when(nodeRepository.searchNodesInAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of(node)));

        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("Spring", "nodes", null, 0, 20);

        assertTrue(result.getContent().stream().allMatch(r -> r.getResultType() == SearchResultResponse.ResultType.NODE));
        verify(workspaceRepository, never()).searchAccessibleWorkspaces(any(), any(), any());
    }

    @Test
    void searchWithNodeTypeFilter() {
        Node node = createNode("Spring Security", "Auth");
        when(nodeRepository.searchNodesInAccessibleWorkspacesByNodeType(any(), eq("Spring"), eq("Concept"), any()))
                .thenReturn(new PageImpl<>(List.of(node)));

        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("Spring", "nodes", "Concept", 0, 20);

        assertFalse(result.getContent().isEmpty());
        verify(nodeRepository).searchNodesInAccessibleWorkspacesByNodeType(any(), eq("Spring"), eq("Concept"), any());
    }

    @Test
    void exactMatchShouldRankHigherThanPartialMatch() {
        Node exactNode = createNode("Spring", "Exact match");
        Node partialNode = createNode("Spring Boot", "Starts with");
        Node containsNode = createNode("Learn Spring Framework", "Contains");

        when(workspaceRepository.searchAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(nodeRepository.searchNodesInAccessibleWorkspaces(any(), eq("Spring"), any()))
                .thenReturn(new PageImpl<>(List.of(containsNode, partialNode, exactNode)));

        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("Spring", "all", null, 0, 20);

        List<SearchResultResponse> content = result.getContent();
        assertEquals(3, content.size());
        assertEquals("Spring", content.get(0).getTitle());         // exact
        assertEquals("Spring Boot", content.get(1).getTitle());    // starts with
        assertEquals("Learn Spring Framework", content.get(2).getTitle()); // contains
    }

    @Test
    void paginationShouldWorkCorrectly() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            nodes.add(createNode("Node " + i, "desc " + i));
        }
        when(workspaceRepository.searchAccessibleWorkspaces(any(), eq("Node"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(nodeRepository.searchNodesInAccessibleWorkspaces(any(), eq("Node"), any()))
                .thenReturn(new PageImpl<>(nodes));

        // Page 0 of 2
        PageResponse<SearchResultResponse> page0 = graphSearchService.unifiedSearch("Node", "all", null, 0, 2);
        assertEquals(2, page0.getContent().size());
        assertEquals(5, page0.getTotalElements());
        assertEquals(3, page0.getTotalPages());
        assertFalse(page0.isLast());

        // Page 2 (last)
        PageResponse<SearchResultResponse> page2 = graphSearchService.unifiedSearch("Node", "all", null, 2, 2);
        assertEquals(1, page2.getContent().size());
        assertTrue(page2.isLast());
    }

    @Test
    void nodeResultContainsCorrectGraphInformation() {
        Node node = createNode("REST API", "HTTP endpoints");
        when(workspaceRepository.searchAccessibleWorkspaces(any(), eq("REST"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(nodeRepository.searchNodesInAccessibleWorkspaces(any(), eq("REST"), any()))
                .thenReturn(new PageImpl<>(List.of(node)));

        PageResponse<SearchResultResponse> result = graphSearchService.unifiedSearch("REST", "all", null, 0, 20);

        SearchResultResponse r = result.getContent().get(0);
        assertEquals(SearchResultResponse.ResultType.NODE, r.getResultType());
        assertEquals(workspace.getId(), r.getGraphId());
        assertEquals("Spring Boot Learning Path", r.getGraphTitle());
        assertEquals("Concept", r.getNodeTypeName());
        assertEquals("#22D3EE", r.getNodeTypeColor());
    }

    // --- Helper ---

    private Node createNode(String label, String description) {
        Node node = new Node();
        node.setId(UUID.randomUUID());
        node.setLabel(label);
        node.setWorkspace(workspace);
        node.setNodeType(nodeType);
        node.setPositionX(100.0);
        node.setPositionY(200.0);
        node.setCreatedAt(Instant.now());
        node.setUpdatedAt(Instant.now());
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("description", description);
        node.setAttributes(attrs);
        return node;
    }
}
