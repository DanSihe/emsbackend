package com.sihe.emsbackend.service;

import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.repository.HostRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class HostService {

    private final HostRepository hostRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public HostService(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    // Register a new host
    public Host registerHost(Host host) throws Exception {
        if (emailExists(host.getEmail())) {
            throw new Exception("Email already in use");
        }

        // Ensure age is 18+
        if (host.getDateOfBirth() == null ||
                Period.between(host.getDateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw new Exception("Host must be at least 18 years old");
        }

        // ✅ Correct usage — hash the password from the host object
        host.setPassword(passwordEncoder.encode(host.getPassword()));

        return hostRepository.save(host);
    }

    // Check if email is already registered
    public boolean emailExists(String email) {
        return hostRepository.existsByEmail(email);
    }

    public Optional<Host> login(String email, String rawPassword) {
        Optional<Host> hostOpt = hostRepository.findByEmail(email);
        if (hostOpt.isPresent()) {
            Host host = hostOpt.get();
            if (passwordEncoder.matches(rawPassword, host.getPassword())) {
                return Optional.of(host);
            }
        }
        return Optional.empty();
    }



    public Host updateHost(Long id, Host updatedHost) throws Exception {
        Optional<Host> existingHostOpt = hostRepository.findById(id);
        if (!existingHostOpt.isPresent()) {
            throw new Exception("Host not found");
        }

        Host existingHost = existingHostOpt.get();

        // Update fields (except password handled separately)
        existingHost.setFirstName(updatedHost.getFirstName());
        existingHost.setLastName(updatedHost.getLastName());
        existingHost.setEmail(updatedHost.getEmail());
        existingHost.setMobileNumber(updatedHost.getMobileNumber());
        existingHost.setDateOfBirth(updatedHost.getDateOfBirth());
        existingHost.setEventCategory(updatedHost.getEventCategory());

        // Update password only if provided (not null or empty)
        if (updatedHost.getPassword() != null && !updatedHost.getPassword().isEmpty()) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(updatedHost.getPassword());
            existingHost.setPassword(hashedPassword);
        }

        return hostRepository.save(existingHost);
    }

    
public Optional<Host> getHostByEmail(String email) {
    return hostRepository.findByEmail(email);
}


}
