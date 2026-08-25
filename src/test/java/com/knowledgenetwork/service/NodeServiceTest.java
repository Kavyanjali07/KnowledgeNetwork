package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.ConcurrencyConflictException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.NodeMapper;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeServiceTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private NodeTypeRepository nodeTypeRepository;

    @Mock
    private EdgeRepository edgeRepository;

    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;

    @Mock
    private NodeMapper nodeMapper;

    @InjectMocks
    private NodeService nodeService;

    private UUID userId;
    private User testUser;
    private UUID graphId;
    private Workspace testWorkspace;
    private NodeType testNodeType;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("owner@example.com");

        graphId = UUID.randomUUID();
        testWorkspace = new Workspace("Test Graph", "Desc", testUser, Visibility.PRIVATE);
        testWorkspace.setId(graphId);

        testNodeType = new NodeType(testWorkspace, "Concept", "#22D3EE", "brain");
        testNodeType.setId(UUID.randomUUID());

        UserPrincipal principal = UserPrincipal.fromUser(testUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createNode_Success() {
        NodeCreateRequest request = new NodeCreateRequest(graphId, testNodeType.getId(), "New Node", Map.of("description", "Content"));
        request.setPositionX(100.0);
        request.setPositionY(200.0);

        when(workspaceRepository.findByIdAndIsDeletedFalse(graphId)).thenReturn(Optional.of(testWorkspace));
        when(nodeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(testNodeType.getId(), testWorkspace))
                .thenReturn(Optional.of(testNodeType));
        when(nodeRepository.save(any(Node.class))).thenAnswer(invocation -> {
            Node n = invocation.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        NodeResponse mapped = new NodeResponse();
        mapped.setLabel("New Node");
        mapped.setPositionX(100.0);
        mapped.setPositionY(200.0);
        when(nodeMapper.toNodeResponse(any(Node.class))).thenReturn(mapped);

        NodeResponse response = nodeService.createNode(graphId, request);

        assertNotNull(response);
        assertEquals("New Node", response.getLabel());
        verify(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);
        verify(nodeRepository).save(any(Node.class));
    }

    @Test
    void createNode_NonExistentGraph_ThrowsResourceNotFound() {
        NodeCreateRequest request = new NodeCreateRequest(graphId, testNodeType.getId(), "Node", null);
        when(workspaceRepository.findByIdAndIsDeletedFalse(graphId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> nodeService.createNode(graphId, request));
    }

    @Test
    void createNode_UnauthorizedUser_ThrowsAccessDenied() {
        NodeCreateRequest request = new NodeCreateRequest(graphId, testNodeType.getId(), "Node", null);
        when(workspaceRepository.findByIdAndIsDeletedFalse(graphId)).thenReturn(Optional.of(testWorkspace));
        doThrow(new AccessDeniedException("Denied"))
                .when(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);

        assertThrows(AccessDeniedException.class, () -> nodeService.createNode(graphId, request));
    }

    @Test
    void getGraphNodes_Success() {
        when(workspaceRepository.findByIdAndIsDeletedFalse(graphId)).thenReturn(Optional.of(testWorkspace));

        Node node1 = new Node(testWorkspace, testNodeType, "Node 1", Map.of());
        node1.setId(UUID.randomUUID());
        Page<Node> page = new PageImpl<>(List.of(node1));

        when(nodeRepository.findByWorkspaceAndIsDeletedFalse(eq(testWorkspace), any(Pageable.class)))
                .thenReturn(page);

        NodeResponse resp1 = new NodeResponse();
        resp1.setLabel("Node 1");
        when(nodeMapper.toNodeResponse(node1)).thenReturn(resp1);

        Page<NodeResponse> result = nodeService.getGraphNodes(graphId, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Node 1", result.getContent().get(0).getLabel());
        verify(workspaceSecurityValidator).validateReadAccess(testWorkspace, userId);
    }

    @Test
    void getNodeById_Success() {
        UUID nodeId = UUID.randomUUID();
        Node node = new Node(testWorkspace, testNodeType, "Single Node", Map.of());
        node.setId(nodeId);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));

        NodeResponse mapped = new NodeResponse();
        mapped.setId(nodeId);
        mapped.setLabel("Single Node");
        when(nodeMapper.toNodeResponse(node)).thenReturn(mapped);

        NodeResponse result = nodeService.getNodeById(nodeId);

        assertNotNull(result);
        assertEquals("Single Node", result.getLabel());
        verify(workspaceSecurityValidator).validateReadAccess(testWorkspace, userId);
    }

    @Test
    void updateNode_VersionMismatch_ThrowsConcurrencyConflict() {
        UUID nodeId = UUID.randomUUID();
        Node node = new Node(testWorkspace, testNodeType, "Old Node", Map.of());
        node.setId(nodeId);
        node.setVersion(2L);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));

        NodeUpdateRequest updateReq = new NodeUpdateRequest(null, "Updated Title", Map.of(), 1L); // mismatched version 1 vs 2

        assertThrows(ConcurrencyConflictException.class, () -> nodeService.updateNode(nodeId, updateReq));
    }

    @Test
    void updateNode_Success() {
        UUID nodeId = UUID.randomUUID();
        Node node = new Node(testWorkspace, testNodeType, "Old Node", Map.of());
        node.setId(nodeId);
        node.setVersion(1L);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(nodeRepository.save(any(Node.class))).thenAnswer(inv -> inv.getArgument(0));

        NodeResponse mapped = new NodeResponse();
        mapped.setLabel("Updated Node");
        when(nodeMapper.toNodeResponse(any(Node.class))).thenReturn(mapped);

        NodeUpdateRequest updateReq = new NodeUpdateRequest(null, "Updated Node", Map.of("description", "New content"), 1L);

        NodeResponse result = nodeService.updateNode(nodeId, updateReq);

        assertNotNull(result);
        assertEquals("Updated Node", result.getLabel());
        verify(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);
        verify(nodeRepository).save(node);
    }

    @Test
    void deleteNode_Success() {
        UUID nodeId = UUID.randomUUID();
        Node node = new Node(testWorkspace, testNodeType, "To Delete", Map.of());
        node.setId(nodeId);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(edgeRepository.findByWorkspaceAndIsDeletedFalse(eq(testWorkspace), any(Pageable.class)))
                .thenReturn(Page.empty());

        nodeService.deleteNode(nodeId);

        assertTrue(node.isDeleted());
        verify(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);
        verify(nodeRepository).save(node);
    }

    @Test
    void deleteNode_UnauthorizedUser_ThrowsAccessDenied() {
        UUID nodeId = UUID.randomUUID();
        Node node = new Node(testWorkspace, testNodeType, "Node", Map.of());
        node.setId(nodeId);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        doThrow(new AccessDeniedException("Denied"))
                .when(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);

        assertThrows(AccessDeniedException.class, () -> nodeService.deleteNode(nodeId));
    }
}
