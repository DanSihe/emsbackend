package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.dto.HostLoginRequest;
import com.sihe.emsbackend.dto.MfaChallengeResponse;
import com.sihe.emsbackend.dto.MfaVerifyRequest;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.service.HostService;
import com.sihe.emsbackend.service.MfaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/hosts")
@CrossOrigin(origins = "http://localhost:3000")
public class HostController {

    private final HostService hostService;
    private final MfaService mfaService;

    public HostController(HostService hostService, MfaService mfaService) {
        this.hostService = hostService;
        this.mfaService = mfaService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHost(@RequestBody Host host) {
        try {
            Host registeredHost = hostService.registerHost(host);
            registeredHost.setPassword(null); // don't return password in response
            return ResponseEntity.ok(registeredHost);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginHost(@RequestBody HostLoginRequest loginRequest) {
        try {
            Host host = hostService.loginOrThrow(loginRequest.getEmail(), loginRequest.getPassword());
            MfaChallengeResponse challenge = mfaService.createChallengeForHost(host);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/login/verify-mfa")
    public ResponseEntity<?> verifyHostMfa(@RequestBody MfaVerifyRequest verifyRequest) {
        try {
            String email = mfaService.getEmailForChallenge(verifyRequest.getChallengeId(), "HOST");
            Optional<Host> hostOpt = hostService.getHostByEmail(email);
            if (hostOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Host not found");
            }

            Host host = hostOpt.get();
            mfaService.verifyChallengeOrThrow(verifyRequest.getChallengeId(), verifyRequest.getCode(), "HOST", host.getId());
            host.setPassword(null);
            return ResponseEntity.ok(host);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHost(@PathVariable Long id, @RequestBody Host updatedHost) {
        try {
            Host host = hostService.updateHost(id, updatedHost);
            host.setPassword(null); // Do not expose password in response
            return ResponseEntity.ok(host);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
