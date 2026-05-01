package com.lms.cart_service.service;

import com.lms.cart_service.client.CourseClient;
import com.lms.cart_service.client.CouponClient;
import com.lms.cart_service.dto.*;
import com.lms.cart_service.entity.CartItem;
import com.lms.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CourseClient courseClient;
    private final CouponClient couponClient;

    @Override
    @Transactional
    public CartResponse addToCart(String userId, CartRequest request) {
        CourseDetails courseData = courseClient.getCourseById(request.getCourseId());

        if (courseData == null) {
            throw new RuntimeException("Course not found in the catalog");
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
        return getUserCart(userId);
    }

    @Override
    public CartResponse getUserCart(String userId) {
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

        // FIXED: Using your actual DTO field name 'totalCartPrice'
        return new CartResponse(dtos, grandTotal);
    }

    @Override
    public CartResponse applyCouponToCart(String userId, String couponCode) {
        CartResponse response = getUserCart(userId);

        // FIXED: Calling your actual client method 'getDiscount' which returns Double
        Double discountPercentage = couponClient.getDiscount(couponCode);

        if (discountPercentage != null && discountPercentage > 0) {
            // FIXED: Using 'totalCartPrice' instead of 'grandTotal'
            double discountAmount = (response.getTotalCartPrice() * discountPercentage) / 100;
            response.setTotalCartPrice(response.getTotalCartPrice() - discountAmount);
        }

        return response;
    }

    @Override
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
        return getUserCart(userId);
    }

    @Override
    @Transactional
    public CartResponse clearFullCart(String userId) {
        cartRepository.deleteByUserId(userId);
        return new CartResponse(new ArrayList<>(), 0.0);
    }
}