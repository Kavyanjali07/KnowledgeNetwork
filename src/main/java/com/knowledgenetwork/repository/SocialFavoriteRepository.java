package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.SocialFavorite;
import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialFavoriteRepository extends JpaRepository<SocialFavorite, UUID> {

    boolean existsByPostAndUser(SocialPost post, User user);

    List<SocialFavorite> findByPost(SocialPost post);

    Optional<SocialFavorite> findByPostAndUser(SocialPost post, User user);
}
