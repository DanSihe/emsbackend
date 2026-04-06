package com.sihe.emsbackend.service;

import com.sihe.emsbackend.dto.MfaChallengeResponse;
import com.sihe.emsbackend.exception.InvalidCredentialsException;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.model.MfaChallenge;
import com.sihe.emsbackend.model.User;
import com.sihe.emsbackend.repository.MfaChallengeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MfaService {

    private final MfaChallengeRepository mfaChallengeRepository;
    private final MfaDeliveryService mfaDeliveryService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${app.mfa.code-length:6}")
    private int codeLength;

    @Value("${app.mfa.expiry-minutes:10}")
    private int expiryMinutes;

    public MfaService(MfaChallengeRepository mfaChallengeRepository, MfaDeliveryService mfaDeliveryService) {
        this.mfaChallengeRepository = mfaChallengeRepository;
        this.mfaDeliveryService = mfaDeliveryService;
    }

    public MfaChallengeResponse createChallengeForUser(User user) {
        return createChallenge("USER", user.getId(), user.getEmail());
    }

    public MfaChallengeResponse createChallengeForHost(Host host) {
        return createChallenge("HOST", host.getId(), host.getEmail());
    }

    public void verifyChallengeOrThrow(String challengeId, String code, String principalType, Long principalId) {
        MfaChallenge challenge = getChallengeOrThrow(challengeId);

        if (challenge.isConsumed()) {
            throw new InvalidCredentialsException("This verification code has already been used");
        }

        if (!principalType.equals(challenge.getPrincipalType()) || !principalId.equals(challenge.getPrincipalId())) {
            throw new InvalidCredentialsException("Verification session does not match this account");
        }

        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Verification code expired. Please sign in again");
        }

        if (!encoder.matches(code, challenge.getCodeHash())) {
            throw new InvalidCredentialsException("Invalid verification code");
        }

        challenge.setConsumed(true);
        mfaChallengeRepository.save(challenge);
    }

    public String getEmailForChallenge(String challengeId, String principalType) {
        MfaChallenge challenge = getChallengeOrThrow(challengeId);
        if (!principalType.equals(challenge.getPrincipalType())) {
            throw new InvalidCredentialsException("Verification session does not match this login type");
        }
        return challenge.getEmail();
    }

    private MfaChallengeResponse createChallenge(String principalType, Long principalId, String email) {
        expireExistingChallenges(principalType, principalId);

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        MfaChallenge challenge = new MfaChallenge();
        challenge.setId(UUID.randomUUID().toString());
        challenge.setPrincipalType(principalType);
        challenge.setPrincipalId(principalId);
        challenge.setEmail(email);
        challenge.setCodeHash(encoder.encode(code));
        challenge.setExpiresAt(expiresAt);
        challenge.setConsumed(false);
        challenge.setCreatedAt(LocalDateTime.now());

        mfaChallengeRepository.save(challenge);
        String deliveryMode = mfaDeliveryService.sendLoginCode(email, code, principalType);

        return new MfaChallengeResponse(
                challenge.getId(),
                expiresAt,
                deliveryMode,
                maskEmail(email),
                deliveryMode.equals("EMAIL")
                        ? "A verification code was sent to your email."
                        : "Mail delivery is in demo mode. Check the backend console for the verification code."
        );
    }

    private void expireExistingChallenges(String principalType, Long principalId) {
        List<MfaChallenge> activeChallenges = mfaChallengeRepository
                .findByPrincipalTypeAndPrincipalIdAndConsumedFalse(principalType, principalId);

        for (MfaChallenge challenge : activeChallenges) {
            challenge.setConsumed(true);
        }

        if (!activeChallenges.isEmpty()) {
            mfaChallengeRepository.saveAll(activeChallenges);
        }
    }

    private MfaChallenge getChallengeOrThrow(String challengeId) {
        return mfaChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new InvalidCredentialsException("Verification session not found"));
    }

    private String generateCode() {
        int max = (int) Math.pow(10, codeLength);
        int min = max / 10;
        int code = min + (int) (Math.random() * (max - min));
        return String.valueOf(code);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "*" + domain;
        }

        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domain;
    }
}
