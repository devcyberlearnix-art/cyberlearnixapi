package com.example.notification.repository;

import com.example.notification.entity.CertificateDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificateDeliveryRepository
        extends JpaRepository<CertificateDelivery, UUID> {

    List<CertificateDelivery> findByNotificationId(UUID notificationId);
}