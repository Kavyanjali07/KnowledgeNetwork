package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.mapper.GraphMapper;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphPublishingSecurityTest {

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

    private User userA;
    private User userB;
    private UUID userAId;
    private UUID userBId;
    private Workspace privateWorkspaceA;
    private Workspace publicWorkspaceA;
    private UUID privateWorkspaceId;
    private UUID publicWorkspaceId;

    @BeforeEach
    void setUp() {
        userAId = UUID.randomUUID();
        userBId = UUID.randomUUID();

        userA = new User();
        userA.setId(userAId);
        userA.setEmail("userA@example.com");
        userA.setFirstName("User");
        userA.setLastName("A");

        userB = new User();
        userB.setId(userBId);
        userB.setEmail("userB@example.com");
        userB.setFirstName("User");
        userB.setLastName("B");

        privateWorkspaceId = UUID.randomUUID();
        privateWorkspaceA = new Workspace("Private Net A", "Description", userA, Visibility.PRIVATE);
        privateWorkspaceA.setId(privateWorkspaceId);
        privateWorkspaceA.setPublished(false);

        publicWorkspaceId = UUID.randomUUID();
        publicWorkspaceA = new Workspace("Public Net A", "Description", userA, Visibility.PUBLIC);
        publicWorkspaceA.setId(publicWorkspaceId);
        publicWorkspaceA.setPublished(true);
        publicWorkspaceA.setLicenseType(LicenseType.CC_BY_4_0);

        setAuthenticatedUser(userBId, "userB@example.com");
    }

    private void setAuthenticatedUser(UUID id, String email) {
        UserPrincipal principal = new UserPrincipal(id, email, "password", true, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("User B direct access to Private Workspace A should be rejected")
    void testDirectAccessToPrivateWorkspaceRejected() {
        when(workspaceRepository.findByIdAndIsDeletedFalse(privateWorkspaceId)).thenReturn(Optional.of(privateWorkspaceA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateReadAccess(privateWorkspaceA, userBId);

        assertThatThrownBy(() -> graphService.getGraphById(privateWorkspaceId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User does not have access");
    }

    @Test
    @DisplayName("User B provenance view of Private Workspace A should be rejected")
    void testProvenanceViewOfPrivateWorkspaceRejected() {
        when(workspaceRepository.findById(privateWorkspaceId)).thenReturn(Optional.of(privateWorkspaceA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateReadAccess(privateWorkspaceA, userBId);

        assertThatThrownBy(() -> graphVersioningService.getProvenance(privateWorkspaceId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User does not have access");
    }

    @Test
    @DisplayName("User B forking of Private Workspace A should be rejected")
    void testForkingPrivateWorkspaceRejected() {
        when(workspaceRepository.findById(privateWorkspaceId)).thenReturn(Optional.of(privateWorkspaceA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateReadAccess(privateWorkspaceA, userBId);

        UUID versionId = UUID.randomUUID();
        assertThatThrownBy(() -> graphVersioningService.createFork(privateWorkspaceId, versionId, "Fork Name", "Fork Desc"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Non-owner User B trying to publish Workspace A should be rejected")
    void testNonOwnerPublishingRejected() {
        when(workspaceRepository.findByIdAndIsDeletedFalse(privateWorkspaceId)).thenReturn(Optional.of(privateWorkspaceA));
        doThrow(new AccessDeniedException("User must be an owner of this workspace"))
                .when(workspaceSecurityValidator).validateOwnerAccess(privateWorkspaceA, userBId);

        assertThatThrownBy(() -> graphService.publishGraph(privateWorkspaceId, LicenseType.CC_BY_4_0, "Attribution"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Non-owner User B trying to unpublish Workspace A should be rejected")
    void testNonOwnerUnpublishingRejected() {
        when(workspaceRepository.findByIdAndIsDeletedFalse(publicWorkspaceId)).thenReturn(Optional.of(publicWorkspaceA));
        doThrow(new AccessDeniedException("User must be an owner of this workspace"))
                .when(workspaceSecurityValidator).validateOwnerAccess(publicWorkspaceA, userBId);

        assertThatThrownBy(() -> graphService.unpublishGraph(publicWorkspaceId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Forking rejected for ALL_RIGHTS_RESERVED, CC_BY_ND_4_0, CC_BY_NC_ND_4_0")
    void testForkingRejectedForNoDerivativesLicenses() {
        for (LicenseType noDerivLicense : List.of(LicenseType.ALL_RIGHTS_RESERVED, LicenseType.CC_BY_ND_4_0, LicenseType.CC_BY_NC_ND_4_0)) {
            publicWorkspaceA.setLicenseType(noDerivLicense);
            when(workspaceRepository.findById(publicWorkspaceId)).thenReturn(Optional.of(publicWorkspaceA));

            UUID versionId = UUID.randomUUID();
            assertThatThrownBy(() -> graphVersioningService.createFork(publicWorkspaceId, versionId, "Fork Name", "Fork Desc"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Derivation is not permitted");
        }
    }

    @Test
    @DisplayName("Forking allowed for CC_BY_4_0, CC_BY_SA_4_0, CC_BY_NC_4_0, CC_BY_NC_SA_4_0, CC0_1_0")
    void testForkingAllowedForDerivativeLicenses() {
        for (LicenseType derivLicense : List.of(LicenseType.CC_BY_4_0, LicenseType.CC_BY_SA_4_0, LicenseType.CC_BY_NC_4_0, LicenseType.CC_BY_NC_SA_4_0, LicenseType.CC0_1_0)) {
            publicWorkspaceA.setLicenseType(derivLicense);
            UUID versionId = UUID.randomUUID();
            GraphVersion version = new GraphVersion();
            version.setId(versionId);
            version.setWorkspace(publicWorkspaceA);
            GraphSnapshot snapshot = new GraphSnapshot();
            snapshot.setSnapshotData(java.util.Map.of("nodes", java.util.List.of(), "edges", java.util.List.of()));
            version.setSnapshot(snapshot);

            when(workspaceRepository.findById(publicWorkspaceId)).thenReturn(Optional.of(publicWorkspaceA));
            when(graphVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
            when(userRepository.findById(userBId)).thenReturn(Optional.of(userB));
            when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));
            when(graphForkRepository.save(any(GraphFork.class))).thenAnswer(inv -> inv.getArgument(0));

            GraphForkResponse response = graphVersioningService.createFork(publicWorkspaceId, versionId, "Fork Name", "Fork Desc");

            assertThat(response).isNotNull();
            assertThat(response.getSourceLicense()).isEqualTo(derivLicense);
        }
    }

    @Test
    @DisplayName("Forking CC_BY_NC_SA_4_0 preserves both NC and ShareAlike restrictions")
    void testForkingPreservesNonCommercialShareAlike() {
        publicWorkspaceA.setLicenseType(LicenseType.CC_BY_NC_SA_4_0);
        UUID versionId = UUID.randomUUID();
        GraphVersion version = new GraphVersion();
        version.setId(versionId);
        version.setWorkspace(publicWorkspaceA);
        GraphSnapshot snapshot = new GraphSnapshot();
        snapshot.setSnapshotData(java.util.Map.of("nodes", java.util.List.of(), "edges", java.util.List.of()));
        version.setSnapshot(snapshot);

        when(workspaceRepository.findById(publicWorkspaceId)).thenReturn(Optional.of(publicWorkspaceA));
        when(graphVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
        when(userRepository.findById(userBId)).thenReturn(Optional.of(userB));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));
        when(graphForkRepository.save(any(GraphFork.class))).thenAnswer(inv -> inv.getArgument(0));

        GraphForkResponse response = graphVersioningService.createFork(publicWorkspaceId, versionId, "Fork Name", "Fork Desc");

        assertThat(response.getSourceLicense()).isEqualTo(LicenseType.CC_BY_NC_SA_4_0);
        assertThat(response.getSourceLicense().requiresShareAlike()).isTrue();
        assertThat(response.getSourceLicense().allowsCommercial()).isFalse();
    }
}
