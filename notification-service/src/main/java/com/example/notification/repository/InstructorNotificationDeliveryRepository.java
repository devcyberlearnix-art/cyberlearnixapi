package com.example.notification.repository;

import com.example.notification.entity.InstructorNotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstructorNotificationDeliveryRepository extends JpaRepository<InstructorNotificationDelivery, UUID> {

    List<InstructorNotificationDelivery> findByNotificationId(UUID notificationId);
}