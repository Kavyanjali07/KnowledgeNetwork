package com.knowledgenetwork.repository;

import com.knowledgenetwork.domain.model.VerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationOtpRepository extends JpaRepository<VerificationOtp, UUID> {

    Optional<VerificationOtp> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("UPDATE VerificationOtp vo SET vo.used = true WHERE vo.userId = :userId AND vo.used = false")
    void invalidateAllByUserId(@Param("userId") UUID userId);
}
