package com.lms.courseservice.repository;

import com.lms.courseservice.entity.CertificateRevocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRevocationRepository extends JpaRepository<CertificateRevocation, Long> {

    Optional<CertificateRevocation> findByCertificateNumber(String certificateNumber);

    Optional<CertificateRevocation> findByVerificationCode(String verificationCode);

    List<CertificateRevocation> findByStudentId(UUID studentId);

    List<CertificateRevocation> findByCourseId(Long courseId);

    List<CertificateRevocation> findByStudentIdAndCourseId(UUID studentId, Long courseId);
}
