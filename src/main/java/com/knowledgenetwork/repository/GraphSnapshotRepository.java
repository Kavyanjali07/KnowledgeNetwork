package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.GraphSnapshot;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GraphSnapshotRepository extends JpaRepository<GraphSnapshot, UUID> {
    List<GraphSnapshot> findByWorkspaceOrderByCreatedAtDesc(Workspace workspace);
}
