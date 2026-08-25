package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.SocialFollow;
import com.knowledgenetwork.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialFollowRepository extends JpaRepository<SocialFollow, UUID> {

    boolean existsByFollowerAndFollowee(User follower, User followee);

    List<SocialFollow> findByFollower(User follower);

    List<SocialFollow> findByFollowee(User followee);

    Optional<SocialFollow> findByFollowerAndFollowee(User follower, User followee);

    long countByFollower(User follower);

    long countByFollowee(User followee);
}
