package com.example.notification.repository;

import com.example.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {

    Optional<DeviceToken> findByTokenAndUserId(String token, String userId);

    // ✅ ADD THESE METHODS

    @Query("SELECT d.token FROM DeviceToken d WHERE d.userId IN :userIds AND d.active = true")
    List<String> findTokensByUserIds(List<String> userIds);

    @Query("SELECT d.token FROM DeviceToken d WHERE d.active = true")
    List<String> findAllActiveTokens();
}