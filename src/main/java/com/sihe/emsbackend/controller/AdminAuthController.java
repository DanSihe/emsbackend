package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.dto.LoginRequest;
import com.sihe.emsbackend.dto.MfaChallengeResponse;
import com.sihe.emsbackend.dto.MfaVerifyRequest;
import com.sihe.emsbackend.model.Admin;
import com.sihe.emsbackend.service.AdminService;
import com.sihe.emsbackend.service.MfaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminAuthController {

    private final AdminService adminService;
    private final MfaService mfaService;

    public AdminAuthController(AdminService adminService, MfaService mfaService) {
        this.adminService = adminService;
        this.mfaService = mfaService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Admin admin) {
        try {
            return ResponseEntity.ok(adminService.register(admin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Admin admin = adminService.login(loginRequest);
            MfaChallengeResponse challenge = mfaService.createChallengeForAdmin(admin);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerifyRequest verifyRequest) {
        try {
            String email = mfaService.getEmailForChallenge(verifyRequest.getChallengeId(), "ADMIN");
            Admin admin = adminService.getByEmail(email);
            mfaService.verifyChallengeOrThrow(verifyRequest.getChallengeId(), verifyRequest.getCode(), "ADMIN", admin.getId());
            return ResponseEntity.ok(admin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
