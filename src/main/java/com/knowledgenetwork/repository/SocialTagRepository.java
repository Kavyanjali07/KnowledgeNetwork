package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.SocialTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialTagRepository extends JpaRepository<SocialTag, UUID> {

    boolean existsByPostAndTag(SocialPost post, String tag);

    List<SocialTag> findByPost(SocialPost post);

    Optional<SocialTag> findByPostAndTag(SocialPost post, String tag);
}
