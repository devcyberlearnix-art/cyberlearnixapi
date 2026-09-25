package com.example.instructorservice.repository;

import com.example.instructorservice.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstructorRepository extends JpaRepository<Instructor, UUID> {

    Optional<Instructor> findFirstByUserId(UUID userId);
    default Optional<Instructor> findByUserId(UUID userId) {
        if (userId == null) return Optional.empty();
        return findFirstByUserId(userId);
    }
}
