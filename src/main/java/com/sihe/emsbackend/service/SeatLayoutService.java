package com.sihe.emsbackend.service;

import com.sihe.emsbackend.model.Booking;
import com.sihe.emsbackend.model.Event;
import com.sihe.emsbackend.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeatLayoutService {

    private static final int SEATS_PER_ROW = 8;

    private final BookingRepository bookingRepository;

    public SeatLayoutService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public SeatLayoutResponse buildSeatLayout(Event event) {
        List<Booking> confirmedBookings = bookingRepository.findByEventIdAndStatusIgnoreCase(event.getId(), "CONFIRMED");
        int bookedCount = confirmedBookings.stream()
                .mapToInt(booking -> booking.getQuantity() == null ? 0 : booking.getQuantity())
                .sum();

        int remainingSeats = event.getTicketQuantity() == null ? 0 : event.getTicketQuantity();
        int totalSeats = remainingSeats + bookedCount;
        List<String> allSeatLabels = generateSeatLabels(totalSeats);
        Set<String> bookedSeats = resolveBookedSeats(confirmedBookings, allSeatLabels);

        List<SeatItem> seats = new ArrayList<>();
        for (int index = 0; index < allSeatLabels.size(); index++) {
            String label = allSeatLabels.get(index);
            seats.add(new SeatItem(
                    label,
                    bookedSeats.contains(label) ? "BOOKED" : "AVAILABLE",
                    (index / SEATS_PER_ROW) + 1
            ));
        }

        return new SeatLayoutResponse(
                totalSeats,
                bookedSeats.size(),
                Math.max(totalSeats - bookedSeats.size(), 0),
                SEATS_PER_ROW,
                seats
        );
    }

    public List<String> assignRequestedSeats(Event event, Integer requestedQuantity, List<String> requestedSeats) {
        SeatLayoutResponse layout = buildSeatLayout(event);
        int quantity = requestedQuantity == null ? 0 : requestedQuantity;

        if (quantity <= 0) {
            throw new IllegalArgumentException("Select at least one seat");
        }

        Set<String> availableSeats = new LinkedHashSet<>();
        for (SeatItem seat : layout.getSeats()) {
            if ("AVAILABLE".equals(seat.getStatus())) {
                availableSeats.add(seat.getLabel());
            }
        }

        if (quantity > availableSeats.size()) {
            throw new IllegalArgumentException("Not enough seats available");
        }

        if (requestedSeats != null && !requestedSeats.isEmpty()) {
            if (requestedSeats.size() != quantity) {
                throw new IllegalArgumentException("Selected seat count must match booking quantity");
            }

            List<String> normalizedSeats = requestedSeats.stream()
                    .map(String::trim)
                    .toList();

            Set<String> uniqueSeats = new LinkedHashSet<>(normalizedSeats);
            if (uniqueSeats.size() != normalizedSeats.size()) {
                throw new IllegalArgumentException("Duplicate seats selected");
            }

            for (String seat : normalizedSeats) {
                if (!availableSeats.contains(seat)) {
                    throw new IllegalArgumentException("Seat " + seat + " is no longer available");
                }
            }

            return new ArrayList<>(normalizedSeats);
        }

        List<String> autoAssigned = new ArrayList<>();
        for (String seat : availableSeats) {
            autoAssigned.add(seat);
            if (autoAssigned.size() == quantity) {
                break;
            }
        }

        return autoAssigned;
    }

    private Set<String> resolveBookedSeats(List<Booking> bookings, List<String> allSeatLabels) {
        Set<String> takenSeats = new LinkedHashSet<>();

        for (Booking booking : bookings) {
            List<String> storedSeats = booking.getSeatNumberList();
            if (!storedSeats.isEmpty()) {
                takenSeats.addAll(storedSeats);
                continue;
            }

            int quantity = booking.getQuantity() == null ? 0 : booking.getQuantity();
            for (String seatLabel : allSeatLabels) {
                if (!takenSeats.contains(seatLabel)) {
                    takenSeats.add(seatLabel);
                    quantity--;
                }

                if (quantity <= 0) {
                    break;
                }
            }
        }

        return takenSeats;
    }

    private List<String> generateSeatLabels(int totalSeats) {
        List<String> labels = new ArrayList<>();
        for (int index = 0; index < totalSeats; index++) {
            int rowIndex = index / SEATS_PER_ROW;
            int seatNumber = (index % SEATS_PER_ROW) + 1;
            labels.add(String.valueOf((char) ('A' + rowIndex)) + seatNumber);
        }
        return labels;
    }

    public static class SeatLayoutResponse {
        private final int totalSeats;
        private final int bookedSeats;
        private final int availableSeats;
        private final int seatsPerRow;
        private final List<SeatItem> seats;

        public SeatLayoutResponse(int totalSeats, int bookedSeats, int availableSeats, int seatsPerRow, List<SeatItem> seats) {
            this.totalSeats = totalSeats;
            this.bookedSeats = bookedSeats;
            this.availableSeats = availableSeats;
            this.seatsPerRow = seatsPerRow;
            this.seats = seats;
        }

        public int getTotalSeats() { return totalSeats; }
        public int getBookedSeats() { return bookedSeats; }
        public int getAvailableSeats() { return availableSeats; }
        public int getSeatsPerRow() { return seatsPerRow; }
        public List<SeatItem> getSeats() { return seats; }
    }

    public static class SeatItem {
        private final String label;
        private final String status;
        private final int row;

        public SeatItem(String label, String status, int row) {
            this.label = label;
            this.status = status;
            this.row = row;
        }

        public String getLabel() { return label; }
        public String getStatus() { return status; }
        public int getRow() { return row; }
    }
}
