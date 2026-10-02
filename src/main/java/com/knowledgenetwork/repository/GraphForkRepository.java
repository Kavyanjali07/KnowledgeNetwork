package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.GraphFork;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GraphForkRepository extends JpaRepository<GraphFork, UUID> {
    List<GraphFork> findByWorkspace(Workspace workspace);
    long countBySourceWorkspace(Workspace sourceWorkspace);
}
