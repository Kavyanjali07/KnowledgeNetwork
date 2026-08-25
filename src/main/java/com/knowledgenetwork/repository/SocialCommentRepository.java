package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.SocialComment;
import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialCommentRepository extends JpaRepository<SocialComment, UUID> {

    List<SocialComment> findByWorkspaceAndIsDeletedFalse(Workspace workspace);

    Page<SocialComment> findByPostAndIsDeletedFalse(SocialPost post, Pageable pageable);

    Optional<SocialComment> findByIdAndWorkspaceAndIsDeletedFalse(UUID id, Workspace workspace);
}
