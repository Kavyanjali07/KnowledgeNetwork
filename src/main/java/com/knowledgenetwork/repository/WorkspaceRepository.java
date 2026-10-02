package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByOwner(User owner);

    List<Workspace> findByOwnerAndIsDeletedFalse(User owner);

    Optional<Workspace> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
        SELECT DISTINCT w FROM Workspace w
        LEFT JOIN w.members m
        WHERE w.isDeleted = false
        AND (
            (:user IS NOT NULL AND w.owner = :user)
            OR (:user IS NOT NULL AND m.user = :user)
            OR w.visibility = com.knowledgenetwork.domain.model.Visibility.PUBLIC
        )
        AND (:visibility IS NULL OR w.visibility = :visibility)
    """)
    Page<Workspace> findAccessibleWorkspaces(
            @Param("user") User user,
            @Param("visibility") Visibility visibility,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT w FROM Workspace w
        LEFT JOIN w.members m
        WHERE w.isDeleted = false
        AND (
            (:user IS NOT NULL AND w.owner = :user)
            OR (:user IS NOT NULL AND m.user = :user)
            OR w.visibility = com.knowledgenetwork.domain.model.Visibility.PUBLIC
        )
        AND (
            LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(w.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<Workspace> searchAccessibleWorkspaces(
            @Param("user") User user,
            @Param("query") String query,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT w FROM Workspace w
        WHERE w.isDeleted = false
        AND w.isPublished = true
        AND w.visibility = com.knowledgenetwork.domain.model.Visibility.PUBLIC
        AND (:concept IS NULL OR :concept = '' OR EXISTS (
            SELECT n FROM Node n WHERE n.workspace = w AND n.isDeleted = false AND LOWER(n.label) LIKE LOWER(CONCAT('%', :concept, '%'))
        ))
        AND (:creatorUsername IS NULL OR :creatorUsername = '' OR LOWER(w.owner.email) = LOWER(:creatorUsername) OR LOWER(SUBSTRING(w.owner.email, 1, CASE WHEN LOCATE('@', w.owner.email) > 0 THEN LOCATE('@', w.owner.email) - 1 ELSE LENGTH(w.owner.email) END)) = LOWER(:creatorUsername) OR LOWER(w.owner.firstName) = LOWER(:creatorUsername))
        AND (:licenseType IS NULL OR w.licenseType = :licenseType)
    """)
    Page<Workspace> findPublicWorkspaces(
            @Param("concept") String concept,
            @Param("creatorUsername") String creatorUsername,
            @Param("licenseType") com.knowledgenetwork.domain.enums.LicenseType licenseType,
            Pageable pageable);

    List<Workspace> findByOwnerAndIsPublishedTrueAndVisibilityAndIsDeletedFalse(
            User owner,
            Visibility visibility);

    @Query("""
        SELECT DISTINCT w2 FROM Node n1
        JOIN Node n2 ON LOWER(n1.label) = LOWER(n2.label)
        JOIN n2.workspace w2
        WHERE n1.workspace = :targetWorkspace
        AND n1.isDeleted = false
        AND n2.isDeleted = false
        AND w2.id != :targetWorkspaceId
        AND w2.isDeleted = false
        AND w2.isPublished = true
        AND w2.visibility = com.knowledgenetwork.domain.model.Visibility.PUBLIC
    """)
    List<Workspace> findRelatedPublicWorkspacesBySharedConcepts(
            @Param("targetWorkspace") Workspace targetWorkspace,
            @Param("targetWorkspaceId") UUID targetWorkspaceId,
            Pageable pageable);
}


