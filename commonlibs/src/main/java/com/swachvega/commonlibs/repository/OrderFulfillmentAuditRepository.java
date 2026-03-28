package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.OrderFulfillmentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderFulfillmentAuditRepository extends JpaRepository<OrderFulfillmentAudit, Long> {
    List<OrderFulfillmentAudit> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<OrderFulfillmentAudit> findByProductIdOrderByCreatedAtDesc(Long productId);
}
