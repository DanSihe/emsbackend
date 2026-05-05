package com.sihe.emsbackend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    private String title;
    private String category;
    private LocalDate date;
    private String location;
    private String description;

    private Integer ticketQuantity;   // NEW FIELD
    private Double ticketPrice;       // NEW FIELD
    private String status;
    private String approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private Host host;

    private String  userEmail;



    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTicketQuantity() { return ticketQuantity; }
    public void setTicketQuantity(Integer ticketQuantity) { this.ticketQuantity = ticketQuantity; }

    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }

    public Host getHost() { return host; }
    public void setHost(Host host) { this.host = host; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
        if (approvalStatus == null || approvalStatus.isBlank()) {
            approvalStatus = "PENDING";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
        if (approvalStatus == null || approvalStatus.isBlank()) {
            approvalStatus = "PENDING";
        }
    }

}
