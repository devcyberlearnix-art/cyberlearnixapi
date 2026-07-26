package com.lms.wishlist_service.controller;

import com.lms.wishlist_service.dto.*;
import com.lms.wishlist_service.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService service;

    /**
     * Professional ADD: Accepts a JSON body with courseId.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> add(Authentication auth,
            @RequestBody WishlistCreateRequest request) {
        String userId = auth.getName();
        WishlistResponse data = service.addToWishlist(userId, request.getCourseId().toString());

        ApiResponse<WishlistResponse> response = ApiResponse.<WishlistResponse>builder()
                .status("SUCCESS")
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .message("Course added to wishlist successfully")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Professional GET: Returns the authenticated student's wishlist.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<WishlistListResponse>> get(Authentication auth) {
        String userId = auth.getName();
        WishlistListResponse data = service.getWishlist(userId);

        ApiResponse<WishlistListResponse> response = ApiResponse.<WishlistListResponse>builder()
                .status("SUCCESS")
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .message("Wishlist retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional GET: Retrieve a single wishlist item by its ID.
     */
    @GetMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> getById(Authentication auth, @PathVariable String wishlistId) {
        String userId = auth.getName();
        WishlistResponse data = service.getWishlistItemById(userId, wishlistId);

        ApiResponse<WishlistResponse> response = ApiResponse.<WishlistResponse>builder()
                .status("SUCCESS")
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .message("Wishlist item retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional CONTAINS: Wraps the check in the same Envelope.
     */
    @GetMapping("/check/{courseId}")
    public ResponseEntity<ApiResponse<WishlistCheckResponse>> contains(Authentication auth,
            @PathVariable Long courseId) {
        String userId = auth.getName();
        boolean exists = service.checkExists(userId, courseId.toString());

        WishlistCheckResponse checkData = WishlistCheckResponse.builder()
                .userId(userId)
                .courseId(courseId.toString())
                .isInWishlist(exists)
                .checkedAt(LocalDateTime.now())
                .statusMessage(exists ? "Course is in your wishlist" : "Course is not in your wishlist")
                .build();

        ApiResponse<WishlistCheckResponse> response = ApiResponse.<WishlistCheckResponse>builder()
                .status("SUCCESS")
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(checkData)
                .message("Status checked successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional MOVE: Returns a success message inside the data block.
     */
    @PostMapping("/{courseId}/move-to-cart")
    public ResponseEntity<ApiResponse<MoveToCartResponse>> moveToCart(Authentication auth,
            @PathVariable Long courseId) {
        String userId = auth.getName();
        MoveToCartResponse data = service.moveToCart(userId, courseId.toString());

        ApiResponse<MoveToCartResponse> response = ApiResponse.<MoveToCartResponse>builder()
                .status("SUCCESS")
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .message("Course moved to cart successfully.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional REMOVE: Standardizes the response even for deletions.
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<RemoveFromWishlistResponse>> remove(Authentication auth,
            @PathVariable Long courseId) {
        String userId = auth.getName();
        RemoveFromWishlistResponse data = service.removeFromWishlist(userId, courseId.toString());

        ApiResponse<RemoveFromWishlistResponse> response = ApiResponse.<RemoveFromWishlistResponse>builder()
                .status("SUCCESS")
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .message("Course removed from wishlist successfully.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional CLEAR: Final cleanup.
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clear(Authentication auth) {
        String userId = auth.getName();
        String message = service.clearWishlist(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("SUCCESS")
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(null) // Make sure this is here!
                .message(message)
                .build();

        return ResponseEntity.ok(response);
    }
}