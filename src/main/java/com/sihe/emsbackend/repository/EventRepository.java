package com.sihe.emsbackend.repository;

import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByHost(Host host);
}
