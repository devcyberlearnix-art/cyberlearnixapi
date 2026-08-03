package com.lms.wishlist_service.repository;

import com.lms.wishlist_service.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<WishlistItem, UUID> {
    List<WishlistItem> findByUserId(String userId);
    Optional<WishlistItem> findByUserIdAndCourseId(String userId, Long courseId);
    void deleteByUserId(String userId);
    void deleteByUserIdAndCourseId(String userId, Long courseId);
}