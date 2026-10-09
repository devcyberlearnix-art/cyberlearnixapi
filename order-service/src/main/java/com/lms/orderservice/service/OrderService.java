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
import com.lms.orderservice.entity.OrderStatus;
import com.lms.orderservice.repository.OrderItemRepository;
import com.lms.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;


import com.lms.orderservice.dto.CreateOrderRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final CartClient cartClient;
    private final CouponClient couponClient;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${course.service.url:${COURSE_SERVICE_URL:http://localhost:8083}}")
    private String courseServiceUrl;

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
    public Order createOrder(String userId,
                             CreateOrderRequest request) {

        if (request == null || userId == null || userId.isBlank()) {
            throw new RuntimeException("userId is required");
        }

        // 1) Pull cart items for the user
        List<CartItem> cartItems = new ArrayList<>();
        Double cartTotal = 0.0;

        try {
            ApiResponse<CartResponse> cartApi = cartClient.getCart(userId);

            if (cartApi != null && cartApi.isSuccess() && cartApi.getData() != null) {
                CartResponse data = cartApi.getData();

                if (data.getItems() != null) {
                    cartItems = data.getItems();
                }

                cartTotal = Objects.requireNonNullElse(data.getTotalCartPrice(), 0.0);
            }

        } catch (Exception e) {
            logger.warn("Cart service unavailable for userId {}: {}", userId, e.getMessage());
            if (request.getCourseIds() == null || request.getCourseIds().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Cart service is unavailable and no courseIds were provided in request: " + e.getMessage(), e);
            }
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

        double coursePricesTotal = validateAndGetCoursesPrice(courseIds);

        // 3) Apply coupon (optional) per course item using coupon-service validate API
        String couponCode = request.getCouponCode();
        Double requestedTotal = request.getTotalAmount();
        double baseTotal;
        if (requestedTotal != null && requestedTotal > 0.0) {
            baseTotal = requestedTotal;
        } else {
            baseTotal = (cartTotal != null && cartTotal > 0.0) ? cartTotal : coursePricesTotal;
        }
        double finalTotal = baseTotal;
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

        // 4) Redeem coupon (optional) per course BEFORE order persistence
        // This ensures consistency: if redemption fails, order is not created with invalid discount
        if (couponCode != null && !couponCode.isBlank()) {
            for (Long courseId : courseIds) {
                if (courseId == null)
                    continue;
                try {
                    RedeemRequest redeemRequest = new RedeemRequest();
                    redeemRequest.setCouponCode(couponCode);
                    redeemRequest.setCourseId(courseId.toString());
                    couponClient.redeem(redeemRequest);
                } catch (Exception e) {
                    // Coupon redemption is required for consistency
                    // If it fails, we must not create the order with a discount that wasn't applied
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to redeem coupon. Order cannot be created to ensure consistency.", e);
                }
            }
        }

        // 5. Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING.name());
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(finalTotal);

        Order savedOrder = orderRepository.save(order);

        // 6. Save Order Items (courses)
        for (Long courseId : courseIds) {
            OrderItem item = new OrderItem();
            item.setOrderId(savedOrder.getOrderId());
            item.setCourseId(courseId);

            orderItemRepository.save(item);
        }
        savedOrder.setCourseIds(courseIds);

        // 7) Clear the cart after successful order creation
        // This happens after order persistence to avoid duplicate orders on retry
        // Failure is logged but does not fail the order (order is already valid)
        try {
            cartClient.clearCart(userId);
        } catch (Exception e) {
            // Log the failure with context for manual intervention or retry
            logger.error("Failed to clear cart for userId: {} after order creation. Order ID: {}. Error: {}",
                    userId, savedOrder.getOrderId(), e.getMessage(), e);
            // Cart clear is idempotent (deleteByUserId), so retry is safe
            // Order is valid and persisted, so we return success to the client
        }

        return savedOrder;
    }

    private double validateAndGetCoursesPrice(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Long courseId : courseIds) {
            if (courseId == null) {
                continue;
            }
            try {
                String url = courseServiceUrl + "/api/v1/courses/" + courseId;
                var response = restTemplate.getForEntity(url, java.util.Map.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Course not found with id: " + courseId);
                }
                if (response.getBody() != null) {
                    Object dataObj = response.getBody().get("data");
                    if (dataObj instanceof java.util.Map<?, ?> dataMap) {
                        Object priceObj = dataMap.get("price");
                        if (priceObj instanceof Number num) {
                            total += num.doubleValue();
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("Could not validate or fetch course price for id: {}. Error: {}", courseId, ex.getMessage());
                throw new RuntimeException("Course not found with id: " + courseId, ex);
            }
        }
        return total;
    }

    private Order enrichOrder(Order order) {
        if (order != null && order.getOrderId() != null) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            order.setCourseIds(items.stream().map(OrderItem::getCourseId).filter(Objects::nonNull).toList());
        }
        return order;
    }

    // ✅ Get Order
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::enrichOrder)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public Order getOrderForUser(String orderId, String userId) {
        Order order = getOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return order;
    }

    // ✅ Get Orders by User
    public List<Order> getOrdersByUser(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        orders.forEach(this::enrichOrder);
        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        orders.forEach(this::enrichOrder);
        return orders;
    }

    public java.util.Map<String, Object> getOrderAnalytics() {
        List<Order> orders = orderRepository.findAll();
        long totalOrders = orders.size();
        long completedOrders = orders.stream().filter(o -> OrderStatus.COMPLETED.name().equalsIgnoreCase(o.getStatus())).count();
        long pendingOrders = orders.stream().filter(o -> OrderStatus.PENDING.name().equalsIgnoreCase(o.getStatus())).count();
        long cancelledOrders = orders.stream().filter(o -> OrderStatus.CANCELLED.name().equalsIgnoreCase(o.getStatus())).count();

        return java.util.Map.of(
            "totalOrders", totalOrders,
            "completedOrders", completedOrders,
            "pendingOrders", pendingOrders,
            "cancelledOrders", cancelledOrders
        );
    }

    // ✅ Cancel Order
    public String cancelOrder(String orderId, String userId) {
        Order order = getOrderForUser(orderId, userId);

        transition(order, OrderStatus.CANCELLED);
        orderRepository.save(order);

        return "Order Cancelled";
    }

    public String cancelOrderAsAdmin(String orderId) {
        Order order = getOrder(orderId);

        transition(order, OrderStatus.CANCELLED);
        orderRepository.save(order);

        return "Order Cancelled";
    }

    public Order updateStatus(String orderId, String status) {

        Order order = getOrder(orderId);
        OrderStatus target;
        try {
            target = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported order status: " + status);
        }
        transition(order, target);

        return orderRepository.save(order);
    }

    public void completeOrderInternal(String userId, String courseId) {
        List<Order> orders = getOrdersByUser(userId);
        for (Order order : orders) {
            if (OrderStatus.PENDING.name().equalsIgnoreCase(order.getStatus())) {
                List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                for (OrderItem item : items) {
                    if (courseId.equals(item.getCourseId())) {
                        transition(order, OrderStatus.COMPLETED);
                        orderRepository.save(order);
                        break;
                    }
                }
            }
        }
    }

    public Order refundOrder(String orderId, String userId) {
        Order order = getOrderForUser(orderId, userId);

        if (OrderStatus.REFUNDED.name().equals(order.getStatus())) {
            return order;
        }

        transition(order, OrderStatus.REFUNDED);
        return enrichOrder(orderRepository.save(order));
    }

    public Order refundOrderAsAdmin(String orderId) {
        Order order = getOrder(orderId);

        if (OrderStatus.REFUNDED.name().equals(order.getStatus())) {
            return order;
        }

        transition(order, OrderStatus.REFUNDED);
        return enrichOrder(orderRepository.save(order));
    }

    private void transition(Order order, OrderStatus target) {
        OrderStatus current;
        try {
            current = OrderStatus.valueOf(order.getStatus().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Order has unsupported current status: " + order.getStatus());
        }

        boolean allowed = switch (current) {
            case PENDING -> target == OrderStatus.PAID
                    || target == OrderStatus.COMPLETED
                    || target == OrderStatus.CANCELLED
                    || target == OrderStatus.FAILED
                    || target == OrderStatus.REFUNDED;
            case PAID -> target == OrderStatus.COMPLETED
                    || target == OrderStatus.REFUNDED
                    || target == OrderStatus.CANCELLED;
            case COMPLETED -> target == OrderStatus.REFUNDED;
            case CANCELLED -> target == OrderStatus.REFUNDED;
            case FAILED -> target == OrderStatus.REFUNDED;
            case REFUNDED -> false;
        };
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Order cannot transition from " + current + " to " + target);
        }
        order.setStatus(target.name());
    }
}
