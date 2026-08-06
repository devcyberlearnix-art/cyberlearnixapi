package com.lms.orderservice.repository;

import com.lms.orderservice.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, String> {

    List<Refund> findByOrderId(String orderId);
}