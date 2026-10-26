package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByStudentIdAndCourseId(UUID studentId, Long courseId);

    Optional<Certificate> findByStudentIdAndCourseIdAndStatus(UUID studentId, Long courseId, Certificate.CertificateStatus status);

    Optional<Certificate> findByVerificationCode(String verificationCode);

    List<Certificate> findByStudentId(UUID studentId);

    List<Certificate> findByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(UUID studentId, Long courseId);

    boolean existsByCertificateNumber(String certificateNumber);

    boolean existsByVerificationCode(String verificationCode);
}
