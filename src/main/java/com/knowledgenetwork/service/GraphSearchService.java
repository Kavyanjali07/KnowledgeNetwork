package com.knowledgenetwork.service;

import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.NodeRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class GraphSearchService {

    private final NodeRepository nodeRepository;

    public GraphSearchService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

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
}
