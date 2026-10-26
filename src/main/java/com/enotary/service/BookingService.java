package com.enotary.service;

import com.enotary.model.AppointmentSlot;
import com.enotary.model.BookingRequest;
import com.enotary.model.BookingStatus;
import com.enotary.model.User;
import com.enotary.repository.AppointmentSlotRepository;
import com.enotary.repository.BookingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRequestRepository bookingRepository;
    private final AppointmentSlotRepository slotRepository;

    public BookingService(BookingRequestRepository bookingRepository, AppointmentSlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public BookingRequest createBooking(User client, Long slotId, String documentType, String notes) {
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment slot not found: " + slotId));

        if (slot.isBooked()) {
            throw new IllegalStateException("Selected appointment slot is already booked");
        }

        boolean activeExists = bookingRepository.existsBySlotIdAndStatusIn(
                slotId, List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED));
        if (activeExists) {
            throw new IllegalStateException("An active booking already exists for this slot");
        }

        // Lock slot
        slot.setBooked(true);
        slotRepository.save(slot);

        // Generate unique reference code
        String referenceCode = generateUniqueReferenceCode();

        BookingRequest booking = new BookingRequest(referenceCode, client, slot, documentType, notes);
        return bookingRepository.save(booking);
    }

    @Transactional
    public BookingRequest confirmBooking(Long bookingId) {
        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled appointment");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public BookingRequest cancelBooking(Long bookingId, String reason) {
        if (reason == null || reason.trim().isBlank()) {
            throw new IllegalArgumentException("A cancellation reason is required");
        }

        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason.trim());

        // Dynamic slot reactivation: free up the slot for others to book
        AppointmentSlot slot = booking.getSlot();
        if (slot != null) {
            slot.setBooked(false);
            slotRepository.save(slot);
        }

        return bookingRepository.save(booking);
    }

    public Optional<BookingRequest> trackBooking(String referenceCode) {
        if (referenceCode == null) return Optional.empty();
        return bookingRepository.findByReferenceCode(referenceCode.trim().toUpperCase());
    }

    public List<BookingRequest> getClientBookings(Long clientId) {
        return bookingRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public List<BookingRequest> getNotaryBookings(Long notaryId) {
        return bookingRepository.findBySlotNotaryIdOrderByCreatedAtDesc(notaryId);
    }

    public Optional<BookingRequest> findById(Long id) {
        return bookingRepository.findById(id);
    }

    private String generateUniqueReferenceCode() {
        int year = LocalDate.now().getYear();
        String code;
        do {
            // INTENTIONAL DEFECT: Changed standardized prefix 'ENOT-' to 'DEFECT-'
            StringBuilder sb = new StringBuilder("DEFECT-" + year + "-");
            for (int i = 0; i < 4; i++) {
                sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
            }
            code = sb.toString();
        } while (bookingRepository.findByReferenceCode(code).isPresent());
        return code;
    }
}
