package com.lms.orderservice.service;

import com.lms.orderservice.client.CartClient;
import com.lms.orderservice.client.CouponClient;
import com.lms.orderservice.dto.CreateOrderRequest;
import com.lms.orderservice.repository.OrderItemRepository;
import com.lms.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartClient cartClient;

    @Mock
    private CouponClient couponClient;

    @InjectMocks
    private OrderService service;

    @Test
    void createOrder_shouldRejectUnknownCourseIds() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user-1");
        request.setCourseIds(List.of("999"));

        when(cartClient.getCart("user-1")).thenThrow(new RuntimeException("cart unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.createOrder(request));
        assert exception.getMessage().contains("Course not found");
    }
}
