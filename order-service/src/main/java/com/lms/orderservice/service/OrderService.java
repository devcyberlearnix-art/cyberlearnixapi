package com.lms.orderservice.service;

import com.lms.orderservice.client.CartClient;
import com.lms.orderservice.client.CouponClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
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

    // Create Order
    public Order createOrder(String userId,
                             CreateOrderRequest request) {

        if (request == null || userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No items to order. Provide courseIds or add items to cart.");
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
        double baseTotal = coursePricesTotal;
        if (baseTotal <= 0.0 && cartTotal != null && cartTotal > 0.0) {
            baseTotal = cartTotal;
        }

        double finalTotal = baseTotal;
        if (couponCode != null && !couponCode.isBlank()) {
            finalTotal = 0.0;
            for (Long courseId : courseIds) {
                if (courseId == null) continue;
                double coursePrice = getCoursePrice(courseId);

                ValidateRequest validateReq = new ValidateRequest();
                validateReq.setCouponCode(couponCode.trim());
                validateReq.setCourseId(courseId);
                validateReq.setPrice(coursePrice);

                try {
                    ApiResponse<ValidationResponse> valResp = couponClient.validate(validateReq);
                    ValidationResponse valData = valResp != null ? valResp.getData() : null;
                    if (valData == null || !valData.isValid() || valData.getFinalPrice() == null) {
                        String errMsg = (valData != null && valData.getMessage() != null && !valData.getMessage().isBlank())
                                ? valData.getMessage()
                                : "Invalid or expired coupon: " + couponCode;
                        logger.warn("Coupon validation rejected for code {}: {}", couponCode, errMsg);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errMsg);
                    }
                    finalTotal += valData.getFinalPrice();
                } catch (ResponseStatusException rse) {
                    throw rse;
                } catch (Exception ex) {
                    logger.warn("Coupon validation service call failed for code {}: {}", couponCode, ex.getMessage());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired coupon code: " + couponCode);
                }
            }
        }

        // Backend validation: Do not trust unverified client-supplied total without validation
        if (requestedTotal != null && requestedTotal > 0.0) {
            if (Math.abs(requestedTotal - finalTotal) > 0.05) {
                logger.warn("Order total tampering detected! Client requested {}, but verified order total is {}",
                        requestedTotal, finalTotal);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order total mismatch: client total (" + requestedTotal + ") does not match verified order price (" + finalTotal + ")");
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
                    redeemRequest.setCouponCode(couponCode.trim());
                    redeemRequest.setCourseId(courseId.toString());
                    couponClient.redeem(redeemRequest);
                } catch (Exception e) {
                    logger.error("Failed to redeem coupon {} for course {}: {}", couponCode, courseId, e.getMessage());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Failed to redeem coupon: " + couponCode + ". Order cannot be created.", e);
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
            logger.error("Failed to clear cart for userId: {} after order creation. Order ID: {}. Error: {}",
                    userId, savedOrder.getOrderId(), e.getMessage(), e);
        }

        return savedOrder;
    }

    private double validateAndGetCoursesPrice(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Long courseId : courseIds) {
            if (courseId == null || courseId <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid course id: " + courseId);
            }
            total += getCoursePrice(courseId);
        }
        return total;
    }

    private double getCoursePrice(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid course id: " + courseId);
        }
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId;
            var response = restTemplate.getForEntity(url, java.util.Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found with id: " + courseId);
            }
            Object priceObj = response.getBody().get("price");
            if (priceObj == null) {
                Object dataObj = response.getBody().get("data");
                if (dataObj instanceof java.util.Map<?, ?> dataMap) {
                    priceObj = dataMap.get("price");
                }
            }
            if (priceObj instanceof Number num) {
                return num.doubleValue();
            } else {
                logger.warn("Course {} has no valid price defined", courseId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course price could not be determined for id: " + courseId);
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 404 || (ex.getResponseBodyAsString() != null && ex.getResponseBodyAsString().contains("Course not found"))) {
                logger.warn("Course not found in course-service for id: {}", courseId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found with id: " + courseId);
            }
            logger.warn("Client error fetching course id {}: status {}", courseId, ex.getStatusCode());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid course id: " + courseId);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            logger.error("Unexpected error validating course id {}: {}", courseId, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Course service is currently unavailable", ex);
        }
    }

    private Order enrichOrder(Order order) {
        if (order != null && order.getOrderId() != null) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            order.setCourseIds(items.stream().map(OrderItem::getCourseId).filter(Objects::nonNull).toList());
        }
        return order;
    }

    // Get Order
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

    // Get Orders by User
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

    // Cancel Order
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
                    if (courseId != null && courseId.equals(String.valueOf(item.getCourseId()))) {
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
