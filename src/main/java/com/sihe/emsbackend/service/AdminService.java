package com.sihe.emsbackend.service;

import com.sihe.emsbackend.dto.LoginRequest;
import com.sihe.emsbackend.exception.InvalidCredentialsException;
import com.sihe.emsbackend.exception.UserNotFoundException;
import com.sihe.emsbackend.model.Admin;
import com.sihe.emsbackend.repository.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin register(Admin admin) {
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        admin.setPassword(encoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public Admin login(LoginRequest loginRequest) {
        Admin admin = adminRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));

        if (!encoder.matches(loginRequest.getPassword(), admin.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return admin;
    }

    public Admin getByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
    }
}
