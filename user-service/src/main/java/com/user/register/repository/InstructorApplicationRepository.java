package com.user.register.repository;

import com.user.register.entity.InstructorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, UUID> {

    Optional<InstructorApplication> findTopByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<InstructorApplication> findByStatus(InstructorApplication.ApplicationStatus status);

    boolean existsByUserIdAndStatus(UUID userId, InstructorApplication.ApplicationStatus status);
}
