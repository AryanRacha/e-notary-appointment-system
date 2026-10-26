package com.enotary.service;

import com.enotary.model.AppointmentSlot;
import com.enotary.model.Notary;
import com.enotary.repository.AppointmentSlotRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SlotServiceTest {

    @Autowired
    private SlotService slotService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private NotaryRepository notaryRepository;

    @Autowired
    private UserRepository userRepository;

    private Notary notary;
    private final LocalDate testDate = LocalDate.now().plusDays(5);

    @BeforeEach
    void setUp() {
        slotRepository.deleteAll();
        notaryRepository.deleteAll();
        userRepository.deleteAll();

        notary = userService.registerNotary(
                "Jane Notary", "jane@notary.com", "pass123", "555-9000",
                "LIC-TEST-001", "123 State St", "General"
        );
    }

    @Test
    void generateSlots_GeneratesCorrect30MinIntervals() {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(11, 0); // 2 hours = 4 slots (09:00, 09:30, 10:00, 10:30)

        List<AppointmentSlot> created = slotService.generateSlots(notary.getId(), testDate, start, end);

        assertThat(created).hasSize(4);
        assertThat(created.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(created.get(0).getEndTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(created.get(3).getStartTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(created.get(3).getEndTime()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    void generateSlots_EndTimeBeforeStartTime_ThrowsException() {
        LocalTime start = LocalTime.of(15, 0);
        LocalTime end = LocalTime.of(10, 0);

        assertThatThrownBy(() -> slotService.generateSlots(notary.getId(), testDate, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void getAvailableSlots_OnlyReturnsUnbookedSlots() {
        List<AppointmentSlot> created = slotService.generateSlots(
                notary.getId(), testDate, LocalTime.of(10, 0), LocalTime.of(11, 0)
        ); // 2 slots

        AppointmentSlot slot1 = created.get(0);
        slot1.setBooked(true);
        slotRepository.save(slot1);

        List<AppointmentSlot> available = slotService.getAvailableSlots(notary.getId(), testDate);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getId()).isEqualTo(created.get(1).getId());
    }
}
