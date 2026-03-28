package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.DeliveryCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DeliveryChargeRepository extends JpaRepository<DeliveryCharge, UUID> {
    List<DeliveryCharge> findByPromotionId(String promotionId);
    List<DeliveryCharge> findByStoreId(Long storeId);
    List<DeliveryCharge> findByPromotionIdAndStoreId(String promotionId, Long storeId);
    List<DeliveryCharge> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
