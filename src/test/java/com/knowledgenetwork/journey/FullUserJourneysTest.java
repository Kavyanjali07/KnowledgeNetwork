package com.knowledgenetwork.journey;

import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.GraphVersion;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.Notification;
import com.knowledgenetwork.domain.model.SocialComment;
import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.domain.payload.request.EdgeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceCreateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import com.knowledgenetwork.domain.payload.response.GraphForkResponse;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.NotificationRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.service.GraphVersioningService;
import com.knowledgenetwork.service.RelationshipService;
import com.knowledgenetwork.service.SocialService;
import com.knowledgenetwork.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FullUserJourneysTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private NodeTypeRepository nodeTypeRepository;

    @Autowired
    private EdgeTypeRepository edgeTypeRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private EdgeRepository edgeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private SocialService socialService;

    @Autowired
    private GraphVersioningService graphVersioningService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setEmail("usera_" + UUID.randomUUID() + "@example.com");
        userA.setPasswordHash("hash123");
        userA.setFirstName("Alice");
        userA.setLastName("User");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setEmail("userb_" + UUID.randomUUID() + "@example.com");
        userB.setPasswordHash("hash123");
        userB.setFirstName("Bob");
        userB.setLastName("User");
        userB = userRepository.save(userB);
    }

    private void authenticateUser(User user) {
        UserPrincipal principal = UserPrincipal.fromUser(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Journey 1: Register/Login -> Dashboard -> Create Graph -> Add Nodes -> Connect Nodes -> Refresh/Persist")
    void testJourney1_GraphCreationAndPersistence() {
        // Step 1: User A logins
        authenticateUser(userA);

        // Step 2: Create Graph (Workspace)
        WorkspaceCreateRequest createReq = new WorkspaceCreateRequest();
        createReq.setName("Neural Knowledge Graph");
        createReq.setDescription("A graph representing neural network architectures");
        WorkspaceResponse createdWs = workspaceService.createWorkspace(createReq);
        assertNotNull(createdWs);
        UUID workspaceId = createdWs.getId();

        // Retrieve seeded node and edge types
        Workspace ws = workspaceRepository.findById(workspaceId).orElseThrow();
        NodeType nodeType = nodeTypeRepository.findByWorkspaceAndIsDeletedFalse(ws, Pageable.unpaged()).getContent().get(0);
        EdgeType edgeType = edgeTypeRepository.findByWorkspaceAndIsDeletedFalse(ws, Pageable.unpaged()).getContent().get(0);

        // Step 3: Add Nodes
        NodeCreateRequest node1Req = new NodeCreateRequest();
        node1Req.setWorkspaceId(workspaceId);
        node1Req.setLabel("Transformer Core");
        node1Req.setNodeTypeId(nodeType.getId());
        NodeResponse node1 = relationshipService.createNode(node1Req);

        NodeCreateRequest node2Req = new NodeCreateRequest();
        node2Req.setWorkspaceId(workspaceId);
        node2Req.setLabel("Self Attention Mechanism");
        node2Req.setNodeTypeId(nodeType.getId());
        NodeResponse node2 = relationshipService.createNode(node2Req);

        // Step 4: Connect Nodes
        EdgeCreateRequest edgeReq = new EdgeCreateRequest();
        edgeReq.setWorkspaceId(workspaceId);
        edgeReq.setEdgeTypeId(edgeType.getId());
        edgeReq.setSourceNodeId(node1.getId());
        edgeReq.setTargetNodeId(node2.getId());
        edgeReq.setWeight(2.5);
        EdgeResponse edge = relationshipService.createEdge(edgeReq);

        // Step 5: Refresh (Query workspace and graph contents)
        Workspace reloadedWorkspace = workspaceRepository.findById(workspaceId).orElseThrow();
        List<Node> nodes = nodeRepository.findByWorkspaceAndIsDeletedFalse(reloadedWorkspace, Pageable.unpaged()).getContent();
        List<Edge> edges = edgeRepository.findByWorkspaceAndIsDeletedFalse(reloadedWorkspace, Pageable.unpaged()).getContent();

        // Step 6: Verify Graph still exists with connected nodes & edges
        assertEquals("Neural Knowledge Graph", reloadedWorkspace.getName());
        assertEquals(2, nodes.size());
        assertEquals(1, edges.size());
        assertEquals(node1.getId(), edges.get(0).getSourceNode().getId());
        assertEquals(node2.getId(), edges.get(0).getTargetNode().getId());
    }

    @Test
    @DisplayName("Journey 2: User A creates graph -> User B searches & opens -> User B likes & comments -> User A receives notification")
    void testJourney2_SocialDiscoveryAndNotifications() {
        // Step 1: User A creates graph & post
        authenticateUser(userA);
        WorkspaceCreateRequest wsReq = new WorkspaceCreateRequest();
        wsReq.setName("Public Physics Knowledge Base");
        wsReq.setDescription("Open access physics formulas");
        WorkspaceResponse ws = workspaceService.createWorkspace(wsReq);

        Workspace workspace = workspaceRepository.findById(ws.getId()).orElseThrow();
        // Add User B as member/collaborator of the workspace with EDITOR role to participate in discussions
        workspaceMemberRepository.save(new WorkspaceMember(workspace, userB, WorkspaceRole.EDITOR));

        SocialPost post = socialService.createPost(ws.getId(), "Welcome to the physics graph!", "WORKSPACE", ws.getId());
        assertNotNull(post);

        // Step 2: User B searches, opens graph, likes and comments
        authenticateUser(userB);

        // User B likes User A's post
        socialService.likePost(post.getId());

        // User B comments on User A's post
        SocialComment comment = socialService.addComment(post.getId(), "Incredible visualization!", null);
        assertNotNull(comment);

        // Step 3: Switch back to User A and check Notifications
        authenticateUser(userA);
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(userA, Pageable.unpaged()).getContent();

        assertFalse(notifications.isEmpty(), "User A should receive notifications for Bob's like/comment");
        assertTrue(notifications.stream().anyMatch(n -> n.getType().equals("LIKE") || n.getType().equals("COMMENT")));
    }

    @Test
    @DisplayName("Journey 3: User B forks User A's graph -> New graph created -> User B edits fork -> Original graph unchanged")
    void testJourney3_GraphForking() {
        // Step 1: User A creates a graph and initial snapshot
        authenticateUser(userA);
        WorkspaceCreateRequest wsReq = new WorkspaceCreateRequest();
        wsReq.setName("Original AI Graph");
        WorkspaceResponse wsA = workspaceService.createWorkspace(wsReq);

        Workspace workspaceA = workspaceRepository.findById(wsA.getId()).orElseThrow();
        workspaceMemberRepository.save(new WorkspaceMember(workspaceA, userB, WorkspaceRole.VIEWER));

        GraphVersion versionA = graphVersioningService.createSnapshot(wsA.getId(), "v1.0", "Initial release");

        // Step 2: User B forks User A's graph version
        authenticateUser(userB);
        GraphForkResponse forkResponse = graphVersioningService.createFork(
                wsA.getId(), versionA.getId(), "Bob's AI Fork", "Experimenting with new nodes");

        assertNotNull(forkResponse);

        // Verify new fork graph exists under User B
        Workspace forkedWorkspace = workspaceRepository.findById(forkResponse.getWorkspaceId()).orElseThrow();
        assertEquals("Bob's AI Fork", forkedWorkspace.getName());
        assertEquals(userB.getId(), forkedWorkspace.getOwner().getId());

        // Step 3: User B edits the fork (create node type & add node to fork)
        NodeType forkNodeType = new NodeType();
        forkNodeType.setName("Fork Experimental Node");
        forkNodeType.setColorCode("#10B981");
        forkNodeType.setWorkspace(forkedWorkspace);
        forkNodeType = nodeTypeRepository.save(forkNodeType);

        NodeCreateRequest forkNodeReq = new NodeCreateRequest();
        forkNodeReq.setWorkspaceId(forkedWorkspace.getId());
        forkNodeReq.setLabel("Quantum Computing Node");
        forkNodeReq.setNodeTypeId(forkNodeType.getId());
        relationshipService.createNode(forkNodeReq);

        // Step 4: Verify original graph remains unchanged
        Workspace originalWorkspace = workspaceRepository.findById(wsA.getId()).orElseThrow();
        List<Node> originalNodes = nodeRepository.findByWorkspaceAndIsDeletedFalse(originalWorkspace, Pageable.unpaged()).getContent();
        List<Node> forkedNodes = nodeRepository.findByWorkspaceAndIsDeletedFalse(forkedWorkspace, Pageable.unpaged()).getContent();

        assertEquals(0, originalNodes.size(), "Original graph nodes must remain unchanged");
        assertEquals(1, forkedNodes.size(), "Forked graph should contain the new node");
    }

    @Test
    @DisplayName("Journey 4: User edits graph -> Version created -> Make another change -> Restore previous version -> State restored")
    void testJourney4_VersioningAndRollback() {
        // Step 1: User A creates graph & node 1
        authenticateUser(userA);
        WorkspaceCreateRequest wsReq = new WorkspaceCreateRequest();
        wsReq.setName("Evolving Graph");
        WorkspaceResponse ws = workspaceService.createWorkspace(wsReq);
        UUID workspaceId = ws.getId();

        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow();
        NodeType nodeType = nodeTypeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged()).getContent().get(0);

        NodeCreateRequest node1Req = new NodeCreateRequest();
        node1Req.setWorkspaceId(workspaceId);
        node1Req.setLabel("Node Version 1");
        node1Req.setNodeTypeId(nodeType.getId());
        NodeResponse node1 = relationshipService.createNode(node1Req);

        // Step 2: Create initial snapshot / version 1
        GraphVersion version1 = graphVersioningService.createSnapshot(workspaceId, "State V1", "Single node state");
        assertEquals(1L, version1.getVersionNumber());

        // Step 3: Make another change (Add Node 2)
        NodeCreateRequest node2Req = new NodeCreateRequest();
        node2Req.setWorkspaceId(workspaceId);
        node2Req.setLabel("Node Version 2");
        node2Req.setNodeTypeId(nodeType.getId());
        relationshipService.createNode(node2Req);

        List<Node> currentNodes = nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged()).getContent();
        assertEquals(2, currentNodes.size());

        // Create version 2 snapshot
        GraphVersion version2 = graphVersioningService.createSnapshot(workspaceId, "State V2", "Two node state");
        assertEquals(2L, version2.getVersionNumber());

        // Step 4: Restore previous version (Version 1)
        GraphVersion restoredVersion = graphVersioningService.restoreVersion(workspaceId, version1.getId());
        assertNotNull(restoredVersion);

        // Step 5: Verify graph returns to previous state (1 node)
        List<Node> restoredNodes = nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged()).getContent();
        assertEquals(1, restoredNodes.size(), "Graph should roll back to single node state of Version 1");
        assertEquals("Node Version 1", restoredNodes.get(0).getLabel());
    }
}
