package com.example.notification.repository;

import com.example.notification.entity.CertificateNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificateNotificationRepository
        extends JpaRepository<CertificateNotification, UUID> {
}
