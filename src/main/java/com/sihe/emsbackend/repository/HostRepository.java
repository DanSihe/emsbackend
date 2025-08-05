package com.sihe.emsbackend.repository;

import com.sihe.emsbackend.model.Host;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    boolean existsByEmail(String email);
    Optional<Host> findByEmail(String email);

}
