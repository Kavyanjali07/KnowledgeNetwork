package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<Node, UUID>, JpaSpecificationExecutor<Node> {

    Page<Node> findByWorkspaceAndIsDeletedFalse(Workspace workspace, Pageable pageable);

    Optional<Node> findByIdAndWorkspaceAndIsDeletedFalse(UUID id, Workspace workspace);

    boolean existsByIdAndWorkspaceAndIsDeletedFalse(UUID id, Workspace workspace);

    long countByWorkspaceAndIsDeletedFalse(Workspace workspace);

    @Query("""
        SELECT n FROM Node n
        JOIN n.workspace w
        LEFT JOIN w.members m
        WHERE n.isDeleted = false
        AND w.isDeleted = false
        AND (
            (:user IS NOT NULL AND w.owner = :user)
            OR (:user IS NOT NULL AND m.user = :user)
            OR w.visibility = com.knowledgenetwork.domain.model.Visibility.PUBLIC
        )
        AND (
            LOWER(n.label) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<Node> searchNodesInAccessibleWorkspaces(
            @Param("user") User user,
            @Param("query") String query,
            Pageable pageable);

    @Query("""
        SELECT n FROM Node n
        JOIN n.workspace w
        LEFT JOIN w.members m
        WHERE n.isDeleted = false
        AND w.isDeleted = false
        AND (
            (:user IS NOT NULL AND w.owner = :user)
            OR (:user IS NOT NULL AND m.user = :user)
            OR w.visibility = com.knowledgenetwork.domain.model.Visibility.PUBLIC
        )
        AND (
            LOWER(n.label) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND (:nodeTypeName IS NULL OR n.nodeType.name = :nodeTypeName)
    """)
    Page<Node> searchNodesInAccessibleWorkspacesByNodeType(
            @Param("user") User user,
            @Param("query") String query,
            @Param("nodeTypeName") String nodeTypeName,
            Pageable pageable);
}
