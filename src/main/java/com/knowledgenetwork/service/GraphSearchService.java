package com.knowledgenetwork.service;

import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.response.SearchResultResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

@Service
public class GraphSearchService {

    private final NodeRepository nodeRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final EdgeRepository edgeRepository;

    public GraphSearchService(NodeRepository nodeRepository,
                               WorkspaceRepository workspaceRepository,
                               UserRepository userRepository,
                               EdgeRepository edgeRepository) {
        this.nodeRepository = nodeRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.edgeRepository = edgeRepository;
    }

    /**
     * Unified search across graphs (workspaces) and nodes.
     * Authorization: only returns results the current user can access.
     * Ranking: exact title match > starts with > contains.
     */
    public PageResponse<SearchResultResponse> unifiedSearch(String query, String type,
                                                              String nodeTypeName, int page, int size) {
        String trimmed = (query == null) ? "" : query.trim();
        if (trimmed.isEmpty()) {
            return PageResponse.<SearchResultResponse>builder()
                    .content(List.of())
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements(0)
                    .totalPages(0)
                    .isLast(true)
                    .build();
        }

        User currentUser = resolveCurrentUser();
        Pageable pageable = PageRequest.of(0, Math.min(size * 3, 100), Sort.by(Sort.Direction.ASC, "updatedAt"));

        List<SearchResultResponse> allResults = new ArrayList<>();

        boolean includeGraphs = "all".equalsIgnoreCase(type) || "graphs".equalsIgnoreCase(type);
        boolean includeNodes = "all".equalsIgnoreCase(type) || "nodes".equalsIgnoreCase(type);

        // Search graphs (workspaces)
        if (includeGraphs) {
            Page<Workspace> graphResults = workspaceRepository.searchAccessibleWorkspaces(
                    currentUser, trimmed, pageable);
            for (Workspace w : graphResults.getContent()) {
                long nCount = nodeRepository.countByWorkspaceAndIsDeletedFalse(w);
                long eCount = edgeRepository.countByWorkspaceAndIsDeletedFalse(w);
                String desc = w.getDescription();
                allResults.add(SearchResultResponse.fromGraph(
                        w.getId(), w.getName(), desc,
                        w.getVisibility() != null ? w.getVisibility().name() : "PRIVATE",
                        nCount, eCount, w.getCreatedAt(), w.getUpdatedAt()));
            }
        }

        // Search nodes
        if (includeNodes) {
            Page<Node> nodeResults;
            if (nodeTypeName != null && !nodeTypeName.isBlank()) {
                nodeResults = nodeRepository.searchNodesInAccessibleWorkspacesByNodeType(
                        currentUser, trimmed, nodeTypeName, pageable);
            } else {
                nodeResults = nodeRepository.searchNodesInAccessibleWorkspaces(
                        currentUser, trimmed, pageable);
            }
            for (Node n : nodeResults.getContent()) {
                String desc = extractDescription(n);
                Workspace w = n.getWorkspace();
                allResults.add(SearchResultResponse.fromNode(
                        n.getId(), n.getLabel(), desc,
                        w.getId(), w.getName(),
                        n.getNodeType() != null ? n.getNodeType().getName() : null,
                        n.getNodeType() != null ? n.getNodeType().getColorCode() : null,
                        n.getNodeType() != null ? n.getNodeType().getIcon() : null,
                        n.getPositionX(), n.getPositionY(),
                        n.getCreatedAt(), n.getUpdatedAt()));
            }
        }

        // Rank results: exact match > starts with > contains
        String lowerQuery = trimmed.toLowerCase();
        allResults.sort(Comparator.<SearchResultResponse, Integer>comparing(r -> {
            String title = r.getTitle() != null ? r.getTitle().toLowerCase() : "";
            if (title.equals(lowerQuery)) return 0;       // exact match
            if (title.startsWith(lowerQuery)) return 1;    // starts with
            return 2;                                       // contains
        }).thenComparing(r -> r.getResultType() == SearchResultResponse.ResultType.GRAPH ? 0 : 1)
          .thenComparing(SearchResultResponse::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        // Apply pagination to combined results
        int totalElements = allResults.size();
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<SearchResultResponse> pageContent = allResults.subList(fromIndex, toIndex);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<SearchResultResponse>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(page >= totalPages - 1)
                .build();
    }

    // --- Existing workspace-scoped search (preserved for backward compatibility) ---

    public Pageable createPageable(int page, int size, String sortBy) {
        Sort sort = switch (sortBy == null ? "relevance" : sortBy) {
            case "recent" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            case "confidence", "connections" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
        return PageRequest.of(page, size, sort);
    }

    public Page<Node> searchNodes(Workspace workspace, String query, List<String> tags,
                                  Visibility visibility, Pageable pageable) {
        return searchNodes(workspace, query, tags, visibility, null, null, pageable);
    }

    public Page<Node> searchNodes(Workspace workspace, String query, List<String> tags, Visibility visibility,
                                  Instant updatedAfter, Double minConfidence, Pageable pageable) {
        Specification<Node> specification = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("workspace"), workspace));
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (query != null && !query.isBlank()) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("label")), likePattern),
                        cb.like(cb.lower(root.get("attributes")), likePattern)
                ));
            }

            if (visibility != null) {
                predicates.add(cb.equal(root.get("visibility"), visibility));
            }

            if (updatedAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAfter));
            }

            if (minConfidence != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        cb.function("jsonb_extract_path_text", Double.class, root.get("attributes"), cb.literal("confidence")),
                        minConfidence));
            }

            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    predicates.add(cb.isMember(tag, root.get("tags")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return nodeRepository.findAll(specification, pageable);
    }

    // --- Private helpers ---

    private User resolveCurrentUser() {
        Optional<UUID> userIdOpt = SecurityUtils.getCurrentUserPrincipal()
                .map(p -> p.getId());
        if (userIdOpt.isPresent()) {
            return userRepository.findById(userIdOpt.get()).orElse(null);
        }
        return null;
    }

    private String extractDescription(Node node) {
        if (node.getAttributes() == null) return null;
        Object desc = node.getAttributes().get("description");
        if (desc instanceof String s) return s;
        Object content = node.getAttributes().get("content");
        if (content instanceof String s) return s;
        return null;
    }
}
