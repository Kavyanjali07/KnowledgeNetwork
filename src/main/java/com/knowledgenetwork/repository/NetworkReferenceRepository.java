package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.NetworkReference;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NetworkReferenceRepository extends JpaRepository<NetworkReference, UUID> {

    Page<NetworkReference> findBySourceWorkspace(Workspace sourceWorkspace, Pageable pageable);

    Page<NetworkReference> findByTargetWorkspace(Workspace targetWorkspace, Pageable pageable);

    long countBySourceWorkspace(Workspace sourceWorkspace);

    long countByTargetWorkspace(Workspace targetWorkspace);

    Optional<NetworkReference> findBySourceWorkspaceAndSourceNodeIdAndTargetWorkspaceAndTargetNodeId(
            Workspace sourceWorkspace, UUID sourceNodeId, Workspace targetWorkspace, UUID targetNodeId);

    boolean existsBySourceWorkspaceAndSourceNodeIdAndTargetWorkspaceAndTargetNodeId(
            Workspace sourceWorkspace, UUID sourceNodeId, Workspace targetWorkspace, UUID targetNodeId);
}
