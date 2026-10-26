package com.enotary.repository;

import com.enotary.model.Notary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotaryRepository extends JpaRepository<Notary, Long> {
    Optional<Notary> findByUserId(Long userId);
    Optional<Notary> findByLicenseNumber(String licenseNumber);
    boolean existsByLicenseNumber(String licenseNumber);
}
