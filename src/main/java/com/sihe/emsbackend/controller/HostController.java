package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.dto.HostLoginRequest;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.service.HostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/hosts")
@CrossOrigin(origins = "http://localhost:3000")
public class HostController {

    private final HostService hostService;

    public HostController(HostService hostService) {
        this.hostService = hostService;
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
        Optional<Host> hostOpt = hostService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (hostOpt.isPresent()) {
            Host host = hostOpt.get();
            host.setPassword(null); // Hide password
            return ResponseEntity.ok(host);
        } else {
            return ResponseEntity.status(401).body("Invalid email or password.");
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
