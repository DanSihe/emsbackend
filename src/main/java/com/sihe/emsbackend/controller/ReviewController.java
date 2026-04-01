package com.sihe.emsbackend.controller;

import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.model.Review;
import com.sihe.emsbackend.model.User;
import com.sihe.emsbackend.repository.EventRepository;
import com.sihe.emsbackend.repository.ReviewRepository;
import com.sihe.emsbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ReviewController(
            ReviewRepository reviewRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ReviewResponse>> getEventReviews(@PathVariable Long eventId) {
        return ResponseEntity.ok(
                reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId)
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<ReviewResponse>> getHostReviews(@PathVariable Long hostId) {
        return ResponseEntity.ok(
                reviewRepository.findByEventHostIdOrderByCreatedAtDesc(hostId)
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/public")
    public ResponseEntity<List<ReviewResponse>> getPublicReviews() {
        return ResponseEntity.ok(
                reviewRepository.findAllByOrderByCreatedAtDesc()
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody CreateReviewRequest request) {
        Optional<Event> optEvent = eventRepository.findById(request.getEventId());
        Optional<User> optUser = userRepository.findById(request.getUserId());

        if (optEvent.isEmpty() || optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid event or user");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rating must be between 1 and 5");
        }

        if (request.getComment() == null || request.getComment().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Review comment is required");
        }

        Review review = new Review();
        review.setEvent(optEvent.get());
        review.setUser(optUser.get());
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());

        return ResponseEntity.ok(toResponse(reviewRepository.save(review)));
    }

    @PatchMapping("/{reviewId}/reply")
    public ResponseEntity<?> replyToReview(@PathVariable Long reviewId, @RequestBody ReplyRequest request) {
        Optional<Review> optReview = reviewRepository.findById(reviewId);
        if (optReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");
        }

        if (request.getHostReply() == null || request.getHostReply().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reply is required");
        }

        Review review = optReview.get();
        review.setHostReply(request.getHostReply().trim());
        review.setRepliedAt(LocalDateTime.now());

        return ResponseEntity.ok(toResponse(reviewRepository.save(review)));
    }

    private ReviewResponse toResponse(Review review) {
        String reviewerName = ((review.getUser().getFirstName() == null ? "" : review.getUser().getFirstName()) + " "
                + (review.getUser().getLastName() == null ? "" : review.getUser().getLastName())).trim();

        return new ReviewResponse(
                review.getId(),
                review.getEvent().getId(),
                review.getEvent().getTitle(),
                review.getRating(),
                review.getComment(),
                review.getHostReply(),
                review.getCreatedAt(),
                review.getRepliedAt(),
                reviewerName.isBlank() ? "Guest user" : reviewerName,
                review.getUser().getEmail()
        );
    }

    public static class CreateReviewRequest {
        private Long eventId;
        private Long userId;
        private Integer rating;
        private String comment;

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public static class ReplyRequest {
        private String hostReply;

        public String getHostReply() { return hostReply; }
        public void setHostReply(String hostReply) { this.hostReply = hostReply; }
    }

    public static class ReviewResponse {
        private Long id;
        private Long eventId;
        private String eventTitle;
        private Integer rating;
        private String comment;
        private String hostReply;
        private LocalDateTime createdAt;
        private LocalDateTime repliedAt;
        private String reviewerName;
        private String reviewerEmail;

        public ReviewResponse(Long id, Long eventId, String eventTitle, Integer rating, String comment, String hostReply,
                              LocalDateTime createdAt, LocalDateTime repliedAt, String reviewerName, String reviewerEmail) {
            this.id = id;
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.rating = rating;
            this.comment = comment;
            this.hostReply = hostReply;
            this.createdAt = createdAt;
            this.repliedAt = repliedAt;
            this.reviewerName = reviewerName;
            this.reviewerEmail = reviewerEmail;
        }

        public Long getId() { return id; }
        public Long getEventId() { return eventId; }
        public String getEventTitle() { return eventTitle; }
        public Integer getRating() { return rating; }
        public String getComment() { return comment; }
        public String getHostReply() { return hostReply; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getRepliedAt() { return repliedAt; }
        public String getReviewerName() { return reviewerName; }
        public String getReviewerEmail() { return reviewerEmail; }
    }
}
