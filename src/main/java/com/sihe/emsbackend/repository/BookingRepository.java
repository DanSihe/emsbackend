// src/main/java/com/sihe/emsbackend/repository/BookingRepository.java
package com.sihe.emsbackend.repository;

import com.sihe.emsbackend.model.Booking;
import com.sihe.emsbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
}
