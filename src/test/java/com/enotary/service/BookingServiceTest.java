package com.enotary.service;

import com.enotary.model.*;
import com.enotary.repository.AppointmentSlotRepository;
import com.enotary.repository.BookingRequestRepository;
import com.enotary.repository.NotaryRepository;
import com.enotary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SlotService slotService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRequestRepository bookingRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private NotaryRepository notaryRepository;

    @Autowired
    private UserRepository userRepository;

    private User client;
    private Notary notary;
    private AppointmentSlot slot;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        slotRepository.deleteAll();
        notaryRepository.deleteAll();
        userRepository.deleteAll();

        client = userService.registerClient("Alice Client", "alice@bookingtest.com", "pass123", "555-1111");
        notary = userService.registerNotary(
                "Bob Notary", "bob@bookingtest.com", "pass123", "555-2222",
                "NOT-BOOK-001", "100 Pine St", "Deeds"
        );

        List<AppointmentSlot> slots = slotService.generateSlots(
                notary.getId(), LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(15, 0)
        );
        slot = slots.get(0);
    }

    @Test
    void createBooking_Success_GeneratesReferenceCodeAndMarksSlotBooked() {
        BookingRequest booking = bookingService.createBooking(client, slot.getId(), "AFFIDAVIT", "Witness required");

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getReferenceCode()).startsWith("ENOT-");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(booking.getClient().getId()).isEqualTo(client.getId());

        AppointmentSlot updatedSlot = slotRepository.findById(slot.getId()).orElseThrow();
        assertThat(updatedSlot.isBooked()).isTrue();
    }

    @Test
    void createBooking_WhenSlotAlreadyBooked_ThrowsIllegalStateException() {
        bookingService.createBooking(client, slot.getId(), "AFFIDAVIT", "First booking");

        User client2 = userService.registerClient("Second Client", "second@client.com", "pass123", "555-3333");

        // Concurrent attempt to book the exact same slot must be rejected
        assertThatThrownBy(() -> bookingService.createBooking(client2, slot.getId(), "POWER_OF_ATTORNEY", "Second booking"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void confirmBooking_Success_UpdatesStatusToConfirmed() {
        BookingRequest booking = bookingService.createBooking(client, slot.getId(), "PROPERTY_DEED", "Real estate transfer");

        BookingRequest confirmed = bookingService.confirmBooking(booking.getId());

        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancelBooking_ReactivatesSlotToAvailable() {
        BookingRequest booking = bookingService.createBooking(client, slot.getId(), "AFFIDAVIT", "Notes");
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().isBooked()).isTrue();

        BookingRequest cancelled = bookingService.cancelBooking(booking.getId(), "Client rescheduling due to travel");

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.getCancellationReason()).isEqualTo("Client rescheduling due to travel");

        // Slot must now be dynamically freed and available again
        AppointmentSlot releasedSlot = slotRepository.findById(slot.getId()).orElseThrow();
        assertThat(releasedSlot.isBooked()).isFalse();
    }

    @Test
    void trackBooking_ByReferenceCode_ReturnsAppointmentDetails() {
        BookingRequest booking = bookingService.createBooking(client, slot.getId(), "AFFIDAVIT", "Track test");

        Optional<BookingRequest> tracked = bookingService.trackBooking(booking.getReferenceCode());

        assertThat(tracked).isPresent();
        assertThat(tracked.get().getId()).isEqualTo(booking.getId());
        assertThat(tracked.get().getReferenceCode()).isEqualTo(booking.getReferenceCode());
    }
}
