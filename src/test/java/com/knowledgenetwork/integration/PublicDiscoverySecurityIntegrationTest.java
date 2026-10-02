package com.knowledgenetwork.integration;

import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.GraphMapper;
import com.knowledgenetwork.domain.enums.LicenseType;
import com.knowledgenetwork.domain.model.*;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.domain.payload.response.PublicConceptExploreResponse;
import com.knowledgenetwork.domain.payload.response.PublicCreatorProfileResponse;
import com.knowledgenetwork.repository.*;
import com.knowledgenetwork.service.PublicDiscoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicDiscoverySecurityIntegrationTest {

    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EdgeRepository edgeRepository;
    @Mock
    private GraphForkRepository graphForkRepository;
    @Mock
    private NetworkReferenceRepository networkReferenceRepository;
    @Mock
    private GraphMapper graphMapper;

    @InjectMocks
    private PublicDiscoveryService publicDiscoveryService;

    private User creator;
    private Workspace publicWorkspace;
    private Workspace privateWorkspace;
    private Workspace unpublishedWorkspace;
    private Workspace deletedWorkspace;
    private Node publicNode;
    private Node privateNode;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(UUID.randomUUID());
        creator.setEmail("alice@example.com");
        creator.setFirstName("Alice");
        creator.setLastName("Dev");

        publicWorkspace = new Workspace("Public Network", "Public Desc", creator, Visibility.PUBLIC);
        publicWorkspace.setId(UUID.randomUUID());
        publicWorkspace.setPublished(true);
        publicWorkspace.setLicenseType(LicenseType.CC_BY_4_0);

        privateWorkspace = new Workspace("Private Network", "Secret Desc", creator, Visibility.PRIVATE);
        privateWorkspace.setId(UUID.randomUUID());
        privateWorkspace.setPublished(false);

        unpublishedWorkspace = new Workspace("Unpublished Net", "Draft Desc", creator, Visibility.PUBLIC);
        unpublishedWorkspace.setId(UUID.randomUUID());
        unpublishedWorkspace.setPublished(false);

        deletedWorkspace = new Workspace("Deleted Net", "Deleted Desc", creator, Visibility.PUBLIC);
        deletedWorkspace.setId(UUID.randomUUID());
        deletedWorkspace.setPublished(true);
        deletedWorkspace.setDeleted(true);

        publicNode = new Node();
        publicNode.setId(UUID.randomUUID());
        publicNode.setLabel("Recursion");
        publicNode.setWorkspace(publicWorkspace);

        privateNode = new Node();
        privateNode.setId(UUID.randomUUID());
        privateNode.setLabel("Secret Algorithm");
        privateNode.setWorkspace(privateWorkspace);
    }

    @Test
    @DisplayName("exploreConcepts returns only nodes from public, published, active networks")
    void testExploreConcepts_ExcludesPrivateUnpublishedDeleted() {
        when(nodeRepository.findPublicNodesByLabel(eq("Recursion"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(publicNode)));
        when(nodeRepository.findPublicRelatedConceptLabels(eq("Recursion"), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{"Base Case", 2L}));

        PublicConceptExploreResponse response = publicDiscoveryService.exploreConcepts("Recursion", 0, 20);

        assertThat(response).isNotNull();
        assertThat(response.getConceptLabel()).isEqualTo("Recursion");
        assertThat(response.getTotalPublicNetworks()).isEqualTo(1);
        assertThat(response.getOccurrences()).hasSize(1);
        assertThat(response.getOccurrences().get(0).getNetworkTitle()).isEqualTo("Public Network");
        assertThat(response.getRelatedConcepts()).hasSize(1);
        assertThat(response.getRelatedConcepts().get(0).getLabel()).isEqualTo("Base Case");
    }

    @Test
    @DisplayName("exploreConcepts for private concept query returns empty results when no public nodes match")
    void testExploreConcepts_PrivateConceptQueryReturnsEmpty() {
        when(nodeRepository.findPublicNodesByLabel(eq("Secret Algorithm"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        PublicConceptExploreResponse response = publicDiscoveryService.exploreConcepts("Secret Algorithm", 0, 20);

        assertThat(response.getTotalPublicNetworks()).isEqualTo(0);
        assertThat(response.getOccurrences()).isEmpty();
        assertThat(response.getRelatedConcepts()).isEmpty();
    }

    @Test
    @DisplayName("discoverPublicNetworks excludes private, unpublished, and deleted networks")
    void testDiscoverPublicNetworks_ExcludesPrivateUnpublishedDeleted() {
        GraphResponse responseDto = new GraphResponse();
        responseDto.setId(publicWorkspace.getId());
        responseDto.setTitle(publicWorkspace.getName());

        when(workspaceRepository.findPublicWorkspaces(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(publicWorkspace)));
        when(graphMapper.toGraphResponse(publicWorkspace)).thenReturn(responseDto);

        PageResponse<GraphResponse> result = publicDiscoveryService.discoverPublicNetworks(null, null, null, null, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Public Network");
    }

    @Test
    @DisplayName("getPublicCreatorProfile calculates metrics excluding private networks")
    void testGetPublicCreatorProfile_OnlyIncludesPublicNetworks() {
        GraphResponse publicResponse = new GraphResponse();
        publicResponse.setId(publicWorkspace.getId());
        publicResponse.setTitle(publicWorkspace.getName());

        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(creator));
        when(workspaceRepository.findByOwnerAndIsPublishedTrueAndVisibilityAndIsDeletedFalse(creator, Visibility.PUBLIC))
                .thenReturn(List.of(publicWorkspace));
        when(graphMapper.toGraphResponse(publicWorkspace)).thenReturn(publicResponse);
        when(nodeRepository.findTopConceptsByCreator(eq(creator), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{"Recursion", 5L}, new Object[]{"Graph Theory", 3L}));

        PublicCreatorProfileResponse profile = publicDiscoveryService.getPublicCreatorProfile("alice");

        assertThat(profile).isNotNull();
        assertThat(profile.getUsername()).isEqualTo("alice");
        assertThat(profile.getTotalPublicNetworks()).isEqualTo(1);
        assertThat(profile.getPublicNetworks()).hasSize(1);
        assertThat(profile.getPublicNetworks().get(0).getTitle()).isEqualTo("Public Network");
        assertThat(profile.getTopConcepts()).containsExactly("Recursion", "Graph Theory");
    }

    @Test
    @DisplayName("getPublicCreatorProfile throws ResourceNotFoundException for unknown user")
    void testGetPublicCreatorProfile_UnknownUser() {
        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicDiscoveryService.getPublicCreatorProfile("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getRelatedPublicNetworks returns related public networks and excludes non-public targets")
    void testGetRelatedPublicNetworks_Success() {
        Workspace target = publicWorkspace;
        Workspace related = new Workspace("Related Public Net", "Desc", creator, Visibility.PUBLIC);
        related.setId(UUID.randomUUID());
        related.setPublished(true);

        when(workspaceRepository.findByIdAndIsDeletedFalse(target.getId())).thenReturn(Optional.of(target));
        when(workspaceRepository.findRelatedPublicWorkspacesBySharedConcepts(eq(target), eq(target.getId()), any(Pageable.class)))
                .thenReturn(List.of(related));
        when(nodeRepository.countByWorkspaceAndIsDeletedFalse(related)).thenReturn(5L);
        when(edgeRepository.countByWorkspaceAndIsDeletedFalse(related)).thenReturn(3L);

        List<com.knowledgenetwork.domain.payload.response.PublicRelatedNetworkResponse> result =
                publicDiscoveryService.getRelatedPublicNetworks(target.getId(), 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Related Public Net");
        assertThat(result.get(0).getRelationReason()).isEqualTo("Shared Concept Labels");
    }

    @Test
    @DisplayName("getRelatedPublicNetworks throws AccessDeniedException when target network is private")
    void testGetRelatedPublicNetworks_PrivateTarget_ThrowsAccessDenied() {
        when(workspaceRepository.findByIdAndIsDeletedFalse(privateWorkspace.getId())).thenReturn(Optional.of(privateWorkspace));

        assertThatThrownBy(() -> publicDiscoveryService.getRelatedPublicNetworks(privateWorkspace.getId(), 5))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

}

