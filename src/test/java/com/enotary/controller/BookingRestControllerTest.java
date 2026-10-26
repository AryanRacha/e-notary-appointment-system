package com.enotary.controller;

import com.enotary.model.AppointmentSlot;
import com.enotary.model.BookingRequest;
import com.enotary.model.Notary;
import com.enotary.model.User;
import com.enotary.repository.AppointmentSlotRepository;
import com.enotary.repository.BookingRequestRepository;
import com.enotary.repository.NotaryRepository;
import com.enotary.repository.UserRepository;
import com.enotary.service.BookingService;
import com.enotary.service.SlotService;
import com.enotary.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private SlotService slotService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRequestRepository bookingRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private NotaryRepository notaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User client;
    private Notary notary;
    private AppointmentSlot slot;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        slotRepository.deleteAll();
        notaryRepository.deleteAll();
        userRepository.deleteAll();

        client = userService.registerClient("API Tester", "apitester@example.com", "pass123", "555-0100");
        notary = userService.registerNotary(
                "API Notary", "apinotary@example.com", "pass123", "555-0200",
                "LIC-API-99", "100 Tech Way", "General"
        );

        List<AppointmentSlot> slots = slotService.generateSlots(
                notary.getId(), LocalDate.now().plusDays(3), LocalTime.of(11, 0), LocalTime.of(12, 0)
        );
        slot = slots.get(0);
    }

    @Test
    void createBooking_AuthenticatedClient_ReturnsCreated() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", client);

        Map<String, Object> payload = Map.of(
                "slotId", slot.getId(),
                "documentType", "AFFIDAVIT",
                "notes", "Passport notarization"
        );

        mockMvc.perform(post("/api/bookings")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceCode").exists())
                .andExpect(jsonPath("$.status", is("REQUESTED")));
    }

    @Test
    void trackBooking_ValidReference_ReturnsBookingDetails() throws Exception {
        BookingRequest booking = bookingService.createBooking(client, slot.getId(), "PROPERTY_DEED", "Deed review");

        mockMvc.perform(get("/api/bookings/track/" + booking.getReferenceCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceCode", is(booking.getReferenceCode())))
                .andExpect(jsonPath("$.status", is("REQUESTED")));
    }

    @Test
    void confirmBooking_ValidId_ReturnsConfirmedStatus() throws Exception {
        BookingRequest booking = bookingService.createBooking(client, slot.getId(), "AFFIDAVIT", "Notes");

        mockMvc.perform(post("/api/bookings/" + booking.getId() + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.booking.status", is("CONFIRMED")));
    }

    @Test
    void cancelBooking_WithReason_ReturnsCancelledStatus() throws Exception {
        BookingRequest booking = bookingService.createBooking(client, slot.getId(), "AFFIDAVIT", "Notes");

        Map<String, String> payload = Map.of("cancellationReason", "Illness");

        mockMvc.perform(post("/api/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.booking.status", is("CANCELLED")));
    }
}
