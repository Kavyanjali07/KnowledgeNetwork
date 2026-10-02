package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("""
        SELECT u FROM User u
        WHERE LOWER(u.email) = LOWER(:identifier)
        OR LOWER(SUBSTRING(u.email, 1, CASE WHEN LOCATE('@', u.email) > 0 THEN LOCATE('@', u.email) - 1 ELSE LENGTH(u.email) END)) = LOWER(:identifier)
        OR LOWER(CONCAT(u.firstName, '.', u.lastName)) = LOWER(:identifier)
        OR LOWER(u.firstName) = LOWER(:identifier)
    """)
    Optional<User> findByUsernameOrEmail(@org.springframework.data.repository.query.Param("identifier") String identifier);
}

