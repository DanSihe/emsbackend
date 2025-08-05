// com.sihe.emsbackend.controller.AuthController.java
package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.dto.LoginRequest;
import com.sihe.emsbackend.model.User;
import com.sihe.emsbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.login(loginRequest);
            return ResponseEntity.ok(user); // Optionally exclude password in response
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
