package com.enotary.repository;

import com.enotary.model.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {
    List<AppointmentSlot> findByNotaryIdAndSlotDateOrderByStartTimeAsc(Long notaryId, LocalDate slotDate);
    List<AppointmentSlot> findByNotaryIdAndSlotDateAndIsBookedFalseOrderByStartTimeAsc(Long notaryId, LocalDate slotDate);
    boolean existsByNotaryIdAndSlotDateAndStartTime(Long notaryId, LocalDate slotDate, LocalTime startTime);
}
