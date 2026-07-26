package com.lms.orderservice.service;

import com.lms.orderservice.client.CartClient;
import com.lms.orderservice.client.CouponClient;
import org.springframework.web.client.RestTemplate;
import com.lms.orderservice.client.dto.cart.ApiResponse;
import com.lms.orderservice.client.dto.cart.CartItem;
import com.lms.orderservice.client.dto.cart.CartResponse;
import com.lms.orderservice.client.dto.coupon.RedeemRequest;
import com.lms.orderservice.client.dto.coupon.ValidateRequest;
import com.lms.orderservice.client.dto.coupon.ValidationResponse;
import com.lms.orderservice.entity.Order;
import com.lms.orderservice.entity.OrderItem;
import com.lms.orderservice.repository.OrderItemRepository;
import com.lms.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import com.lms.orderservice.dto.CreateOrderRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final CartClient cartClient;
    private final CouponClient couponClient;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public OrderService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartClient cartClient,
            CouponClient couponClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartClient = cartClient;
        this.couponClient = couponClient;
    }

    // ✅ Create Order
    public Order createOrder(CreateOrderRequest request) {

        if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
            throw new RuntimeException("userId is required");
        }

        // 1) Pull cart items for the user (primary integration path)
        List<CartItem> cartItems = new ArrayList<>();
        Double cartTotal = 0.0;
        try {
            ApiResponse<CartResponse> cartApi = cartClient.getCart(request.getUserId());
            if (cartApi != null && cartApi.isSuccess() && cartApi.getData() != null) {
                CartResponse data = cartApi.getData();
                if (data.getItems() != null)
                    cartItems = data.getItems();
                cartTotal = Objects.requireNonNullElse(data.getTotalCartPrice(), 0.0);
            }
        } catch (Exception ignored) {
            // If cart-service is unavailable, we still allow creating an order via explicit
            // courseIds.
        }

        // 2) Determine which courseIds to persist as order items
        List<Long> courseIds = request.getCourseIds();
        if ((courseIds == null || courseIds.isEmpty()) && (cartItems == null || cartItems.isEmpty())) {
            throw new RuntimeException("No items to order. Provide courseIds or add items to cart.");
        }

        if (courseIds == null || courseIds.isEmpty()) {
            courseIds = cartItems.stream()
                    .map(CartItem::getCourseId)
                    .filter(Objects::nonNull)
                    .toList();
        }

        validateCourseIds(courseIds);

        // 3) Apply coupon (optional) per course item using coupon-service validate API
        String couponCode = request.getCouponCode();
        double finalTotal = cartTotal != null ? cartTotal : 0.0;
        if (couponCode != null && !couponCode.isBlank() && cartItems != null && !cartItems.isEmpty()) {
            finalTotal = 0.0;
            for (CartItem item : cartItems) {
                if (item == null || item.getCourseId() == null)
                    continue;
                double itemPrice = Objects.requireNonNullElse(item.getSubTotal(),
                        Objects.requireNonNullElse(item.getUnitPrice(), 0.0)
                                * Objects.requireNonNullElse(item.getQuantity(), 1));

                ValidateRequest validateRequest = new ValidateRequest();
                validateRequest.setCouponCode(couponCode);
                validateRequest.setUserId(request.getUserId());
                validateRequest.setCourseId(String.valueOf(item.getCourseId()));
                validateRequest.setPrice(itemPrice);

                ValidationResponse validation = couponClient.validate(validateRequest);
                if (validation != null && validation.isValid() && validation.getFinalPrice() != null) {
                    finalTotal += validation.getFinalPrice();
                } else {
                    finalTotal += itemPrice;
                }
            }
        }

        // 4. Create Order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(finalTotal);

        Order savedOrder = orderRepository.save(order);

        // 5. Save Order Items (courses)
        for (Long courseId : courseIds) {
            OrderItem item = new OrderItem();
            item.setOrderId(savedOrder.getOrderId());
            item.setCourseId(courseId);

            orderItemRepository.save(item);
        }

        // 6) Redeem coupon (optional) per course
        if (couponCode != null && !couponCode.isBlank()) {
            for (Long courseId : courseIds) {
                if (courseId == null)
                    continue;
                try {
                    RedeemRequest redeemRequest = new RedeemRequest();
                    redeemRequest.setCouponCode(couponCode);
                    redeemRequest.setUserId(request.getUserId());
                    redeemRequest.setCourseId(courseId.toString());
                    couponClient.redeem(redeemRequest);
                } catch (Exception ignored) {
                    // do not fail the order if coupon redemption fails; coupon-service can be
                    // retried later
                }
            }
        }

        // 7) Clear the cart after successful order creation
        try {
            cartClient.clearCart(request.getUserId());
        } catch (Exception ignored) {
            // cart clear can be retried; order is already persisted
        }

        return savedOrder;
    }

    private void validateCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return;
        }

        for (Long courseId : courseIds) {
            if (courseId == null) {
                continue;
            }
            try {
                String url = "http://localhost:8083/api/v1/courses/" + courseId;
                var response = restTemplate.getForEntity(url, Object.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Course not found with id: " + courseId);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Course not found with id: " + courseId, ex);
            }
        }
    }

    // ✅ Get Order
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // ✅ Get All Orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ Get Orders by User
    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    // ✅ Cancel Order
    public String cancelOrder(String orderId) {
        Order order = getOrder(orderId);

        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel completed order");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        return "Order Cancelled";
    }

    public Order updateStatus(String orderId, String status) {

        Order order = getOrder(orderId);
        order.setStatus(status);

        return orderRepository.save(order);
    }

    public void completeOrderInternal(String userId, String courseId) {
        List<Order> orders = getOrdersByUser(userId);
        for (Order order : orders) {
            if ("PENDING".equalsIgnoreCase(order.getStatus())) {
                List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                for (OrderItem item : items) {
                    if (courseId.equals(item.getCourseId())) {
                        order.setStatus("COMPLETED");
                        orderRepository.save(order);
                        break;
                    }
                }
            }
        }
    }

    public String refundOrder(String orderId) {
        Order order = getOrder(orderId);

        if ("REFUNDED".equals(order.getStatus())) {
            return "Order is already refunded";
        }

        if (!"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Only completed orders can be refunded. Current status: " + order.getStatus());
        }

        order.setStatus("REFUNDED");
        orderRepository.save(order);

        return "Refund processed";
    }
}