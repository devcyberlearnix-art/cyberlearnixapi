package com.example.notification.repository;


import com.example.notification.entity.CourseNotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseNotificationDeliveryRepository
        extends JpaRepository<CourseNotificationDelivery, UUID> {

    List<CourseNotificationDelivery> findByNotificationId(UUID notificationId);
}