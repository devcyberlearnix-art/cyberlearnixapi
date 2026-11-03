package com.user.register.repository;

import com.user.register.entity.InstructorApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, UUID> {
    void deleteByUserId(UUID userId);

    Optional<InstructorApplication> findTopByUserIdOrderBySubmittedAtDesc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM InstructorApplication a WHERE a.id = :id")
    Optional<InstructorApplication> findByIdForUpdate(@Param("id") UUID id);

    List<InstructorApplication> findByStatus(InstructorApplication.ApplicationStatus status);

    boolean existsByUserIdAndStatus(UUID userId, InstructorApplication.ApplicationStatus status);

    Page<InstructorApplication> findAll(Pageable pageable);

    Page<InstructorApplication> findByStatus(InstructorApplication.ApplicationStatus status, Pageable pageable);
}
