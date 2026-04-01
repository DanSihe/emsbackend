package com.sihe.emsbackend.repository;

import com.sihe.emsbackend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEventIdOrderByCreatedAtDesc(Long eventId);
    List<Review> findByEventHostIdOrderByCreatedAtDesc(Long hostId);
    List<Review> findAllByOrderByCreatedAtDesc();
}
