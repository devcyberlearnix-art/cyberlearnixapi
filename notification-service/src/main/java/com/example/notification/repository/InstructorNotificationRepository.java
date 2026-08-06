package com.example.notification.repository;

import com.example.notification.entity.InstructorNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstructorNotificationRepository extends JpaRepository<InstructorNotification, UUID> {

    List<InstructorNotification> findByInstructorId(UUID instructorId);
}