package com.lms.courseservice.repository;

import com.lms.courseservice.entity.CertificateSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface CertificateSequenceRepository extends JpaRepository<CertificateSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CertificateSequence> findByYear(Integer year);

    boolean existsByYear(Integer year);
}
