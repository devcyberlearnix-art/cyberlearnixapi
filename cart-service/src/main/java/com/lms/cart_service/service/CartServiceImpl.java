package com.lms.cart_service.service;

import com.lms.cart_service.client.CourseClient;
import com.lms.cart_service.client.CouponClient;
import com.lms.cart_service.client.OrderClient;
import com.lms.cart_service.dto.*;
import com.lms.cart_service.dto.InstructorApiResponse;
import com.lms.cart_service.entity.CartItem;
import com.lms.cart_service.exception.ResourceNotFoundException;
import com.lms.cart_service.repository.CartRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CourseClient courseClient;
    private final CouponClient couponClient;
    private final OrderClient orderClient;

    @Override
    @Transactional
    public AddCourseResponse addToCart(String userId, CartRequest request) {
        CourseDetails courseData;
        try {
            courseData = courseClient.getCourseById(request.getCourseId());
        } catch (FeignException e) {
            int status = e.status();
            String body = e.contentUTF8();
            if (status == 404 || (status == 400 && body != null && body.contains("Course not found"))) {
                throw new ResourceNotFoundException("Course not found with id: " + request.getCourseId());
            }
            throw e;
        }

        if (courseData == null) {
            throw new ResourceNotFoundException("Course not found with id: " + request.getCourseId());
        }

        var existing = cartRepository.findByUserIdAndCourseIdAndInstructorId(
                userId, courseData.getCourseId(), courseData.getInstructorId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + 1);
            item.setPrice(courseData.getPrice());
            cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setCourseId(courseData.getCourseId());
            newItem.setInstructorId(courseData.getInstructorId());
            newItem.setCourseName(courseData.getCourseName());
            newItem.setPrice(courseData.getPrice());
            newItem.setQuantity(1);

            cartRepository.save(newItem);
        }
        return new AddCourseResponse(String.valueOf(courseData.getCourseId()), Instant.now().toString());
    }

    @Override
    public MyCartResponse getUserCart(String userId) {
        CartResponse cart = buildCartResponse(userId);
        String cartId = UUID.nameUUIDFromBytes(userId.getBytes()).toString();

        List<CourseInCartResponse> courses = cart.getItems().stream()
                .map(item -> new CourseInCartResponse(String.valueOf(item.getCourseId()), item.getCourseName(), item.getUnitPrice()))
                .toList();

        return new MyCartResponse(cartId, courses.size(), courses);
    }

    private CartResponse buildCartResponse(String userId) {
        List<CartItem> entities = cartRepository.findAllByUserId(userId);

        List<com.lms.cart_service.dto.CartItem> dtos = entities.stream().map(entity -> {
            com.lms.cart_service.dto.CartItem dto = new com.lms.cart_service.dto.CartItem();
            dto.setCartId(entity.getId());
            dto.setInstructorId(entity.getInstructorId());
            dto.setCourseId(entity.getCourseId());
            dto.setCourseName(entity.getCourseName());
            dto.setUnitPrice(entity.getPrice());
            dto.setQuantity(entity.getQuantity());
            dto.setSubTotal(entity.getPrice() * entity.getQuantity());
            return dto;
        }).toList();

        Double grandTotal = dtos.stream().mapToDouble(d -> d.getSubTotal()).sum();
        return new CartResponse(dtos, grandTotal);
    }

    @Override
    public CartSummaryResponse getCartSummary(String userId) {
        CartResponse cart = buildCartResponse(userId);
        double subtotal = cart.getTotalCartPrice();
        double discount = 0.0;
        double totalAmount = subtotal - discount;

        return CartSummaryResponse.builder()
                .totalCourses(cart.getItems().size())
                .subtotal(subtotal)
                .discount(discount)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional
    public CouponApplyResponse applyCouponToCart(String userId, String couponCode) {
        CartResponse response = buildCartResponse(userId);
        double discountAmount = 0.0;

        Double discountPercentage = couponClient.getDiscount(couponCode);
        if (discountPercentage != null && discountPercentage > 0) {
            discountAmount = (response.getTotalCartPrice() * discountPercentage) / 100;
            response.setTotalCartPrice(response.getTotalCartPrice() - discountAmount);
            
            // Store coupon code in all cart items for this user
            List<CartItem> items = cartRepository.findAllByUserId(userId);
            items.forEach(item -> item.setCouponCode(couponCode));
            cartRepository.saveAll(items);
        }

        return new CouponApplyResponse(couponCode, discountAmount, response.getTotalCartPrice());
    }

    @Override
    @Transactional
    public CouponApplyResponse removeCouponFromCart(String userId) {
        CartResponse response = buildCartResponse(userId);
        
        // Clear coupon code from all cart items for this user
        List<CartItem> items = cartRepository.findAllByUserId(userId);
        items.forEach(item -> item.setCouponCode(null));
        cartRepository.saveAll(items);
        
        return new CouponApplyResponse(null, 0.0, response.getTotalCartPrice());
    }

    @Override
    public RemoveCourseResponse removeCourseFromCart(String userId, Long courseId) {
        CartItem item = cartRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Course not found in cart"));

        cartRepository.delete(item);
        return new RemoveCourseResponse(courseId);
    }

    @Override
    public CheckoutResponse checkoutCart(String userId, String authorization) {
        CartResponse response = buildCartResponse(userId);
        if (response.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        List<Long> courseIds = response.getItems().stream()
                .map(com.lms.cart_service.dto.CartItem::getCourseId)
                .toList();
        
        // Retrieve coupon code from cart items (if any was applied)
        String couponCode = null;
        List<CartItem> cartItems = cartRepository.findAllByUserId(userId);
        if (!cartItems.isEmpty() && cartItems.get(0).getCouponCode() != null) {
            couponCode = cartItems.get(0).getCouponCode();
        }
        
        OrderClient.OrderApiResponse orderResponse = orderClient.createOrder(
                authorization, new OrderClient.OrderCreateRequest(courseIds, couponCode));
        if (orderResponse == null || !orderResponse.success() || orderResponse.data() == null) {
            throw new IllegalStateException("Order service did not create the checkout order");
        }

        return CheckoutResponse.builder()
                .orderId(orderResponse.data().orderId())
                .paymentMethod("RAZORPAY")
                .paymentStatus(orderResponse.data().status())
                .totalAmount(orderResponse.data().totalAmount())
                .build();
    }

    @Transactional
    public CartResponse removeFromCart(String userId, Long cartId, String instructorId, String courseId) {
        CartItem item = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartRepository.save(item);
        } else {
            cartRepository.delete(item);
        }
        return buildCartResponse(userId);
    }

    @Override
    @Transactional
    public void clearFullCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
    @Override
    public CartResponse getCartForOrderService(String userId) {
        return buildCartResponse(userId);
    }
}