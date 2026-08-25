package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.SocialLike;
import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialLikeRepository extends JpaRepository<SocialLike, UUID> {

    boolean existsByPostAndUser(SocialPost post, User user);

    List<SocialLike> findByPost(SocialPost post);

    Optional<SocialLike> findByPostAndUser(SocialPost post, User user);
}
