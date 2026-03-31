// src/main/java/com/sihe/emsbackend/controller/BookingController.java
package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.model.Booking;
import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.User;
import com.sihe.emsbackend.repository.BookingRepository;
import com.sihe.emsbackend.repository.EventRepository;
import com.sihe.emsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @Transactional // CRITICAL: Ensures atomic operation (prevents partial updates)
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {

        Optional<Event> optEvent = eventRepository.findById(request.getEventId());
        Optional<User> optUser = userRepository.findById(request.getUserId());

        if (optEvent.isEmpty() || optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid event or user");
        }

        Event event = optEvent.get();
        User user = optUser.get();

        // Validate Quantity
        if (request.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity must be at least 1");
        }

        if (request.getQuantity() > event.getTicketQuantity()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough tickets available");
        }

        // 1. Update Event Inventory
        event.setTicketQuantity(event.getTicketQuantity() - request.getQuantity());
        eventRepository.save(event);

        // 2. Create and Save Booking
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setQuantity(request.getQuantity());

        // Calculate total price on backend for security
        booking.setTotalPrice(event.getTicketPrice() * request.getQuantity());

        bookingRepository.save(booking);

        return ResponseEntity.ok(toBookingResponse(booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(bookingRepository.findByUser(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable Long bookingId) {
        Optional<Booking> optBooking = bookingRepository.findById(bookingId);

        if (optBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }

        return ResponseEntity.ok(toBookingResponse(optBooking.get()));
    }

    @DeleteMapping("/{bookingId}")
    @Transactional
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        Optional<Booking> optBooking = bookingRepository.findById(bookingId);

        if (optBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }

        Booking booking = optBooking.get();
        Event event = booking.getEvent();

        // Restore tickets
        event.setTicketQuantity(event.getTicketQuantity() + booking.getQuantity());
        eventRepository.save(event);
        bookingRepository.delete(booking);

        return ResponseEntity.ok("Booking cancelled successfully");
    }

    private BookingResponse toBookingResponse(Booking booking) {
        Event event = booking.getEvent();
        User user = booking.getUser();

        return new BookingResponse(
                booking.getId(),
                event.getId(),
                event.getTitle(),
                event.getCategory(),
                event.getDate(),
                event.getLocation(),
                event.getImageUrl(),
                booking.getQuantity(),
                event.getTicketPrice(),
                booking.getTotalPrice(),
                event.getTicketQuantity(),
                booking.getCreatedAt(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                "CONFIRMED"
        );
    }

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

    public static class BookingResponse {
        private Long bookingId;
        private Long eventId;
        private String eventTitle;
        private String category;
        private Object eventDate;
        private String location;
        private String imageUrl;
        private Integer quantity;
        private Double ticketPrice;
        private Double totalPrice;
        private Integer remainingTickets;
        private Object bookedAt;
        private String firstName;
        private String lastName;
        private String email;
        private String status;

        public BookingResponse(
                Long bookingId,
                Long eventId,
                String eventTitle,
                String category,
                Object eventDate,
                String location,
                String imageUrl,
                Integer quantity,
                Double ticketPrice,
                Double totalPrice,
                Integer remainingTickets,
                Object bookedAt,
                String firstName,
                String lastName,
                String email,
                String status
        ) {
            this.bookingId = bookingId;
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.category = category;
            this.eventDate = eventDate;
            this.location = location;
            this.imageUrl = imageUrl;
            this.quantity = quantity;
            this.ticketPrice = ticketPrice;
            this.totalPrice = totalPrice;
            this.remainingTickets = remainingTickets;
            this.bookedAt = bookedAt;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.status = status;
        }

        public Long getBookingId() { return bookingId; }
        public Long getEventId() { return eventId; }
        public String getEventTitle() { return eventTitle; }
        public String getCategory() { return category; }
        public Object getEventDate() { return eventDate; }
        public String getLocation() { return location; }
        public String getImageUrl() { return imageUrl; }
        public Integer getQuantity() { return quantity; }
        public Double getTicketPrice() { return ticketPrice; }
        public Double getTotalPrice() { return totalPrice; }
        public Integer getRemainingTickets() { return remainingTickets; }
        public Object getBookedAt() { return bookedAt; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
    }
}
