package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.GraphTraversalRequest;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.service.RelationshipService;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GraphTraversalIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private NodeTypeRepository nodeTypeRepository;

    @Autowired
    private EdgeTypeRepository edgeTypeRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private EdgeRepository edgeRepository;

    @Autowired
    private RelationshipService relationshipService;

    private User owner;
    private Workspace workspace;
    private Node nodeA, nodeB, nodeC, nodeD, nodeE;
    private NodeType nodeType;
    private EdgeType edgeType;

    @BeforeEach
    void setUp() {
        owner = userRepository.saveAndFlush(TestFixtures.createUser("graph_owner_" + UUID.randomUUID() + "@example.com", "Graph", "Owner"));
        workspace = workspaceRepository.saveAndFlush(TestFixtures.createWorkspace("Traversal WS", owner));

        // Authenticate owner context
        UserPrincipal principal = UserPrincipal.fromUser(owner);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        nodeType = nodeTypeRepository.saveAndFlush(TestFixtures.createNodeType(workspace, "Concept"));
        edgeType = edgeTypeRepository.saveAndFlush(TestFixtures.createEdgeType(workspace, "DEPENDS_ON"));

        // Create linear graph: A -> B -> C -> D -> E
        nodeA = nodeRepository.saveAndFlush(TestFixtures.createNode(workspace, nodeType, "A", 0.0, 0.0));
        nodeB = nodeRepository.saveAndFlush(TestFixtures.createNode(workspace, nodeType, "B", 1.0, 1.0));
        nodeC = nodeRepository.saveAndFlush(TestFixtures.createNode(workspace, nodeType, "C", 2.0, 2.0));
        nodeD = nodeRepository.saveAndFlush(TestFixtures.createNode(workspace, nodeType, "D", 3.0, 3.0));
        nodeE = nodeRepository.saveAndFlush(TestFixtures.createNode(workspace, nodeType, "E", 4.0, 4.0));

        edgeRepository.saveAndFlush(TestFixtures.createEdge(workspace, edgeType, nodeA, nodeB));
        edgeRepository.saveAndFlush(TestFixtures.createEdge(workspace, edgeType, nodeB, nodeC));
        edgeRepository.saveAndFlush(TestFixtures.createEdge(workspace, edgeType, nodeC, nodeD));
        edgeRepository.saveAndFlush(TestFixtures.createEdge(workspace, edgeType, nodeD, nodeE));
    }

    @Test
    void traverseLinearGraph_ShouldTraverseCorrectDepthAndReachTargetNodes() {
        GraphTraversalRequest request = new GraphTraversalRequest();
        request.setWorkspaceId(workspace.getId());
        request.setRootNodeId(nodeA.getId());
        request.setMaxDepth(2);
        request.setDirection(GraphTraversalRequest.TraversalDirection.OUTGOING);

        Map<String, Object> result = relationshipService.traverse(request);

        assertNotNull(result);
        List<?> nodes = (List<?>) result.get("nodes");
        assertNotNull(nodes);
        assertTrue(nodes.size() >= 3, "Expected traversal depth 2 to include A, B, C");
    }

    @Test
    void detectCycles_ShouldSafelyDetectCyclesAndNotInfiniteLoop() {
        // Add edge E -> A to create cycle A -> B -> C -> D -> E -> A
        edgeRepository.saveAndFlush(TestFixtures.createEdge(workspace, edgeType, nodeE, nodeA));

        Map<String, Object> cycles = relationshipService.detectCycles(workspace.getId());

        assertNotNull(cycles);
        assertEquals(workspace.getId().toString(), cycles.get("workspaceId"));
        int cycleCount = (int) cycles.get("cycleCount");
        assertTrue(cycleCount >= 1, "Expected cycle detection to find cyclic edge E -> A");
    }
}
