package com.example.notification.repository;

import com.example.notification.entity.CourseNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseNotificationRepository
        extends JpaRepository<CourseNotification, UUID> {
}