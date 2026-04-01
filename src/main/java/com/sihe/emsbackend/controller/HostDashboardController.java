package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.model.Booking;
import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.repository.BookingRepository;
import com.sihe.emsbackend.repository.EventRepository;
import com.sihe.emsbackend.repository.HostRepository;
import com.sihe.emsbackend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/host-dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class HostDashboardController {

    private final HostRepository hostRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public HostDashboardController(
            HostRepository hostRepository,
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            NotificationService notificationService
    ) {
        this.hostRepository = hostRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/{hostId}")
    public ResponseEntity<?> getDashboard(@PathVariable Long hostId) {
        Optional<Host> optHost = hostRepository.findById(hostId);
        if (optHost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Host not found");
        }

        List<Event> events = eventRepository.findByHostId(hostId)
                .stream()
                .sorted(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<Booking> bookings = bookingRepository.findByEventHostId(hostId);

        long activeEvents = events.stream().filter(event -> !"CANCELLED".equalsIgnoreCase(event.getStatus())).count();
        long cancelledEvents = events.stream().filter(event -> "CANCELLED".equalsIgnoreCase(event.getStatus())).count();
        long totalBookings = bookings.stream().filter(booking -> !"REFUNDED".equalsIgnoreCase(booking.getStatus())).count();
        long pendingRefunds = bookings.stream().filter(booking -> "PENDING".equalsIgnoreCase(booking.getRefundStatus())).count();
        long refundedBookings = bookings.stream().filter(booking -> "REFUNDED".equalsIgnoreCase(booking.getRefundStatus())).count();

        double revenue = bookings.stream()
                .filter(booking -> "CONFIRMED".equalsIgnoreCase(booking.getStatus()))
                .mapToDouble(booking -> booking.getTotalPrice() == null ? 0.0 : booking.getTotalPrice())
                .sum();

        double refunds = bookings.stream()
                .mapToDouble(booking -> booking.getRefundAmount() == null ? 0.0 : booking.getRefundAmount())
                .sum();

        int totalCapacity = events.stream().mapToInt(event -> event.getTicketQuantity() == null ? 0 : event.getTicketQuantity()).sum();

        HostDashboardResponse response = new HostDashboardResponse(
                new DashboardStats(activeEvents, cancelledEvents, totalBookings, pendingRefunds, refundedBookings, revenue, refunds, totalCapacity),
                events,
                bookings
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable Long eventId, @RequestBody Event updatedEvent) {
        Optional<Event> optEvent = eventRepository.findById(eventId);
        if (optEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        Event event = optEvent.get();
        event.setTitle(updatedEvent.getTitle());
        event.setCategory(updatedEvent.getCategory());
        event.setDate(updatedEvent.getDate());
        event.setLocation(updatedEvent.getLocation());
        event.setDescription(updatedEvent.getDescription());
        event.setImageUrl(updatedEvent.getImageUrl());
        event.setTicketPrice(updatedEvent.getTicketPrice());
        event.setTicketQuantity(updatedEvent.getTicketQuantity());
        if (updatedEvent.getStatus() != null && !updatedEvent.getStatus().isBlank()) {
            event.setStatus(updatedEvent.getStatus());
        }

        return ResponseEntity.ok(eventRepository.save(event));
    }

    @PatchMapping("/events/{eventId}/cancel")
    @Transactional
    public ResponseEntity<?> cancelEventByHost(@PathVariable Long eventId, @RequestBody CancelEventRequest request) {
        Optional<Event> optEvent = eventRepository.findById(eventId);
        if (optEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        Event event = optEvent.get();
        event.setStatus("CANCELLED");
        eventRepository.save(event);

        List<Booking> bookings = bookingRepository.findByEventId(eventId);
        for (Booking booking : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                booking.setStatus("CANCELLED");
                booking.setRefundStatus("PENDING");
                booking.setRefundAmount(booking.getTotalPrice());
                booking.setCancellationReason(request.getReason());
                booking.setCancelledAt(LocalDateTime.now());
                bookingRepository.save(booking);

                String fullName = ((booking.getUser().getFirstName() == null ? "" : booking.getUser().getFirstName()) + " "
                        + (booking.getUser().getLastName() == null ? "" : booking.getUser().getLastName())).trim();
                booking.setNotificationMessage(notificationService.buildCancellationRefundMessage(
                        fullName.isBlank() ? "Customer" : fullName,
                        event.getTitle(),
                        booking.getRefundAmount(),
                        booking.getRefundStatus()
                ));
                bookingRepository.save(booking);
            }
        }

        return ResponseEntity.ok("Event cancelled and customers notified");
    }

    @PatchMapping("/bookings/{bookingId}/refund")
    public ResponseEntity<?> updateRefundStatus(@PathVariable Long bookingId, @RequestBody RefundRequest request) {
        Optional<Booking> optBooking = bookingRepository.findById(bookingId);
        if (optBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }

        Booking booking = optBooking.get();
        booking.setRefundStatus(request.getRefundStatus());
        booking.setRefundAmount(request.getRefundAmount() == null ? booking.getTotalPrice() : request.getRefundAmount());

        if ("REFUNDED".equalsIgnoreCase(request.getRefundStatus())) {
            booking.setStatus("REFUNDED");
            booking.setRefundedAt(LocalDateTime.now());
        }

        String fullName = ((booking.getUser().getFirstName() == null ? "" : booking.getUser().getFirstName()) + " "
                + (booking.getUser().getLastName() == null ? "" : booking.getUser().getLastName())).trim();
        booking.setNotificationMessage(notificationService.buildCancellationRefundMessage(
                fullName.isBlank() ? "Customer" : fullName,
                booking.getEvent().getTitle(),
                booking.getRefundAmount(),
                booking.getRefundStatus()
        ));

        bookingRepository.save(booking);

        return ResponseEntity.ok(booking);
    }

    public static class DashboardStats {
        private long activeEvents;
        private long cancelledEvents;
        private long totalBookings;
        private long pendingRefunds;
        private long refundedBookings;
        private double revenue;
        private double refunds;
        private int availableSeats;

        public DashboardStats(long activeEvents, long cancelledEvents, long totalBookings, long pendingRefunds, long refundedBookings, double revenue, double refunds, int availableSeats) {
            this.activeEvents = activeEvents;
            this.cancelledEvents = cancelledEvents;
            this.totalBookings = totalBookings;
            this.pendingRefunds = pendingRefunds;
            this.refundedBookings = refundedBookings;
            this.revenue = revenue;
            this.refunds = refunds;
            this.availableSeats = availableSeats;
        }

        public long getActiveEvents() { return activeEvents; }
        public long getCancelledEvents() { return cancelledEvents; }
        public long getTotalBookings() { return totalBookings; }
        public long getPendingRefunds() { return pendingRefunds; }
        public long getRefundedBookings() { return refundedBookings; }
        public double getRevenue() { return revenue; }
        public double getRefunds() { return refunds; }
        public int getAvailableSeats() { return availableSeats; }
    }

    public static class HostDashboardResponse {
        private DashboardStats stats;
        private List<Event> events;
        private List<Booking> bookings;

        public HostDashboardResponse(DashboardStats stats, List<Event> events, List<Booking> bookings) {
            this.stats = stats;
            this.events = events;
            this.bookings = bookings;
        }

        public DashboardStats getStats() { return stats; }
        public List<Event> getEvents() { return events; }
        public List<Booking> getBookings() { return bookings; }
    }

    public static class CancelEventRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class RefundRequest {
        private String refundStatus;
        private Double refundAmount;

        public String getRefundStatus() { return refundStatus; }
        public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
        public Double getRefundAmount() { return refundAmount; }
        public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }
    }
}
