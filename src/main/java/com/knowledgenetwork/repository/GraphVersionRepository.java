package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.GraphVersion;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GraphVersionRepository extends JpaRepository<GraphVersion, UUID> {
    List<GraphVersion> findByWorkspaceOrderByVersionNumberDesc(Workspace workspace);

    Page<GraphVersion> findByWorkspaceOrderByVersionNumberDesc(Workspace workspace, Pageable pageable);

    Optional<GraphVersion> findTopByWorkspaceOrderByVersionNumberDesc(Workspace workspace);
}
