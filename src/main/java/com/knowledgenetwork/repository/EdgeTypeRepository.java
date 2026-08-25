package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.EdgeType;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdgeTypeRepository extends JpaRepository<EdgeType, UUID> {

    Page<EdgeType> findByWorkspaceAndIsDeletedFalse(Workspace workspace, Pageable pageable);

    Optional<EdgeType> findByIdAndWorkspaceAndIsDeletedFalse(UUID id, Workspace workspace);

    boolean existsByWorkspaceAndNameAndIsDeletedFalse(Workspace workspace, String name);
}
