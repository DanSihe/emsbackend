// com.sihe.emsbackend.controller.AuthController.java
package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.dto.LoginRequest;
import com.sihe.emsbackend.dto.MfaChallengeResponse;
import com.sihe.emsbackend.dto.MfaVerifyRequest;
import com.sihe.emsbackend.model.User;
import com.sihe.emsbackend.service.MfaService;
import com.sihe.emsbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private final UserService userService;
    private final MfaService mfaService;

    public AuthController(UserService userService, MfaService mfaService) {
        this.userService = userService;
        this.mfaService = mfaService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.login(loginRequest);
            MfaChallengeResponse challenge = mfaService.createChallengeForUser(user);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerifyRequest verifyRequest) {
        try {
            User user = userService.getUserByEmail(resolveEmailFromChallenge(verifyRequest));
            mfaService.verifyChallengeOrThrow(verifyRequest.getChallengeId(), verifyRequest.getCode(), "USER", user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String resolveEmailFromChallenge(MfaVerifyRequest verifyRequest) {
        if (verifyRequest.getChallengeId() == null || verifyRequest.getChallengeId().isBlank()) {
            throw new IllegalArgumentException("Challenge ID is required");
        }
        return mfaService.getEmailForChallenge(verifyRequest.getChallengeId(), "USER");
    }
}
