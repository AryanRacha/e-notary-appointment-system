package com.enotary.service;

import com.enotary.model.AppointmentSlot;
import com.enotary.model.Notary;
import com.enotary.repository.AppointmentSlotRepository;
import com.enotary.repository.NotaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SlotService {

    private final AppointmentSlotRepository slotRepository;
    private final NotaryRepository notaryRepository;

    public SlotService(AppointmentSlotRepository slotRepository, NotaryRepository notaryRepository) {
        this.slotRepository = slotRepository;
        this.notaryRepository = notaryRepository;
    }

    @Transactional
    public List<AppointmentSlot> generateSlots(Long notaryId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Notary notary = notaryRepository.findById(notaryId)
                .orElseThrow(() -> new IllegalArgumentException("Notary not found with ID: " + notaryId));

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        List<AppointmentSlot> generated = new ArrayList<>();
        LocalTime current = startTime;

        while (current.plusMinutes(30).isBefore(endTime) || current.plusMinutes(30).equals(endTime)) {
            LocalTime slotEnd = current.plusMinutes(30);

            if (!slotRepository.existsByNotaryIdAndSlotDateAndStartTime(notaryId, date, current)) {
                AppointmentSlot slot = new AppointmentSlot(notary, date, current, slotEnd);
                generated.add(slotRepository.save(slot));
            }
            current = slotEnd;
        }

        return generated;
    }

    public List<AppointmentSlot> getAvailableSlots(Long notaryId, LocalDate date) {
        return slotRepository.findByNotaryIdAndSlotDateAndIsBookedFalseOrderByStartTimeAsc(notaryId, date);
    }

    public List<AppointmentSlot> getAllSlots(Long notaryId, LocalDate date) {
        return slotRepository.findByNotaryIdAndSlotDateOrderByStartTimeAsc(notaryId, date);
    }

    public Optional<AppointmentSlot> findSlotById(Long id) {
        return slotRepository.findById(id);
    }
}
