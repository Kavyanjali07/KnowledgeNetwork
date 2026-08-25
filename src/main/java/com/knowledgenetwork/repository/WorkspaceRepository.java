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
}
