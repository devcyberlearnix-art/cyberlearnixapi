package com.lms.wishlist_service.service;

import com.lms.wishlist_service.client.CourseClient;
import com.lms.wishlist_service.dto.*;
import com.lms.wishlist_service.entity.WishlistItem;
import com.lms.wishlist_service.exception.WishlistException;
import com.lms.wishlist_service.repository.WishlistRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository repository;
    private final CourseClient courseClient;

    /**
     * Adds an item to the database.
     * Throws WishlistException (409 CONFLICT) if duplicate is found.
     */
    public WishlistResponse addToWishlist(String userId, String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new WishlistException("Course ID is required", HttpStatus.BAD_REQUEST);
        }

        validateCourseExists(courseId);

        if (repository.findByUserIdAndCourseId(userId, Long.valueOf(courseId)).isPresent()) {
            throw new WishlistException("Course is already in your wishlist", HttpStatus.CONFLICT);
        }

        WishlistItem item = WishlistItem.builder()
                .userId(userId)
                .courseId(Long.valueOf(courseId))
                .build();

        WishlistItem savedItem = repository.save(item);
        return mapToWishlistResponse(savedItem);
    }

    /**
     * Retrieves all items for a specific user.
     */
    public WishlistListResponse getWishlist(String userId) {
        List<WishlistItem> entities = repository.findByUserId(userId);

        List<WishlistResponse> responses = entities.stream()
                .map(this::mapToWishlistResponse)
                .collect(Collectors.toList());

        return WishlistListResponse.builder()
                .userId(userId)
                .totalItems(responses.size())
                .items(responses)
                .build();
    }

    public WishlistResponse getWishlistItemById(String userId, String wishlistId) {
        return repository.findById(UUID.fromString(wishlistId))
                .filter(item -> item.getUserId().equals(userId))
                .map(this::mapToWishlistResponse)
                .orElseThrow(() -> new WishlistException("Wishlist item not found", HttpStatus.NOT_FOUND));
    }

    public boolean checkExists(String userId, String courseId) {
        return repository.findByUserIdAndCourseId(userId, Long.valueOf(courseId)).isPresent();
    }

    @Transactional
    public RemoveFromWishlistResponse removeFromWishlist(String userId, String courseId) {
        WishlistItem item = repository.findByUserIdAndCourseId(userId, Long.valueOf(courseId))
                .orElseThrow(() -> new WishlistException("Course not found in the wishlist.", HttpStatus.NOT_FOUND));

        repository.deleteByUserIdAndCourseId(userId, Long.valueOf(courseId));

        return RemoveFromWishlistResponse.builder()
                .courseId(courseId)
                .removedAt(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .build();
    }

    @Transactional
    public String clearWishlist(String userId) {
        List<WishlistItem> items = repository.findByUserId(userId);

        if (items.isEmpty()) {
            return "Wishlist was already empty";
        }

        repository.deleteByUserId(userId);
        return "Wishlist cleared successfully";
    }

    @Transactional
    public MoveToCartResponse moveToCart(String userId, String courseId) {
        // TODO: replace placeholder logic with a CartClient integration
        removeFromWishlist(userId, courseId);

        return MoveToCartResponse.builder()
                .courseId(courseId)
                .moveToCartStatus("COURSE_MOVED")
                .message("Course moved to cart successfully")
                .movedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Maps the DB Entity to the nested DTO structure.
     */
    private void validateCourseExists(String courseId) {
        try {
            CourseDetails course = courseClient.getCourseById(Long.valueOf(courseId));
            if (course == null || course.getCourseId() == null || course.getCourseId().isBlank()) {
                throw new WishlistException("Course not found with id: " + courseId, HttpStatus.NOT_FOUND);
            }
        } catch (FeignException.NotFound ex) {
            throw new WishlistException("Course not found with id: " + courseId, HttpStatus.NOT_FOUND);
        } catch (FeignException ex) {
            throw new WishlistException("Unable to validate course at the moment", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception ex) {
            throw new WishlistException("Unable to validate course at the moment", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private WishlistResponse mapToWishlistResponse(WishlistItem entity) {

        CourseDetails course = courseClient.getCourseById(entity.getCourseId());

        return WishlistResponse.builder()
                .wishlistId(entity.getId())
                .userId(entity.getUserId())
                .addedAt(entity.getAddedAt())
                .course(course)
                .build();

    }
}