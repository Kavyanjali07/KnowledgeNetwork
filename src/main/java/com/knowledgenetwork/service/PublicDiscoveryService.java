package com.knowledgenetwork.service;

import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.mapper.GraphMapper;
import com.knowledgenetwork.domain.enums.LicenseType;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.domain.payload.response.PublicConceptExploreResponse;
import com.knowledgenetwork.domain.payload.response.PublicCreatorProfileResponse;
import com.knowledgenetwork.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublicDiscoveryService {

    private final NodeRepository nodeRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final EdgeRepository edgeRepository;
    private final GraphForkRepository graphForkRepository;
    private final NetworkReferenceRepository networkReferenceRepository;
    private final GraphMapper graphMapper;

    public PublicDiscoveryService(NodeRepository nodeRepository,
                                  WorkspaceRepository workspaceRepository,
                                  UserRepository userRepository,
                                  EdgeRepository edgeRepository,
                                  GraphForkRepository graphForkRepository,
                                  NetworkReferenceRepository networkReferenceRepository,
                                  GraphMapper graphMapper) {
        this.nodeRepository = nodeRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.edgeRepository = edgeRepository;
        this.graphForkRepository = graphForkRepository;
        this.networkReferenceRepository = networkReferenceRepository;
        this.graphMapper = graphMapper;
    }

    /**
     * Aggregates public concept occurrences by normalized label across published, public networks.
     */
    public PublicConceptExploreResponse exploreConcepts(String query, int page, int size) {
        String trimmedQuery = (query == null) ? "" : query.trim();
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Node> nodePage = nodeRepository.findPublicNodesByLabel(trimmedQuery, pageable);

        List<PublicConceptExploreResponse.ConceptOccurrence> occurrences = nodePage.getContent().stream()
                .map(node -> {
                    Workspace ws = node.getWorkspace();
                    User owner = ws.getOwner();
                    String creatorName = (owner != null) ? (owner.getFirstName() + " " + owner.getLastName()).trim() : "Anonymous";
                    String creatorUsername = (owner != null && owner.getEmail() != null && owner.getEmail().contains("@"))
                            ? owner.getEmail().substring(0, owner.getEmail().indexOf('@'))
                            : "creator";

                    return new PublicConceptExploreResponse.ConceptOccurrence(
                            ws.getId(),
                            ws.getName(),
                            ws.getLicenseType(),
                            creatorName,
                            creatorUsername,
                            node.getId(),
                            node.getNodeType() != null ? node.getNodeType().getName() : "Concept",
                            node.getPositionX(),
                            node.getPositionY()
                    );
                })
                .collect(Collectors.toList());

        long totalPublicNetworks = occurrences.stream()
                .map(PublicConceptExploreResponse.ConceptOccurrence::getNetworkId)
                .distinct()
                .count();

        List<PublicConceptExploreResponse.RelatedConcept> relatedConcepts = new ArrayList<>();
        if (!trimmedQuery.isEmpty()) {
            List<Object[]> relatedObjects = nodeRepository.findPublicRelatedConceptLabels(trimmedQuery, PageRequest.of(0, 10));
            for (Object[] obj : relatedObjects) {
                String label = (String) obj[0];
                Long count = (Long) obj[1];
                relatedConcepts.add(new PublicConceptExploreResponse.RelatedConcept(label, count != null ? count : 0L));
            }
        }

        return new PublicConceptExploreResponse(trimmedQuery, totalPublicNetworks, occurrences, relatedConcepts);
    }

    /**
     * Searches and lists public networks matching discovery criteria.
     */
    public PageResponse<GraphResponse> discoverPublicNetworks(String concept, String creatorUsername,
                                                             String licenseTypeStr, Boolean allowDerivatives,
                                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"));
        LicenseType licenseType = null;
        if (licenseTypeStr != null && !licenseTypeStr.trim().isEmpty()) {
            try {
                licenseType = LicenseType.valueOf(licenseTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Workspace> workspacePage = workspaceRepository.findPublicWorkspaces(
                concept, creatorUsername, licenseType, pageable
        );

        List<GraphResponse> content = workspacePage.getContent().stream()
                .filter(ws -> {
                    if (allowDerivatives != null && allowDerivatives) {
                        LicenseType lt = ws.getLicenseType();
                        return lt != LicenseType.ALL_RIGHTS_RESERVED
                                && lt != LicenseType.CC_BY_ND_4_0
                                && lt != LicenseType.CC_BY_NC_ND_4_0;
                    }
                    return true;
                })
                .map(this::mapToPublicGraphResponse)
                .collect(Collectors.toList());

        return PageResponse.<GraphResponse>builder()
                .content(content)
                .pageNumber(workspacePage.getNumber())
                .pageSize(workspacePage.getSize())
                .totalElements(workspacePage.getTotalElements())
                .totalPages(workspacePage.getTotalPages())
                .isLast(workspacePage.isLast())
                .build();
    }

    /**
     * Retrieves creator public profile displaying only public, published networks.
     */
    public PublicCreatorProfileResponse getPublicCreatorProfile(String username) {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Creator", "username", username));

        List<Workspace> publicWorkspaces = workspaceRepository.findByOwnerAndIsPublishedTrueAndVisibilityAndIsDeletedFalse(
                user, Visibility.PUBLIC
        );

        List<GraphResponse> publicNetworks = publicWorkspaces.stream()
                .map(this::mapToPublicGraphResponse)
                .collect(Collectors.toList());

        List<Object[]> topConceptObjects = nodeRepository.findTopConceptsByCreator(user, PageRequest.of(0, 10));
        List<String> topConcepts = topConceptObjects.stream()
                .map(obj -> (String) obj[0])
                .collect(Collectors.toList());

        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        String userHandle = user.getEmail().contains("@") ? user.getEmail().substring(0, user.getEmail().indexOf('@')) : user.getFirstName().toLowerCase();

        return new PublicCreatorProfileResponse(
                user.getId(),
                userHandle,
                fullName,
                user.getBio(),
                null,
                publicNetworks.size(),
                publicNetworks,
                topConcepts
        );
    }

    /**
     * Discovers related public networks based deterministically on shared concept labels and lineage.
     */
    public List<com.knowledgenetwork.domain.payload.response.PublicRelatedNetworkResponse> getRelatedPublicNetworks(UUID workspaceId, int limit) {
        Workspace target = workspaceRepository.findByIdAndIsDeletedFalse(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Network", "id", workspaceId.toString()));

        // Enforce public access boundary: target workspace itself must be published & public
        if (!target.isPublished() || target.getVisibility() != Visibility.PUBLIC) {
            throw new org.springframework.security.access.AccessDeniedException("Access restricted to published public networks");
        }


        Pageable pageable = PageRequest.of(0, Math.min(limit, 10));
        List<Workspace> relatedWorkspaces = workspaceRepository.findRelatedPublicWorkspacesBySharedConcepts(target, workspaceId, pageable);

        return relatedWorkspaces.stream()
                .map(ws -> {
                    User owner = ws.getOwner();
                    String creatorName = owner != null ? (owner.getFirstName() + " " + owner.getLastName()).trim() : "Anonymous";
                    String creatorUsername = owner != null && owner.getEmail() != null && owner.getEmail().contains("@")
                            ? owner.getEmail().substring(0, owner.getEmail().indexOf('@'))
                            : "creator";

                    long nodeCount = nodeRepository.countByWorkspaceAndIsDeletedFalse(ws);
                    long edgeCount = edgeRepository.countByWorkspaceAndIsDeletedFalse(ws);

                    return new com.knowledgenetwork.domain.payload.response.PublicRelatedNetworkResponse(
                            ws.getId(),
                            ws.getName(),
                            ws.getDescription(),
                            creatorName,
                            creatorUsername,
                            ws.getLicenseType(),
                            nodeCount,
                            edgeCount,
                            "Shared Concept Labels",
                            ws.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    private GraphResponse mapToPublicGraphResponse(Workspace workspace) {
        GraphResponse response = graphMapper.toGraphResponse(workspace);
        response.setNodeCount(nodeRepository.countByWorkspaceAndIsDeletedFalse(workspace));
        response.setEdgeCount(edgeRepository.countByWorkspaceAndIsDeletedFalse(workspace));
        response.setDerivativeCount(graphForkRepository.countBySourceWorkspace(workspace));
        response.setReferenceCount(networkReferenceRepository.countByTargetWorkspace(workspace));
        return response;
    }
}

