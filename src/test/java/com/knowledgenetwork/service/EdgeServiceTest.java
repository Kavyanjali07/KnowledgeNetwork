package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.ConcurrencyConflictException;
import com.knowledgenetwork.common.exception.DuplicateEdgeException;
import com.knowledgenetwork.common.exception.InvalidEdgeConnectionException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.EdgeMapper;
import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.EdgeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdgeServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private EdgeRepository edgeRepository;

    @Mock
    private EdgeTypeRepository edgeTypeRepository;

    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;

    @Mock
    private EdgeMapper edgeMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private EdgeService edgeService;

    private UUID userId;
    private User testUser;
    private UUID graphId;
    private Workspace testWorkspace;
    private NodeType testNodeType;
    private EdgeType testEdgeType;
    private Node sourceNode;
    private Node targetNode;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("user@example.com");

        graphId = UUID.randomUUID();
        testWorkspace = new Workspace("Test Graph", "Desc", testUser, Visibility.PRIVATE);
        testWorkspace.setId(graphId);

        testNodeType = new NodeType(testWorkspace, "Concept", "#22D3EE", "brain");
        testNodeType.setId(UUID.randomUUID());

        testEdgeType = new EdgeType(testWorkspace, "RELATED_TO", true);
        testEdgeType.setId(UUID.randomUUID());

        sourceNode = new Node(testWorkspace, testNodeType, "Source Node", Map.of());
        sourceNode.setId(UUID.randomUUID());

        targetNode = new Node(testWorkspace, testNodeType, "Target Node", Map.of());
        targetNode.setId(UUID.randomUUID());

        UserPrincipal principal = UserPrincipal.fromUser(testUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createEdge_Success() {
        EdgeCreateRequest request = new EdgeCreateRequest();
        request.setSourceNodeId(sourceNode.getId());
        request.setTargetNodeId(targetNode.getId());
        request.setEdgeTypeId(testEdgeType.getId());
        request.setLabel("Direct Link");

        when(workspaceRepository.findById(graphId)).thenReturn(Optional.of(testWorkspace));
        when(nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(sourceNode.getId(), testWorkspace)).thenReturn(Optional.of(sourceNode));
        when(nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(targetNode.getId(), testWorkspace)).thenReturn(Optional.of(targetNode));
        when(edgeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(testEdgeType.getId(), testWorkspace)).thenReturn(Optional.of(testEdgeType));
        when(edgeRepository.existsByWorkspaceAndSourceNodeAndTargetNodeAndEdgeTypeAndIsDeletedFalse(testWorkspace, sourceNode, targetNode, testEdgeType)).thenReturn(false);

        when(edgeRepository.save(any(Edge.class))).thenAnswer(inv -> {
            Edge e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EdgeResponse mapped = new EdgeResponse();
        mapped.setLabel("Direct Link");
        mapped.setRelationshipType("RELATED_TO");
        when(edgeMapper.toEdgeResponse(any(Edge.class))).thenReturn(mapped);

        EdgeResponse response = edgeService.createEdge(graphId, request);

        assertNotNull(response);
        assertEquals("Direct Link", response.getLabel());
        verify(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);
        verify(edgeRepository).save(any(Edge.class));
    }

    @Test
    void createEdge_SelfConnection_ThrowsInvalidEdgeConnectionException() {
        EdgeCreateRequest request = new EdgeCreateRequest();
        request.setSourceNodeId(sourceNode.getId());
        request.setTargetNodeId(sourceNode.getId()); // Self connection

        when(workspaceRepository.findById(graphId)).thenReturn(Optional.of(testWorkspace));
        when(nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(sourceNode.getId(), testWorkspace)).thenReturn(Optional.of(sourceNode));

        assertThrows(InvalidEdgeConnectionException.class, () -> edgeService.createEdge(graphId, request));
    }

    @Test
    void createEdge_DuplicateConnection_ThrowsDuplicateEdgeException() {
        EdgeCreateRequest request = new EdgeCreateRequest();
        request.setSourceNodeId(sourceNode.getId());
        request.setTargetNodeId(targetNode.getId());
        request.setEdgeTypeId(testEdgeType.getId());

        when(workspaceRepository.findById(graphId)).thenReturn(Optional.of(testWorkspace));
        when(nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(sourceNode.getId(), testWorkspace)).thenReturn(Optional.of(sourceNode));
        when(nodeRepository.findByIdAndWorkspaceAndIsDeletedFalse(targetNode.getId(), testWorkspace)).thenReturn(Optional.of(targetNode));
        when(edgeTypeRepository.findByIdAndWorkspaceAndIsDeletedFalse(testEdgeType.getId(), testWorkspace)).thenReturn(Optional.of(testEdgeType));
        when(edgeRepository.existsByWorkspaceAndSourceNodeAndTargetNodeAndEdgeTypeAndIsDeletedFalse(testWorkspace, sourceNode, targetNode, testEdgeType)).thenReturn(true);

        assertThrows(DuplicateEdgeException.class, () -> edgeService.createEdge(graphId, request));
    }

    @Test
    void createEdge_UnauthorizedUser_ThrowsAccessDeniedException() {
        EdgeCreateRequest request = new EdgeCreateRequest();
        request.setSourceNodeId(sourceNode.getId());
        request.setTargetNodeId(targetNode.getId());

        when(workspaceRepository.findById(graphId)).thenReturn(Optional.of(testWorkspace));
        doThrow(new AccessDeniedException("Denied"))
                .when(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);

        assertThrows(AccessDeniedException.class, () -> edgeService.createEdge(graphId, request));
    }

    @Test
    void updateEdge_StaleVersion_ThrowsConcurrencyConflictException() {
        UUID edgeId = UUID.randomUUID();
        Edge edge = new Edge(testWorkspace, testEdgeType, sourceNode, targetNode, 1.0, Map.of());
        edge.setId(edgeId);
        edge.setVersion(2L);

        when(edgeRepository.findById(edgeId)).thenReturn(Optional.of(edge));

        EdgeUpdateRequest updateReq = new EdgeUpdateRequest();
        updateReq.setVersion(1L); // mismatched version

        assertThrows(ConcurrencyConflictException.class, () -> edgeService.updateEdge(edgeId, updateReq));
    }

    @Test
    void deleteEdge_Success() {
        UUID edgeId = UUID.randomUUID();
        Edge edge = new Edge(testWorkspace, testEdgeType, sourceNode, targetNode, 1.0, Map.of());
        edge.setId(edgeId);

        when(edgeRepository.findById(edgeId)).thenReturn(Optional.of(edge));

        edgeService.deleteEdge(edgeId);

        assertTrue(edge.isDeleted());
        verify(workspaceSecurityValidator).validateWriteAccess(testWorkspace, userId);
        verify(edgeRepository).save(edge);
    }
}
