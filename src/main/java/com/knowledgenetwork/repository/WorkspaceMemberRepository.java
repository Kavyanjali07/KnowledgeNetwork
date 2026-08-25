package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    List<WorkspaceMember> findByWorkspace(Workspace workspace);

    Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);

    List<WorkspaceMember> findByUser(User user);

    boolean existsByWorkspaceAndUser(Workspace workspace, User user);
}
