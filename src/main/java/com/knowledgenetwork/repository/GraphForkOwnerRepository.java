package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.GraphFork;
import com.knowledgenetwork.domain.model.GraphForkOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GraphForkOwnerRepository extends JpaRepository<GraphForkOwner, UUID> {
    List<GraphForkOwner> findByFork(GraphFork fork);
}
