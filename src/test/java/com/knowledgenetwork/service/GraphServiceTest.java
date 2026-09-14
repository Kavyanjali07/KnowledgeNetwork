package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.ConcurrencyConflictException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.GraphMapper;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.GraphCreateRequest;
import com.knowledgenetwork.domain.payload.request.GraphUpdateRequest;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NodeTypeRepository nodeTypeRepository;

    @Mock
    private EdgeTypeRepository edgeTypeRepository;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private EdgeRepository edgeRepository;

    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;

    @Mock
    private GraphMapper graphMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private GraphService graphService;

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        com.knowledgenetwork.security.UserPrincipal principal = com.knowledgenetwork.security.UserPrincipal.fromUser(testUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createGraph_Success() {
        GraphCreateRequest request = new GraphCreateRequest("Test Graph", "Description", Visibility.PUBLIC);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace w = invocation.getArgument(0);
            w.setId(UUID.randomUUID());
            return w;
        });

        GraphResponse mappedResponse = new GraphResponse();
        mappedResponse.setTitle("Test Graph");
        mappedResponse.setVisibility(Visibility.PUBLIC);
        when(graphMapper.toGraphResponse(any(Workspace.class))).thenReturn(mappedResponse);

        GraphResponse result = graphService.createGraph(request);

        assertNotNull(result);
        assertEquals("Test Graph", result.getTitle());
        assertEquals(Visibility.PUBLIC, result.getVisibility());
    }

    @Test
    void getGraphById_NotFound_ThrowsException() {
        UUID graphId = UUID.randomUUID();
        when(workspaceRepository.findByIdAndIsDeletedFalse(graphId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> graphService.getGraphById(graphId));
    }

    @Test
    void updateGraph_VersionMismatch_ThrowsConcurrencyConflictException() {
        UUID graphId = UUID.randomUUID();
        Workspace existingWorkspace = new Workspace("Old Title", "Old Desc", testUser, Visibility.PRIVATE);
        existingWorkspace.setId(graphId);
        existingWorkspace.setVersion(2L);

        when(workspaceRepository.findByIdAndIsDeletedFalse(graphId)).thenReturn(Optional.of(existingWorkspace));

        GraphUpdateRequest request = new GraphUpdateRequest("New Title", "New Desc", Visibility.PUBLIC, 1L);

        assertThrows(ConcurrencyConflictException.class, () -> graphService.updateGraph(graphId, request));
    }

    @Test
    void deleteGraph_Success() {
        UUID graphId = UUID.randomUUID();
        Workspace existingWorkspace = new Workspace("Graph to delete", "Desc", testUser, Visibility.PRIVATE);
        existingWorkspace.setId(graphId);

        when(workspaceRepository.findByIdAndIsDeletedFalse(graphId)).thenReturn(Optional.of(existingWorkspace));

        graphService.deleteGraph(graphId);

        verify(workspaceRepository).save(existingWorkspace);
        assertEquals(true, existingWorkspace.isDeleted());
    }
}
