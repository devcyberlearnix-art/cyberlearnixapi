package com.lms.seeder.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseVerifier {

    private final Map<String, JdbcTemplate> jdbcTemplates;

    public void verifyAll() {
        log.info("========================================");
        log.info("DATABASE VERIFICATION");
        log.info("========================================");
        
        verifyUsers();
        verifyCourses();
        verifyEnrollments();
        verifyCart();
        verifyWishlist();
        verifyReviews();
        verifyCoupons();
        verifyOrders();
        verifyPayments();
        verifyNotifications();
        verifyInstructors();
        
        verifyCourseThumbnails();
        verifyCourseStatusDistribution();
        verifyNoOrphanedRecords();
        verifyDuplicateRecords();
        
        log.info("========================================");
        log.info("VERIFICATION COMPLETED");
        log.info("========================================");
    }

    private void verifyUsers() {
        JdbcTemplate userJdbc = jdbcTemplates.get("user");
        int count = userJdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        int activeCount = userJdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'", Integer.class);
        int studentCount = userJdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'", Integer.class);
        int instructorCount = userJdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE role = 'INSTRUCTOR'", Integer.class);
        
        log.info("Users: total={}, active={}, students={}, instructors={}",
                count, activeCount, studentCount, instructorCount);
    }

    private void verifyCourses() {
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        int count = courseJdbc.queryForObject("SELECT COUNT(*) FROM courses", Integer.class);
        int publishedCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE status = 'PUBLISHED'", Integer.class);
        int draftCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE status = 'DRAFT'", Integer.class);
        int archivedCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE status = 'ARCHIVED'", Integer.class);
        int premiumCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE premium = true", Integer.class);
        
        log.info("Courses: total={}, published={}, draft={}, archived={}, premium={}",
                count, publishedCount, draftCount, archivedCount, premiumCount);
    }

    private void verifyEnrollments() {
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        int count = courseJdbc.queryForObject("SELECT COUNT(*) FROM enrollment", Integer.class);
        int activeCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM enrollment WHERE status = 'ACTIVE'", Integer.class);

        log.info("Enrollments: total={}, active={}", count, activeCount);
    }

    private void verifyCart() {
        JdbcTemplate cartJdbc = jdbcTemplates.get("cart");
        int count = cartJdbc.queryForObject("SELECT COUNT(*) FROM cart_items", Integer.class);
        
        log.info("Cart Items: {}", count);
    }

    private void verifyWishlist() {
        JdbcTemplate wishlistJdbc = jdbcTemplates.get("wishlist");
        int count = wishlistJdbc.queryForObject("SELECT COUNT(*) FROM wishlist_items", Integer.class);
        
        log.info("Wishlist Items: {}", count);
    }

    private void verifyReviews() {
        JdbcTemplate reviewJdbc = jdbcTemplates.get("review");
        int count = reviewJdbc.queryForObject("SELECT COUNT(*) FROM reviews", Integer.class);
        int activeCount = reviewJdbc.queryForObject(
            "SELECT COUNT(*) FROM reviews WHERE status = 'ACTIVE'", Integer.class);
        Double avgRating = reviewJdbc.queryForObject(
            "SELECT AVG(rating) FROM reviews", Double.class);

        double finalAvgRating = (avgRating != null) ? avgRating : 0.0d;
        log.info("Reviews: total={}, active={}, avg_rating={}",
                count, activeCount, String.format("%.2f", finalAvgRating));
    }

    private void verifyCoupons() {
        JdbcTemplate couponJdbc = jdbcTemplates.get("coupon");
        int count = couponJdbc.queryForObject("SELECT COUNT(*) FROM coupons", Integer.class);
        int activeCount = couponJdbc.queryForObject(
            "SELECT COUNT(*) FROM coupons WHERE is_active = true", Integer.class);
        
        log.info("Coupons: total={}, active={}", count, activeCount);
    }

    private void verifyOrders() {
        JdbcTemplate orderJdbc = jdbcTemplates.get("order");
        int count = orderJdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        int completedCount = orderJdbc.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'", Integer.class);
        
        log.info("Orders: total={}, completed={}", count, completedCount);
    }

    private void verifyPayments() {
        JdbcTemplate paymentJdbc = jdbcTemplates.get("payment");
        int count = paymentJdbc.queryForObject("SELECT COUNT(*) FROM payments", Integer.class);
        int successCount = paymentJdbc.queryForObject(
            "SELECT COUNT(*) FROM payments WHERE status = 'SUCCESS'", Integer.class);
        
        log.info("Payments: total={}, success={}", count, successCount);
    }

    private void verifyNotifications() {
        JdbcTemplate notificationJdbc = jdbcTemplates.get("notification");
        int count = notificationJdbc.queryForObject("SELECT COUNT(*) FROM notifications", Integer.class);
        int sentCount = notificationJdbc.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE status = 'SENT'", Integer.class);
        
        log.info("Notifications: total={}, sent={}", count, sentCount);
    }

    private void verifyInstructors() {
        JdbcTemplate instructorJdbc = jdbcTemplates.get("instructor");
        int count = instructorJdbc.queryForObject("SELECT COUNT(*) FROM instructor", Integer.class);
        
        log.info("Instructors: {}", count);
    }

    private void verifyCourseThumbnails() {
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        
        int nullThumbnailCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE thumbnail IS NULL OR thumbnail = ''", Integer.class);
        
        int invalidThumbnailCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE thumbnail NOT LIKE 'https://res.cloudinary.com/%'",
            Integer.class);
        
        log.info("Course Thumbnails: null={}, invalid_https={}",
                nullThumbnailCount, invalidThumbnailCount);
        
        if (nullThumbnailCount > 0 || invalidThumbnailCount > 0) {
            log.warn("WARNING: Some courses have missing or invalid thumbnails!");
            
            // Show some examples
            List<Map<String, Object>> problematicCourses = courseJdbc.queryForList(
                "SELECT title, thumbnail FROM courses WHERE thumbnail IS NULL OR thumbnail = '' OR thumbnail NOT LIKE 'https://res.cloudinary.com/%' LIMIT 5");
            
            if (!problematicCourses.isEmpty()) {
                log.warn("Example problematic courses: {}", problematicCourses);
            }
        } else {
            log.info("✓ All courses have valid Cloudinary HTTPS thumbnails");
        }
    }

    private void verifyCourseStatusDistribution() {
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        
        int published = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE status = 'PUBLISHED'", Integer.class);
        int draft = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE status = 'DRAFT'", Integer.class);
        int archived = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses WHERE status = 'ARCHIVED'", Integer.class);
        int total = published + draft + archived;
        
        log.info("Course Status Distribution:");
        log.info("  PUBLISHED: {} ({})", published, String.format("%.1f%%", published * 100.0 / total));
        log.info("  DRAFT: {} ({})", draft, String.format("%.1f%%", draft * 100.0 / total));
        log.info("  ARCHIVED: {} ({})", archived, String.format("%.1f%%", archived * 100.0 / total));
        
        // Check if distribution is within expected ranges
        boolean publishedOk = published >= 280 && published <= 320;
        boolean draftOk = draft >= 40 && draft <= 60;
        boolean archivedOk = archived >= 20 && archived <= 30;
        
        if (publishedOk && draftOk && archivedOk) {
            log.info("✓ Course status distribution is within expected ranges");
        } else {
            log.warn("WARNING: Course status distribution is outside expected ranges!");
        }
    }

    private void verifyNoOrphanedRecords() {
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        JdbcTemplate userJdbc = jdbcTemplates.get("user");

        // Check enrollments with invalid course_id (same database)
        int invalidCourseEnrollments = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM enrollment WHERE course_id NOT IN (SELECT id FROM courses)",
            Integer.class);

        // Note: Cross-database orphaned checks are skipped due to PostgreSQL limitations
        // User ID validation would require fetching user IDs from lms_user_db separately

        log.info("Orphaned Records Check:");
        log.info("  Enrollments with invalid course_id: {}", invalidCourseEnrollments);
        log.info("  (Cross-database user ID checks skipped - PostgreSQL limitation)");

        if (invalidCourseEnrollments == 0) {
            log.info("✓ No orphaned records found (within single database)");
        } else {
            log.warn("WARNING: Orphaned records detected!");
        }
    }

    private void verifyDuplicateRecords() {
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        JdbcTemplate cartJdbc = jdbcTemplates.get("cart");
        JdbcTemplate wishlistJdbc = jdbcTemplates.get("wishlist");
        JdbcTemplate reviewJdbc = jdbcTemplates.get("review");
        
        // Check duplicate courses (title + instructor)
        int duplicateCourses = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM (SELECT title, instructor_id, COUNT(*) FROM courses GROUP BY title, instructor_id HAVING COUNT(*) > 1) AS duplicates",
            Integer.class);
        
        // Check duplicate enrollments
        int duplicateEnrollments = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM (SELECT student_id, course_id, COUNT(*) FROM enrollment GROUP BY student_id, course_id HAVING COUNT(*) > 1) AS duplicates",
            Integer.class);
        
        // Check duplicate cart items
        int duplicateCart = cartJdbc.queryForObject(
            "SELECT COUNT(*) FROM (SELECT user_id, course_id, COUNT(*) FROM cart_items GROUP BY user_id, course_id HAVING COUNT(*) > 1) AS duplicates",
            Integer.class);
        
        // Check duplicate wishlist items
        int duplicateWishlist = wishlistJdbc.queryForObject(
            "SELECT COUNT(*) FROM (SELECT user_id, course_id, COUNT(*) FROM wishlist_items GROUP BY user_id, course_id HAVING COUNT(*) > 1) AS duplicates",
            Integer.class);
        
        // Check duplicate reviews
        int duplicateReviews = reviewJdbc.queryForObject(
            "SELECT COUNT(*) FROM (SELECT user_id, course_id, COUNT(*) FROM reviews GROUP BY user_id, course_id HAVING COUNT(*) > 1) AS duplicates",
            Integer.class);
        
        log.info("Duplicate Records Check:");
        log.info("  Duplicate courses (title+instructor): {}", duplicateCourses);
        log.info("  Duplicate enrollments: {}", duplicateEnrollments);
        log.info("  Duplicate cart items: {}", duplicateCart);
        log.info("  Duplicate wishlist items: {}", duplicateWishlist);
        log.info("  Duplicate reviews: {}", duplicateReviews);
        
        if (duplicateCourses == 0 && duplicateEnrollments == 0 && duplicateCart == 0 && 
            duplicateWishlist == 0 && duplicateReviews == 0) {
            log.info("✓ No duplicate records found");
        } else {
            log.warn("WARNING: Duplicate records detected!");
        }
    }
}
