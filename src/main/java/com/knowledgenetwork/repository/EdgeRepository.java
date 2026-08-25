package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, UUID> {

    Page<Edge> findByWorkspaceAndIsDeletedFalse(Workspace workspace, Pageable pageable);

    Optional<Edge> findByIdAndWorkspaceAndIsDeletedFalse(UUID id, Workspace workspace);

    List<Edge> findByWorkspaceAndEdgeTypeAndIsDeletedFalse(Workspace workspace, EdgeType edgeType);

    List<Edge> findBySourceNodeAndWorkspaceAndIsDeletedFalse(Node sourceNode, Workspace workspace);

    List<Edge> findByTargetNodeAndWorkspaceAndIsDeletedFalse(Node targetNode, Workspace workspace);

    boolean existsByIdAndWorkspaceAndIsDeletedFalse(UUID id, Workspace workspace);

    long countByWorkspaceAndIsDeletedFalse(Workspace workspace);
}
