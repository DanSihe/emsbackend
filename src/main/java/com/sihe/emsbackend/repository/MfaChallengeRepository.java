package com.sihe.emsbackend.repository;

import com.sihe.emsbackend.model.MfaChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MfaChallengeRepository extends JpaRepository<MfaChallenge, String> {
    List<MfaChallenge> findByPrincipalTypeAndPrincipalIdAndConsumedFalse(String principalType, Long principalId);
}
