package com.enotary.controller;

import com.enotary.model.BookingRequest;
import com.enotary.model.Notary;
import com.enotary.model.User;
import com.enotary.service.BookingService;
import com.enotary.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingRestController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> payload, HttpSession session) {
        User client = (User) session.getAttribute("currentUser");
        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please sign in to book an appointment"));
        }

        try {
            Long slotId = Long.valueOf(payload.get("slotId").toString());
            String documentType = (String) payload.getOrDefault("documentType", "AFFIDAVIT");
            String notes = (String) payload.getOrDefault("notes", "");

            BookingRequest booking = bookingService.createBooking(client, slotId, documentType, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/track/{code}")
    public ResponseEntity<?> trackBooking(@PathVariable("code") String code) {
        Optional<BookingRequest> bookingOpt = bookingService.trackBooking(code);
        if (bookingOpt.isPresent()) {
            return ResponseEntity.ok(bookingOpt.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No appointment found for reference code: " + code));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBookings(HttpSession session) {
        User client = (User) session.getAttribute("currentUser");
        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(bookingService.getClientBookings(client.getId()));
    }

    @GetMapping("/notary")
    public ResponseEntity<?> getNotaryBookings(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.getRole().name().equals("NOTARY")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Notary access only"));
        }

        Optional<Notary> notaryOpt = userService.getNotaryByUserId(user.getId());
        if (notaryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Notary record not found"));
        }

        return ResponseEntity.ok(bookingService.getNotaryBookings(notaryOpt.get().getId()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable("id") Long id) {
        try {
            BookingRequest confirmed = bookingService.confirmBooking(id);
            return ResponseEntity.ok(Map.of("success", true, "booking", confirmed));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        String reason = payload.get("cancellationReason");
        if (reason == null || reason.trim().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A cancellation reason is required"));
        }

        try {
            BookingRequest cancelled = bookingService.cancelBooking(id, reason);
            return ResponseEntity.ok(Map.of("success", true, "booking", cancelled));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
