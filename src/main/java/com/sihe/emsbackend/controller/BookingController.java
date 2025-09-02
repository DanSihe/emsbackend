// src/main/java/com/sihe/emsbackend/controller/BookingController.java
package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.model.Booking;
import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.User;
import com.sihe.emsbackend.repository.BookingRepository;
import com.sihe.emsbackend.repository.EventRepository;
import com.sihe.emsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:3000")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Booking createBooking(@RequestBody BookingRequest request) {
        Optional<Event> optEvent = eventRepository.findById(request.getEventId());
        Optional<User> optUser = userRepository.findById(request.getUserId());

        if (optEvent.isEmpty() || optUser.isEmpty()) {
            throw new RuntimeException("Invalid event or user");
        }

        Event event = optEvent.get();
        User user = optUser.get();

        if (request.getQuantity() > event.getTicketQuantity()) {
            throw new RuntimeException("Not enough tickets available");
        }

        // Reduce ticket quantity
        event.setTicketQuantity(event.getTicketQuantity() - request.getQuantity());
        eventRepository.save(event);

        // Create booking
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setQuantity(request.getQuantity());
        booking.setTotalPrice(event.getTicketPrice() * request.getQuantity());

        return bookingRepository.save(booking);
    }

    // Get all bookings for a user
    @GetMapping("/user/{userId}")
    public java.util.List<Booking> getBookingsByUser(@PathVariable Long userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) throw new RuntimeException("User not found");

        return bookingRepository.findByUser(optUser.get());
    }

    // DTO for request
    public static class BookingRequest {
        private Long eventId;
        private Long userId;
        private Integer quantity;

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
