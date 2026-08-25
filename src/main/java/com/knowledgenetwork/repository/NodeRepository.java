package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
