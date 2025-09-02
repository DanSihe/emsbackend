package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.service.EventService;
import com.sihe.emsbackend.service.HostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

        Host host = hostService.getHostByEmail(event.getHost().getEmail())
                .orElseThrow(() -> new Exception("Host not found"));

        event.setHost(host);
        return eventService.createEvent(event);
    }
@GetMapping("/all") public List<Event> getAllEvents() { return eventService.getAllEvents(); }
    // Get all events (for host table)
    // @GetMapping("/host")
    // public List<Event> getEventsForHost(@RequestParam String hostEmail) {
    //     return eventService.getAllEvents(); // return all events
    // }

    // Cancel event
@DeleteMapping("/{eventId}")
public ResponseEntity<?> cancelEvent(@PathVariable Long eventId) {
    Optional<Event> optionalEvent = eventService.getEventById(eventId);

    if (!optionalEvent.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
    }

    Event event = optionalEvent.get();

    // Compare LocalDate with LocalDate
    if (event.getDate().isBefore(LocalDate.now())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot cancel event that has already started or passed");
    }

    eventService.deleteEvent(event);
    return ResponseEntity.ok("Event canceled successfully");
}
@GetMapping("/host/{hostId}")
public ResponseEntity<List<Event>> getEventsByHost(@PathVariable Long hostId) {
    List<Event> events = eventService.getEventsByHostId(hostId); // use service, not repository
    return ResponseEntity.ok(events);
}
@GetMapping("/{id}")
public ResponseEntity<Event> getEvent(@PathVariable Long id) {
    return eventService.getEventById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}


}
