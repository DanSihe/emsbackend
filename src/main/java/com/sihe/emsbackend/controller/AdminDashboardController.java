package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.Host;
import com.sihe.emsbackend.model.User;
import com.sihe.emsbackend.repository.EventRepository;
import com.sihe.emsbackend.repository.HostRepository;
import com.sihe.emsbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final HostRepository hostRepository;
    private final EventRepository eventRepository;

    public AdminDashboardController(UserRepository userRepository, HostRepository hostRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.hostRepository = hostRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        List<User> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();
        List<Host> hosts = hostRepository.findAll().stream()
                .sorted(Comparator.comparing(Host::getId).reversed())
                .toList();
        List<Event> events = eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        AdminDashboardResponse response = new AdminDashboardResponse(
                new AdminStats(
                        users.size(),
                        users.stream().filter(user -> "PENDING".equalsIgnoreCase(user.getApprovalStatus())).count(),
                        hosts.size(),
                        hosts.stream().filter(host -> "PENDING".equalsIgnoreCase(host.getApprovalStatus())).count(),
                        events.size(),
                        events.stream().filter(event -> "PENDING".equalsIgnoreCase(event.getApprovalStatus())).count()
                ),
                users,
                hosts,
                events
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/approval")
    public ResponseEntity<?> updateUserApproval(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();
        user.setApprovalStatus(normalizeApproval(request.getApprovalStatus()));
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PatchMapping("/hosts/{id}/approval")
    public ResponseEntity<?> updateHostApproval(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        Optional<Host> optionalHost = hostRepository.findById(id);
        if (optionalHost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Host not found");
        }

        Host host = optionalHost.get();
        host.setApprovalStatus(normalizeApproval(request.getApprovalStatus()));
        return ResponseEntity.ok(hostRepository.save(host));
    }

    @PatchMapping("/events/{id}/approval")
    public ResponseEntity<?> updateEventApproval(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        Event event = optionalEvent.get();
        event.setApprovalStatus(normalizeApproval(request.getApprovalStatus()));
        return ResponseEntity.ok(eventRepository.save(event));
    }

    private String normalizeApproval(String value) {
        String approval = value == null ? "" : value.trim().toUpperCase();
        if (!approval.equals("APPROVED") && !approval.equals("REJECTED") && !approval.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid approval status");
        }
        return approval;
    }

    public static class ApprovalRequest {
        private String approvalStatus;

        public String getApprovalStatus() {
            return approvalStatus;
        }

        public void setApprovalStatus(String approvalStatus) {
            this.approvalStatus = approvalStatus;
        }
    }

    public static class AdminStats {
        private final long totalUsers;
        private final long pendingUsers;
        private final long totalHosts;
        private final long pendingHosts;
        private final long totalEvents;
        private final long pendingEvents;

        public AdminStats(long totalUsers, long pendingUsers, long totalHosts, long pendingHosts, long totalEvents, long pendingEvents) {
            this.totalUsers = totalUsers;
            this.pendingUsers = pendingUsers;
            this.totalHosts = totalHosts;
            this.pendingHosts = pendingHosts;
            this.totalEvents = totalEvents;
            this.pendingEvents = pendingEvents;
        }

        public long getTotalUsers() { return totalUsers; }
        public long getPendingUsers() { return pendingUsers; }
        public long getTotalHosts() { return totalHosts; }
        public long getPendingHosts() { return pendingHosts; }
        public long getTotalEvents() { return totalEvents; }
        public long getPendingEvents() { return pendingEvents; }
    }

    public static class AdminDashboardResponse {
        private final AdminStats stats;
        private final List<User> users;
        private final List<Host> hosts;
        private final List<Event> events;

        public AdminDashboardResponse(AdminStats stats, List<User> users, List<Host> hosts, List<Event> events) {
            this.stats = stats;
            this.users = users;
            this.hosts = hosts;
            this.events = events;
        }

        public AdminStats getStats() { return stats; }
        public List<User> getUsers() { return users; }
        public List<Host> getHosts() { return hosts; }
        public List<Event> getEvents() { return events; }
    }
}
