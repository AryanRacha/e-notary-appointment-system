package com.enotary.config;

import com.enotary.model.Notary;
import com.enotary.model.User;
import com.enotary.service.SlotService;
import com.enotary.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final SlotService slotService;

    public DataInitializer(UserService userService, SlotService slotService) {
        this.userService = userService;
        this.slotService = slotService;
    }

    @Override
    public void run(String... args) {
        List<Notary> existing = userService.getAllNotaries();
        if (existing.isEmpty()) {
            // Seed Demo Notaries
            Notary notary1 = userService.registerNotary(
                    "Sarah Jenkins, Esq.",
                    "sarah.jenkins@notary.org",
                    "notary123",
                    "555-0101",
                    "NOT-NY-2024-8841",
                    "500 Madison Ave, New York, NY",
                    "Affidavits, Real Estate & Escrow Deeds"
            );

            Notary notary2 = userService.registerNotary(
                    "David Miller, Notary Public",
                    "david.miller@notary.org",
                    "notary456",
                    "555-0102",
                    "NOT-CA-2023-1192",
                    "1200 Market St, San Francisco, CA",
                    "Power of Attorney, Wills & Estate Planning"
            );

            // Seed Demo Client
            User client = userService.registerClient(
                    "John Citizen",
                    "john.citizen@example.com",
                    "client123",
                    "555-0199"
            );

            // Generate slots for tomorrow and day after
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDate dayAfter = LocalDate.now().plusDays(2);

            slotService.generateSlots(notary1.getId(), tomorrow, LocalTime.of(9, 0), LocalTime.of(17, 0));
            slotService.generateSlots(notary2.getId(), tomorrow, LocalTime.of(10, 0), LocalTime.of(16, 0));
            slotService.generateSlots(notary1.getId(), dayAfter, LocalTime.of(9, 0), LocalTime.of(13, 0));
        }
    }
}
