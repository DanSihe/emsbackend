package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.service.EventService;
import com.sihe.emsbackend.service.HostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {

    private final EventService eventService;
    private final HostService hostService;

    @Autowired
    public EventController(EventService eventService, HostService hostService) {
        this.eventService = eventService;
        this.hostService = hostService;
    }

    // Create a new event
    @PostMapping("/create")
    public Event createEvent(@RequestBody Event event) throws Exception {
        if (event.getHost() == null || event.getHost().getEmail() == null) {
            throw new Exception("Host information is required");
        }

        // Fetch the full Host object
        Host host = hostService.getHostByEmail(event.getHost().getEmail())
                .orElseThrow(() -> new Exception("Host not found"));

        event.setHost(host);  // Link the host
        return eventService.createEvent(event);
    }

    // Get all events
    @GetMapping("/all")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    // Get events by host
    @GetMapping("/host")
    public List<Event> getEventsByHost(@RequestParam String hostEmail) throws Exception {
        Host host = hostService.getHostByEmail(hostEmail)
                .orElseThrow(() -> new Exception("Host not found"));
        return eventService.getEventsByHost(host);
    }
}
