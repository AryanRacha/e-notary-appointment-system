package com.enotary.repository;

import com.enotary.model.BookingRequest;
import com.enotary.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    Optional<BookingRequest> findByReferenceCode(String referenceCode);
    List<BookingRequest> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<BookingRequest> findBySlotNotaryIdOrderByCreatedAtDesc(Long notaryId);
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<BookingStatus> statuses);
}
