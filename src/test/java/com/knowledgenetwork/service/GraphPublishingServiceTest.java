package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.mapper.GraphMapper;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.LicenseType;
import com.knowledgenetwork.domain.model.*;
import com.knowledgenetwork.domain.payload.response.GraphForkResponse;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.repository.*;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphPublishingServiceTest {

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
    private GraphForkRepository graphForkRepository;
    @Mock
    private NetworkReferenceRepository networkReferenceRepository;
    @Mock
    private GraphVersionRepository graphVersionRepository;
    @Mock
    private GraphSnapshotRepository graphSnapshotRepository;
    @Mock
    private GraphForkOwnerRepository graphForkOwnerRepository;
    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;
    @Mock
    private GraphMapper graphMapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private GraphService graphService;

    @InjectMocks
    private GraphVersioningService graphVersioningService;

    private User testUser;
    private Workspace testWorkspace;
    private UUID userId;
    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("owner@example.com");
        testUser.setFirstName("Owner");
        testUser.setLastName("User");

        testWorkspace = new Workspace("Test Graph", "Description", testUser, Visibility.PRIVATE);
        testWorkspace.setId(workspaceId);

        UserPrincipal principal = new UserPrincipal(userId, "owner@example.com", "password", true, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("publishGraph should update publication state, license, and visibility to PUBLIC")
    void testPublishGraphSuccess() {
        when(workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)).thenReturn(Optional.of(testWorkspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GraphResponse mockResponse = new GraphResponse();
        mockResponse.setId(workspaceId);
        mockResponse.setPublished(true);
        mockResponse.setVisibility(Visibility.PUBLIC);
        mockResponse.setLicenseType(LicenseType.CC_BY_4_0);
        when(graphMapper.toGraphResponse(any(Workspace.class))).thenReturn(mockResponse);

        GraphResponse response = graphService.publishGraph(workspaceId, LicenseType.CC_BY_4_0, "Attribution test");

        assertThat(response).isNotNull();
        assertThat(testWorkspace.isPublished()).isTrue();
        assertThat(testWorkspace.getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(testWorkspace.getLicenseType()).isEqualTo(LicenseType.CC_BY_4_0);
        assertThat(testWorkspace.getCustomAttribution()).isEqualTo("Attribution test");

        verify(workspaceSecurityValidator).validateOwnerAccess(testWorkspace, userId);
        verify(auditLogService).recordEvent(eq(AuditAction.GRAPH_PUBLISHED), any(), eq(workspaceId), eq(workspaceId), any());
    }

    @Test
    @DisplayName("unpublishGraph should set isPublished to false and visibility to PRIVATE")
    void testUnpublishGraphSuccess() {
        testWorkspace.setPublished(true);
        testWorkspace.setVisibility(Visibility.PUBLIC);

        when(workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)).thenReturn(Optional.of(testWorkspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GraphResponse mockResponse = new GraphResponse();
        mockResponse.setId(workspaceId);
        mockResponse.setPublished(false);
        mockResponse.setVisibility(Visibility.PRIVATE);
        when(graphMapper.toGraphResponse(any(Workspace.class))).thenReturn(mockResponse);

        GraphResponse response = graphService.unpublishGraph(workspaceId);

        assertThat(response).isNotNull();
        assertThat(testWorkspace.isPublished()).isFalse();
        assertThat(testWorkspace.getVisibility()).isEqualTo(Visibility.PRIVATE);

        verify(workspaceSecurityValidator).validateOwnerAccess(testWorkspace, userId);
        verify(auditLogService).recordEvent(eq(AuditAction.GRAPH_UNPUBLISHED), any(), eq(workspaceId), eq(workspaceId), any());
    }

    @Test
    @DisplayName("createFork should fail if source license is ALL_RIGHTS_RESERVED")
    void testCreateForkFailsOnNoDerivativesLicense() {
        testWorkspace.setLicenseType(LicenseType.ALL_RIGHTS_RESERVED);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(testWorkspace));

        UUID versionId = UUID.randomUUID();

        assertThatThrownBy(() -> graphVersioningService.createFork(workspaceId, versionId, "Fork Name", "Fork Desc"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Derivation is not permitted");
    }

    @Test
    @DisplayName("createFork should inherit ShareAlike license CC_BY_SA_4_0 when source is CC_BY_SA_4_0")
    void testCreateForkEnforcesShareAlike() {
        testWorkspace.setLicenseType(LicenseType.CC_BY_SA_4_0);
        UUID versionId = UUID.randomUUID();

        GraphVersion mockVersion = new GraphVersion();
        mockVersion.setId(versionId);
        mockVersion.setWorkspace(testWorkspace);
        GraphSnapshot snapshot = new GraphSnapshot();
        snapshot.setSnapshotData(java.util.Map.of("nodes", java.util.List.of(), "edges", java.util.List.of()));
        mockVersion.setSnapshot(snapshot);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(testWorkspace));
        when(graphVersionRepository.findById(versionId)).thenReturn(Optional.of(mockVersion));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(graphForkRepository.save(any(GraphFork.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GraphForkResponse response = graphVersioningService.createFork(workspaceId, versionId, "Fork Name", "Fork Desc");

        assertThat(response).isNotNull();
        assertThat(response.getSourceLicense()).isEqualTo(LicenseType.CC_BY_SA_4_0);
        assertThat(response.isDerivative()).isTrue();
    }
}
