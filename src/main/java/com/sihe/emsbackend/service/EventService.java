package com.sihe.emsbackend.service;

import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Create or save event
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    // Get all events
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Get events by host
    public List<Event> getEventsByHost(Host host) {
        return eventRepository.findByHostId(host.getId());
    }

    // --- Add these methods ---
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }
    public List<Event> getEventsByHostId(Long hostId) {
    return eventRepository.findByHostId(hostId);
}
}
