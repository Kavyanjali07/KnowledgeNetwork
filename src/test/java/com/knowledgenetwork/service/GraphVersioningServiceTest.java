package com.knowledgenetwork.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.model.GraphFork;
import com.knowledgenetwork.domain.model.GraphForkOwner;
import com.knowledgenetwork.domain.model.GraphSnapshot;
import com.knowledgenetwork.domain.model.GraphVersion;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.response.GraphForkResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.GraphForkOwnerRepository;
import com.knowledgenetwork.repository.GraphForkRepository;
import com.knowledgenetwork.repository.GraphSnapshotRepository;
import com.knowledgenetwork.repository.GraphVersionRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class GraphVersioningServiceTest {

    @Mock
    private GraphSnapshotRepository graphSnapshotRepository;

    @Mock
    private GraphVersionRepository graphVersionRepository;

    @Mock
    private GraphForkRepository graphForkRepository;

    @Mock
    private GraphForkOwnerRepository graphForkOwnerRepository;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private EdgeRepository edgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private com.knowledgenetwork.security.WorkspaceSecurityValidator workspaceSecurityValidator;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private GraphVersioningService graphVersioningService;

    private void mockSecurityContext() {
        UserPrincipal principal = UserPrincipal.fromUser(new User() {{
            setId(UUID.randomUUID());
            setEmail("test@example.com");
            setPasswordHash("hash");
            setFirstName("Test");
            setLastName("User");
        }});
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    void createSnapshotShouldPersistSnapshotAndInitialVersion() {
        mockSecurityContext();

        Workspace workspace = new Workspace();
        workspace.setId(UUID.randomUUID());

        Node node = new Node();
        node.setId(UUID.randomUUID());
        node.setWorkspace(workspace);
        node.setLabel("Alpha");
        node.setAttributes(Map.of("kind", "concept"));

        Edge edge = new Edge();
        edge.setId(UUID.randomUUID());
        edge.setWorkspace(workspace);
        edge.setWeight(1.5);
        edge.setAttributes(Map.of("relation", "uses"));

        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(graphVersionRepository.findTopByWorkspaceOrderByVersionNumberDesc(workspace)).thenReturn(Optional.empty());
        when(nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(node)));
        when(edgeRepository.findByWorkspaceAndIsDeletedFalse(workspace, org.springframework.data.domain.Pageable.unpaged())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(edge)));
        when(graphSnapshotRepository.save(any(GraphSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(graphVersionRepository.save(any(GraphVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GraphVersion version = graphVersioningService.createSnapshot(workspace.getId(), "initial", "seed");

        assertNotNull(version);
        assertEquals(1L, version.getVersionNumber());
        verify(graphSnapshotRepository).save(any(GraphSnapshot.class));
        verify(graphVersionRepository).save(any(GraphVersion.class));
    }

    @Test
    void createForkShouldCreateForkAndOwner() {
        mockSecurityContext();

        Workspace workspace = new Workspace();
        workspace.setId(UUID.randomUUID());

        User owner = new User();
        owner.setId(UUID.randomUUID());

        GraphVersion sourceVersion = new GraphVersion();
        sourceVersion.setId(UUID.randomUUID());
        sourceVersion.setVersionNumber(3L);
        sourceVersion.setWorkspace(workspace);
        GraphSnapshot snapshot = new GraphSnapshot();
        snapshot.setSnapshotData(Map.of("nodes", List.of(), "edges", List.of()));
        sourceVersion.setSnapshot(snapshot);

        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(graphVersionRepository.findById(sourceVersion.getId())).thenReturn(Optional.of(sourceVersion));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(graphForkRepository.save(any(GraphFork.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(graphForkOwnerRepository.save(any(GraphForkOwner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        GraphForkResponse fork = graphVersioningService.createFork(workspace.getId(), sourceVersion.getId(), "exploration", "branch for experiments");

        assertNotNull(fork);
        assertEquals("exploration", fork.getName());
        assertEquals(sourceVersion.getId(), fork.getSourceVersionId());
        verify(graphForkRepository).save(any(GraphFork.class));
        verify(graphForkOwnerRepository).save(any(GraphForkOwner.class));
    }
}
