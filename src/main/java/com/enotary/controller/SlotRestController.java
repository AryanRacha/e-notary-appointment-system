package com.enotary.controller;

import com.enotary.model.AppointmentSlot;
import com.enotary.model.Notary;
import com.enotary.model.User;
import com.enotary.service.SlotService;
import com.enotary.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/slots")
public class SlotRestController {

    private final SlotService slotService;
    private final UserService userService;

    public SlotRestController(SlotService slotService, UserService userService) {
        this.slotService = slotService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentSlot>> getSlots(
            @RequestParam(value = "notaryId", required = false) Long notaryId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now().plusDays(1);
        }

        if (notaryId == null) {
            List<Notary> notaries = userService.getAllNotaries();
            if (notaries.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            notaryId = notaries.get(0).getId();
        }

        return ResponseEntity.ok(slotService.getAllSlots(notaryId, date));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateSlots(@RequestBody Map<String, String> payload, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Long notaryId;

        if (user != null && user.getRole().name().equals("NOTARY")) {
            Optional<Notary> notaryOpt = userService.getNotaryByUserId(user.getId());
            if (notaryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Notary profile not found"));
            }
            notaryId = notaryOpt.get().getId();
        } else if (payload.containsKey("notaryId")) {
            notaryId = Long.parseLong(payload.get("notaryId"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Must be logged in as notary to generate slots"));
        }

        try {
            LocalDate date = LocalDate.parse(payload.get("date"));
            LocalTime start = LocalTime.parse(payload.get("startTime"));
            LocalTime end = LocalTime.parse(payload.get("endTime"));

            List<AppointmentSlot> created = slotService.generateSlots(notaryId, date, start, end);
            return ResponseEntity.ok(created);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
